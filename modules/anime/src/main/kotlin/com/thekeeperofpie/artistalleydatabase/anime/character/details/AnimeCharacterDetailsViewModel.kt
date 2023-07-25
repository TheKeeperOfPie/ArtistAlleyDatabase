package com.thekeeperofpie.artistalleydatabase.anime.character.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.flatMap
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.staff.DetailsStaff
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnimeCharacterDetailsViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
    favoritesController: FavoritesController,
    private val ignoreList: AnimeMediaIgnoreList,
    private val settings: AnimeSettings,
) : ViewModel() {

    val viewer = aniListApi.authedUser

    lateinit var characterId: String

    var entry by mutableStateOf<CharacterDetailsScreen.Entry?>(null)
    var loading by mutableStateOf(true)
    var errorResource by mutableStateOf<Pair<Int, Throwable?>?>(null)
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    val voiceActors = MutableStateFlow(PagingData.empty<DetailsStaff>())

    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    fun initialize(characterId: String) {
        if (::characterId.isInitialized) return
        this.characterId = characterId

        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                val character = aniListApi.characterDetails(characterId)
                val media = character.media?.edges
                    ?.distinctBy { it?.node?.id }
                    ?.mapNotNull { it?.node?.let(AnimeMediaListRow::Entry) }
                    .orEmpty()

                combine(
                    statusController.allChanges(media.map { it.media.id.toString() }.toSet()),
                    ignoreList.updates,
                    settings.showAdult,
                ) { statuses, ignoredIds, showAdult ->
                    CharacterDetailsScreen.Entry(
                        character,
                        media = media.mapNotNull {
                            applyMediaFiltering(
                                statuses = statuses,
                                ignoredIds = ignoredIds,
                                showAdult = showAdult,
                                showIgnored = true,
                                entry = it,
                                transform = { it },
                                media = it.media,
                                copy = { mediaListStatus, progress, progressVolumes, ignored ->
                                    AnimeMediaListRow.Entry(
                                        media = this.media,
                                        mediaListStatus = mediaListStatus,
                                        progress = progress,
                                        progressVolumes = progressVolumes,
                                        ignored = ignored,
                                    )
                                }
                            )
                        },
                    )
                }
                    .collectLatest {
                        withContext(CustomDispatchers.Main) {
                            this@AnimeCharacterDetailsViewModel.entry = it
                        }
                    }
            } catch (exception: Exception) {
                withContext(CustomDispatchers.Main) {
                    errorResource = R.string.anime_character_error_loading to exception
                }
            } finally {
                withContext(CustomDispatchers.Main) {
                    loading = false
                }
            }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { entry?.character }
                .filterNotNull()
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { character ->
                    Pager(config = PagingConfig(10)) {
                        AniListPagingSource {
                            if (it == 1) {
                                character.media?.pageInfo to character.media?.edges?.filterNotNull()
                                    .orEmpty()
                            } else {
                                val result = aniListApi.characterDetailsMediaPage(
                                    character.id.toString(),
                                    it
                                ).media
                                result.pageInfo to result.edges.filterNotNull()
                            }
                        }
                    }.flow
                }
                .map {
                    it.flatMap {
                        it.voiceActorRoles?.filterNotNull()
                            ?.mapNotNull { it.voiceActor }
                            ?.map {
                                DetailsStaff(
                                    id = it.id.toString(),
                                    name = it.name?.userPreferred,
                                    image = it.image?.large,
                                    role = it.languageV2,
                                    staff = it,
                                )
                            }
                            .orEmpty()
                    }
                }
                .enforceUniqueIds { it.id }
                .cachedIn(viewModelScope)
                .collectLatest(voiceActors::emit)
        }

        favoritesToggleHelper.initializeTracking(
            viewModel = this,
            entry = { snapshotFlow { entry } },
            entryToId = { it.character.id.toString() },
            entryToType = { FavoriteType.CHARACTER },
            entryToFavorite = { it.character.isFavourite },
        )
    }

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry<*>) =
        ignoreList.toggle(entry.media.id.toString())
}
