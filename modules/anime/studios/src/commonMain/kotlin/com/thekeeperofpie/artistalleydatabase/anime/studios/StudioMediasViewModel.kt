package com.thekeeperofpie.artistalleydatabase.anime.studios

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import artistalleydatabase.modules.anime.studios.generated.resources.Res
import artistalleydatabase.modules.anime.studios.generated.resources.anime_studio_medias_error_loading
import com.anilist.data.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
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
class StudioMediasViewModel<MediaEntry : Any>(
    private val aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    private val settings: MediaDataSettings,
    favoritesController: FavoritesController,
    featureOverrideProvider: FeatureOverrideProvider,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted private val mediaEntryProvider: MediaEntryProvider<MediaPreview, MediaEntry>,
) : SortFilteredViewModel<StudioMediasScreen.Entry, MediaPreview, MediaEntry, StudioMediaSortFilterController.FilterParams>(
    loadingErrorTextRes = Res.string.anime_studio_medias_error_loading,
) {
    val studioId =
        savedStateHandle.toDestination<StudioDestinations.StudioMedias>(navigationTypeMap).studioId

    val sortFilterController =
        StudioMediaSortFilterController(viewModelScope, settings, featureOverrideProvider)

    override val filterParams = sortFilterController.filterParams

    val viewer = aniListApi.authedUser
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    init {
        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entry = { snapshotFlow { entry.result } },
            entryToId = { it.studio.id.toString() },
            entryToType = { FavoriteType.STUDIO },
            entryToFavorite = { it.studio.isFavourite },
        )
    }

    override fun makeEntry(item: MediaPreview) = mediaEntryProvider.mediaEntry(item)

    override fun entryId(entry: MediaEntry) = mediaEntryProvider.id(entry)

    override suspend fun initialRequest(
        filterParams: StudioMediaSortFilterController.FilterParams?,
    ) = StudioMediasScreen.Entry(aniListApi.studioMedias(studioId = studioId))

    override suspend fun request(
        filterParams: StudioMediaSortFilterController.FilterParams?,
    ): Flow<PagingData<MediaPreview>> = AniListPager { page ->
        aniListApi.studioMediasPage(
            studioId = studioId,
            sort = filterParams!!.sort.selectedOption(MediaSortOption.TRENDING)
                .toApiValue(filterParams.sortAscending),
            main = filterParams.main,
            page = page,
        ).studio.media.run { pageInfo to nodes }
    }

    override fun Flow<PagingData<MediaEntry>>.transformFlow() =
        applyMediaStatusChanges(
            statusController = statusController,
            ignoreController = ignoreController,
            mediaFilteringData = settings.mediaFilteringData(false),
            mediaFilterable = mediaEntryProvider::mediaFilterable,
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
        ) = StudioMediasViewModel(
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
