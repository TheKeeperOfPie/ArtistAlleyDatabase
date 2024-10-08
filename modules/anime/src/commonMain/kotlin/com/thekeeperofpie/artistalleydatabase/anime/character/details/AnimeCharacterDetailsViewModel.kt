package com.thekeeperofpie.artistalleydatabase.anime.character.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.flatMap
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_character_error_loading
import com.anilist.type.CharacterRole
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.staff.DetailsStaff
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AnimeCharacterDetailsViewModel(
    private val aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
    favoritesController: FavoritesController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    private val markdown: Markdown,
    @Assisted savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<AnimeDestination.CharacterDetails>(navigationTypeMap)
    val characterId = destination.characterId

    val viewer = aniListApi.authedUser

    var entry by mutableStateOf<LoadingResult<CharacterDetailsScreen.Entry>>(LoadingResult.loading())

    val refresh = RefreshFlow()

    val voiceActorsDeferred = MutableStateFlow(PagingData.empty<DetailsStaff>())

    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            refresh.updates
                .mapLatest { aniListApi.characterDetails(characterId, skipCache = it.fromUser) }
                .flatMapLatest { result ->
                    val media = result.result?.character?.media?.edges
                        ?.distinctBy { it?.node?.id }
                        ?.mapNotNull {
                            it?.node?.let { node ->
                                MediaEntry(
                                    mediaPreviewEntry = MediaPreviewEntry(node),
                                    characterRole = it.characterRole
                                )
                            }
                        }
                        .orEmpty()
                    combine(
                        statusController
                            .allChanges(media.map { it.mediaPreviewEntry.media.id.toString() }
                                .toSet()),
                        ignoreController.updates(),
                        settings.mediaFilteringData(forceShowIgnored = true),
                    ) { statuses, _, filteringData ->
                        result.transformResult { character ->
                            CharacterDetailsScreen.Entry(
                                character = character.character!!,
                                media = media.mapNotNull {
                                    it.copy(
                                        mediaPreviewEntry = applyMediaFiltering(
                                            statuses = statuses,
                                            ignoreController = ignoreController,
                                            filteringData = filteringData,
                                            entry = it.mediaPreviewEntry,
                                            filterableData = it.mediaPreviewEntry.mediaFilterable,
                                            copy = { copy(mediaFilterable = it) },
                                        ) ?: return@mapNotNull null
                                    )
                                },
                                description = character.character?.description
                                    ?.let(markdown::convertMarkdownText),
                            )
                        }
                    }
                }
                .catch { emit(LoadingResult.error(Res.string.anime_character_error_loading)) }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry = it }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { entry.result?.character }
                .filterNotNull()
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { character ->
                    // TODO: Possible to hook up LocalVoiceActorLanguageOption?
                    AniListPager {
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
                }
                .map {
                    it.flatMap {
                        it.voiceActorRoles?.filterNotNull()
                            ?.mapNotNull { it.voiceActor }
                            ?.map {
                                DetailsStaff(
                                    id = it.id.toString(),
                                    name = it.name,
                                    image = it.image?.large,
                                    role = it.languageV2,
                                    staff = it,
                                )
                            }
                            .orEmpty()
                            .distinctBy { it.idWithRole }
                    }
                }
                .enforceUniqueIds { it.idWithRole }
                .cachedIn(viewModelScope)
                .collectLatest(voiceActorsDeferred::emit)
        }

        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entry = { snapshotFlow { entry.result } },
            entryToId = { it.character.id.toString() },
            entryToType = { FavoriteType.CHARACTER },
            entryToFavorite = { it.character.isFavourite },
        )
    }

    fun refresh() = refresh.refresh()

    data class MediaEntry(
        val mediaPreviewEntry: MediaPreviewEntry,
        val characterRole: CharacterRole?,
    )
}
