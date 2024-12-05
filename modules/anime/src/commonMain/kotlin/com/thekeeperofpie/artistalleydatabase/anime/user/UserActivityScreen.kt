package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityList
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems

@OptIn(ExperimentalMaterial3Api::class)
object UserActivityScreen {

    @Composable
    operator fun invoke(
        viewModel: AniListUserViewModel,
        editViewModel: MediaEditViewModel,
        viewer: AniListViewer?,
        userRoute: UserRoute,
    ) {
        val sortFilterController = viewModel.activitySortFilterController
        SortFilterBottomScaffold(sortFilterController = sortFilterController) {
            ActivityList(
                viewer = viewer,
                activities = viewModel.activities.collectAsLazyPagingItems(),
                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                showMedia = true,
                allowUserClick = false,
                sortFilterState = sortFilterController::state,
                userRoute = userRoute,
                onClickListEdit = editViewModel::initialize,
                mediaRow = { entry, onClickListEdit, modifier ->
                    AnimeMediaCompactListRow(
                        viewer = viewer,
                        entry = entry,
                        onClickListEdit = onClickListEdit,
                        modifier = modifier,
                    )
                },
            )
        }
    }
}
