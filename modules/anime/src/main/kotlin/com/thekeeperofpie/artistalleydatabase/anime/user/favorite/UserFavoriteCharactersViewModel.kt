package com.thekeeperofpie.artistalleydatabase.anime.user.favorite

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserFavoriteCharactersViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    mediaListStatusController: MediaListStatusController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val userId = savedStateHandle.get<String?>("userId")
    val viewer = aniListApi.authedUser
    val characters = MutableStateFlow(PagingData.empty<CharacterListRow.Entry>())

    private val refresh = MutableStateFlow(-1L)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                viewer,
                refresh,
                ::Pair,
            ).flatMapLatest { (viewer) ->
                val userId = userId ?: viewer?.id
                AniListPager {
                    val result =
                        aniListApi.userFavoritesCharacters(userId = userId!!, page = it)
                    result.user?.favourites?.characters?.pageInfo to
                            result.user?.favourites?.characters?.nodes?.filterNotNull()
                                .orEmpty()
                }
            }
                .mapLatest {
                    it.mapOnIO {
                        CharacterListRow.Entry(
                            character = it,
                            role = null,
                            media = it.media?.edges?.filterNotNull().orEmpty().distinctBy { it.id }
                                .mapNotNull { it.node }
                                .map { MediaWithListStatusEntry(it) },
                            favorites = it.favourites,
                            voiceActors = it.media?.edges?.filterNotNull()
                                ?.flatMap { it.voiceActors?.filterNotNull().orEmpty() }
                                ?.groupBy { it.languageV2 }
                                .orEmpty()
                        )
                    }
                }
                .enforceUniqueIntIds { it.character.id }
                .cachedIn(viewModelScope)
                .flatMapLatest {
                    combine(
                        mediaListStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.showIgnored,
                        settings.showAdult,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { statuses, _, showIgnored, showAdult, showLessImportantTags, showSpoilerTags ->
                        it.mapNotNull {
                            it.copy(media = it.media.mapNotNull {
                                applyMediaFiltering(
                                    statuses = statuses,
                                    ignoreController = ignoreController,
                                    showAdult = showAdult,
                                    showIgnored = showIgnored,
                                    showLessImportantTags = showLessImportantTags,
                                    showSpoilerTags = showSpoilerTags,
                                    entry = it,
                                    transform = { it },
                                    media = it.media,
                                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                                        copy(
                                            mediaListStatus = mediaListStatus,
                                            progress = progress,
                                            progressVolumes = progressVolumes,
                                            scoreRaw = scoreRaw,
                                            ignored = ignored,
                                            showLessImportantTags = showLessImportantTags,
                                            showSpoilerTags = showSpoilerTags,
                                        )
                                    },
                                )
                            })
                        }
                    }
                }
                .cachedIn(viewModelScope)
                .collectLatest(characters::emit)
        }
    }

    fun refresh() {
        refresh.value = SystemClock.uptimeMillis()
    }
}
