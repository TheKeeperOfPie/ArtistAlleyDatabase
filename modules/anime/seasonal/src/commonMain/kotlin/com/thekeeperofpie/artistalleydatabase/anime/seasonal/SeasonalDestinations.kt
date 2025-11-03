package com.thekeeperofpie.artistalleydatabase.anime.seasonal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.paging.PagingData
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaPreview
import com.anilist.data.fragment.MediaPreviewWithDescription
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

object SeasonalDestinations {

    @Serializable
    data class Seasonal(val type: Type) : NavDestination {
        @Serializable
        enum class Type {
            LAST,
            THIS,
            NEXT,
        }
    }

    fun <SortFilterViewModel, MediaEntry : Any> addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: SeasonalComponent,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        sortFilterViewModelProvider: @Composable () -> SortFilterViewModel,
        sortFilterState: (SortFilterViewModel) -> SortFilterState<*>,
        filterParams: (SortFilterViewModel) -> Flow<MediaSearchFilterParams<MediaSortOption>>,
        filterMedia: (
            SortFilterViewModel,
            PagingData<MediaEntry>,
            (MediaEntry) -> MediaPreview,
        ) -> Flow<PagingData<MediaEntry>>,
        mediaEntryProvider: MediaEntryProvider<MediaPreviewWithDescription, MediaEntry>,
        mediaViewOptionRow: @Composable (
            AniListViewer?,
            MediaViewOption,
            MediaEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
    ) {
        navGraphBuilder.sharedElementComposable<Seasonal>(navigationTypeMap) {
            val sortFilterViewModel = sortFilterViewModelProvider()
            val viewModel = viewModel {
                component.seasonalViewModelFactoryFactory.create(createSavedStateHandle())
                    .create(
                        mediaEntryProvider = mediaEntryProvider,
                        filterParams = filterParams(sortFilterViewModel),
                        filterMedia = { result, transform ->
                            filterMedia(sortFilterViewModel, result, transform)
                        },
                    )
            }
            val viewer by viewModel.viewer.collectAsStateWithLifecycle()
            SeasonalScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                initialPage = viewModel.initialPage,
                onRefresh = viewModel::onRefresh,
                sortFilterState = sortFilterState(sortFilterViewModel),
                itemsForPage = { viewModel.items(it) },
                itemKey = mediaEntryProvider::id,
                mediaViewOption = { viewModel.mediaViewOption },
                onMediaViewOptionChange = { viewModel.mediaViewOption = it },
                mediaViewOptionRow = { entry, onClickListEdit ->
                    mediaViewOptionRow(viewer, viewModel.mediaViewOption, entry, onClickListEdit)
                },
            )
        }
    }
}
