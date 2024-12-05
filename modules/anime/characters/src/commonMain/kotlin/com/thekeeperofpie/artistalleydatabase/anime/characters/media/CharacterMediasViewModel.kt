package com.thekeeperofpie.artistalleydatabase.anime.characters.media

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import artistalleydatabase.modules.anime.characters.generated.resources.Res
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_medias_error_loading
import com.anilist.data.fragment.MediaPreview
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
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilteredViewModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class CharacterMediasViewModel<MediaEntry : Any>(
    private val aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    private val settings: MediaDataSettings,
    favoritesController: FavoritesController,
    featureOverrideProvider: FeatureOverrideProvider,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted private val mediaEntryProvider: MediaEntryProvider<MediaPreview, MediaEntry>,
) : SortFilteredViewModel<CharacterMediasScreen.Entry, MediaPreview, MediaEntry, CharacterMediaSortFilterController.FilterParams>(
    loadingErrorTextRes = Res.string.anime_character_medias_error_loading,
) {
    private val destination =
        savedStateHandle.toDestination<CharacterDestinations.CharacterMedias>(navigationTypeMap)
    val characterId = destination.characterId
    val viewer = aniListApi.authedUser
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    override val sortFilterController =
        CharacterMediaSortFilterController(viewModelScope, settings, featureOverrideProvider)

    init {
        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entry = { snapshotFlow { entry.result } },
            entryToId = { it.character.id.toString() },
            entryToType = { FavoriteType.CHARACTER },
            entryToFavorite = { it.character.isFavourite },
        )
    }

    override fun makeEntry(item: MediaPreview) = mediaEntryProvider.mediaEntry(item)

    override fun entryId(entry: MediaEntry) = mediaEntryProvider.mediaFilterable(entry).mediaId

    override suspend fun initialRequest(
        filterParams: CharacterMediaSortFilterController.FilterParams?,
    ) = CharacterMediasScreen.Entry(
        aniListApi.characterAndMedias(characterId = characterId)
    )

    override suspend fun request(filterParams: CharacterMediaSortFilterController.FilterParams?): Flow<PagingData<MediaPreview>> =
        AniListPager { page ->
            aniListApi.characterAndMediasPage(
                characterId = characterId,
                sort = filterParams!!.sort.selectedOption(MediaSortOption.TRENDING)
                    .toApiValue(filterParams.sortAscending),
                onList = filterParams.onList,
                page = page,
            ).character.media.run { pageInfo to nodes }
        }

    override fun Flow<PagingData<MediaEntry>>.transformFlow() =
        applyMediaStatusChanges(
            statusController = statusController,
            ignoreController = ignoreController,
            mediaFilteringData = settings.mediaFilteringData(false),
            mediaFilterable = { mediaEntryProvider.mediaFilterable(it) },
            copy = { mediaEntryProvider.copyMediaEntry(this, it) },
        )

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val statusController: MediaListStatusController,
        private val ignoreController: IgnoreController,
        private val settings: MediaDataSettings,
        private val favoritesController: FavoritesController,
        private val featureOverrideProvider: FeatureOverrideProvider,
        private val navigationTypeMap: NavigationTypeMap,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) {

        fun <MediaEntry : Any> create(
            mediaEntryProvider: MediaEntryProvider<MediaPreview, MediaEntry>,
        ) = CharacterMediasViewModel<MediaEntry>(
            aniListApi = aniListApi,
            statusController = statusController,
            ignoreController = ignoreController,
            settings = settings,
            favoritesController = favoritesController,
            featureOverrideProvider = featureOverrideProvider,
            navigationTypeMap = navigationTypeMap,
            savedStateHandle = savedStateHandle,
            mediaEntryProvider = mediaEntryProvider,
        )
    }
}
