package com.thekeeperofpie.artistalleydatabase.anime.studio

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.anilist.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class StudioMediasViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    favoritesController: FavoritesController,
    featureOverrideProvider: FeatureOverrideProvider,
    savedStateHandle: SavedStateHandle,
) : HeaderAndListViewModel<StudioMediasScreen.Entry, MediaPreview, MediaPreviewEntry, MediaSortOption, StudioMediaSortFilterController.FilterParams>(
    aniListApi = aniListApi,
    loadingErrorTextRes = R.string.anime_studio_medias_error_loading,
) {
    val studioId = savedStateHandle.get<String>("studioId")!!

    override val sortFilterController =
        StudioMediaSortFilterController(viewModelScope, settings, featureOverrideProvider)

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

    override fun makeEntry(item: MediaPreview) = MediaPreviewEntry(item)

    override fun entryId(entry: MediaPreviewEntry) = entry.media.id.toString()

    override suspend fun initialRequest(
        filterParams: StudioMediaSortFilterController.FilterParams?,
    ) = StudioMediasScreen.Entry(aniListApi.studioMedias(studioId = studioId))

    override suspend fun pagedRequest(
        page: Int,
        filterParams: StudioMediaSortFilterController.FilterParams?,
    ) = aniListApi.studioMediasPage(
        studioId = studioId,
        sort = filterParams!!.sort.selectedOption(MediaSortOption.TRENDING)
            .toApiValue(filterParams.sortAscending),
        main = filterParams.main,
        page = page,
    ).studio.media.run { pageInfo to nodes }

    override fun Flow<PagingData<MediaPreviewEntry>>.transformFlow() =
        applyMediaStatusChanges(
            statusController = statusController,
            ignoreController = ignoreController,
            settings = settings,
        )
}
