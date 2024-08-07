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
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioListRow
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import com.thekeeperofpie.artistalleydatabase.compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.compose.navigation.toDestination
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
class UserFavoriteStudiosViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    mediaListStatusController: MediaListStatusController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {

    private val destination = savedStateHandle.toDestination<AnimeDestination.UserFavoriteStudios>(navigationTypeMap)
    val userId = destination.userId
    val viewer = aniListApi.authedUser
    val studios = MutableStateFlow(PagingData.empty<StudioListRow.Entry>())

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
                    val result = aniListApi.userFavoritesStudios(userId = userId!!, page = it)
                    result.user?.favourites?.studios?.pageInfo to
                            result.user?.favourites?.studios?.nodes?.filterNotNull()
                                .orEmpty()
                }
            }
                .mapLatest {
                    it.mapOnIO {
                        StudioListRow.Entry(
                            studio = it,
                            media = (it.main?.nodes?.filterNotNull().orEmpty()
                                .map(::MediaWithListStatusEntry) +
                                    it.nonMain?.nodes?.filterNotNull().orEmpty()
                                        .map(::MediaWithListStatusEntry))
                                .distinctBy { it.media.id }
                        )
                    }
                }
                .enforceUniqueIntIds { it.studio.id }
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
                .collectLatest(studios::emit)
        }
    }

    fun refresh() {
        refresh.value = SystemClock.uptimeMillis()
    }
}
