package com.thekeeperofpie.artistalleydatabase.anime.characters.details

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
import artistalleydatabase.modules.anime.characters.generated.resources.Res
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_error_loading
import com.anilist.data.fragment.MediaPreview
import com.anilist.data.type.CharacterRole
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterDestinations
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffDetails
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Inject
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

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AnimeCharacterDetailsViewModel<MediaEntry>(
    private val aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
    favoritesController: FavoritesController,
    private val ignoreController: IgnoreController,
    private val settings: MediaDataSettings,
    private val markdown: Markdown,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted private val mediaEntryProvider: MediaEntryProvider<MediaPreview, MediaEntry>
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<CharacterDestinations.CharacterDetails>(navigationTypeMap)
    val characterId = destination.characterId

    val viewer = aniListApi.authedUser

    var entry by mutableStateOf<LoadingResult<CharacterDetailsScreen.Entry<MediaEntry>>>(LoadingResult.loading())

    val refresh = RefreshFlow()

    val voiceActorsDeferred = MutableStateFlow(PagingData.empty<StaffDetails>())

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
                                val mediaEntry = mediaEntryProvider.mediaEntry(node)
                                Entry(
                                    mediaId = mediaEntryProvider.mediaFilterable(mediaEntry)
                                        .mediaId,
                                    mediaEntry = mediaEntry,
                                    characterRole = it.characterRole
                                )
                            }
                        }
                        .orEmpty()
                    combine(
                        statusController.allChanges(
                            media.map { mediaEntryProvider.id(it.mediaEntry) }.toSet()
                        ),
                        ignoreController.updates(),
                        settings.mediaFilteringData(forceShowIgnored = true),
                    ) { statuses, _, filteringData ->
                        result.transformResult { character ->
                            CharacterDetailsScreen.Entry(
                                character = character.character!!,
                                media = media.mapNotNull {
                                    it.copy(
                                        mediaEntry = applyMediaFiltering(
                                            statuses = statuses,
                                            ignoreController = ignoreController,
                                            filteringData = filteringData,
                                            entry = it.mediaEntry,
                                            filterableData = mediaEntryProvider.mediaFilterable(it.mediaEntry),
                                            copy = { mediaEntryProvider.copyMediaEntry(this, it) },
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
                                StaffDetails(
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

    data class Entry<MediaEntry>(
        val mediaId: String,
        val mediaEntry: MediaEntry,
        val characterRole: CharacterRole?,
    )

    @AssistedInject
    class TypedFactory(
        private val aniListApi: AuthedAniListApi,
        private val statusController: MediaListStatusController,
        private val favoritesController: FavoritesController,
        private val ignoreController: IgnoreController,
        private val settings: MediaDataSettings,
        private val markdown: Markdown,
        private val navigationTypeMap: NavigationTypeMap,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) {
        fun <MediaEntry> create(mediaEntryProvider: MediaEntryProvider<MediaPreview, MediaEntry>) =
            AnimeCharacterDetailsViewModel(
                aniListApi = aniListApi,
                statusController = statusController,
                favoritesController = favoritesController,
                ignoreController = ignoreController,
                settings = settings,
                markdown = markdown,
                navigationTypeMap = navigationTypeMap,
                savedStateHandle = savedStateHandle,
                mediaEntryProvider = mediaEntryProvider,
            )

        @AssistedFactory
        interface Factory {
            fun create(savedStateHandle: SavedStateHandle): TypedFactory
        }
    }
}
