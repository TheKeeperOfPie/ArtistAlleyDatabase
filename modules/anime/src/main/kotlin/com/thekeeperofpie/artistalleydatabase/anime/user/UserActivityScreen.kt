package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityList
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold

@OptIn(ExperimentalMaterial3Api::class)
object UserActivityScreen {

    @Composable
    operator fun invoke(
        viewModel: AniListUserViewModel,
        editViewModel: MediaEditViewModel,
        viewer: AniListViewer?,
    ) {
        val sortFilterController = viewModel.activitySortFilterController
        SortFilterBottomScaffold(sortFilterController = sortFilterController) {
            ActivityList(
                editViewModel = editViewModel,
                viewer = viewer,
                activities = viewModel.activities.collectAsLazyPagingItems(),
                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                showMedia = true,
                allowUserClick = false,
                sortFilterController = sortFilterController,
            )
        }
    }
}
