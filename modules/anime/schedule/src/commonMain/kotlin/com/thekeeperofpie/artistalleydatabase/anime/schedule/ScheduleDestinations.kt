package com.thekeeperofpie.artistalleydatabase.anime.schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.schedule.AiringScheduleScreen.Entry
import com.thekeeperofpie.artistalleydatabase.anime.ui.SeasonalCurrentRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import kotlinx.serialization.Serializable

object ScheduleDestinations {

    @Serializable
    data object AiringSchedule : NavDestination

    fun <MediaEntry : Any> addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: ScheduleComponent,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        mediaEntryProvider: MediaEntryProvider<MediaPreview, MediaEntry>,
        mediaRow: @Composable (
            AniListViewer?,
            Entry<MediaEntry>?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        seasonalCurrentRoute: SeasonalCurrentRoute,
    ) {
        navGraphBuilder.sharedElementComposable<AiringSchedule>(navigationTypeMap) {
            val viewModel =
                viewModel { component.airingScheduleViewModelFactory().create(mediaEntryProvider) }
            val viewer by viewModel.viewer.collectAsState()
            AiringScheduleScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                sortFilterState = viewModel.sortFilterController::state,
                onRefresh = viewModel::refresh,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                itemsForPage = { viewModel.items(it) },
                mediaRow = { entry, onClickListEdit -> mediaRow(viewer, entry, onClickListEdit) },
                seasonalCurrentRoute = seasonalCurrentRoute,
            )
        }
    }
}
