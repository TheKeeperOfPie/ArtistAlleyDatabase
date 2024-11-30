package com.thekeeperofpie.artistalleydatabase.anime.activity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityDestinations
import com.thekeeperofpie.artistalleydatabase.anime.activities.UserRoute
import com.thekeeperofpie.artistalleydatabase.anime.activity.details.ActivityDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavHostController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable

object ActivityDestinations {

    fun addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: AnimeActivitiesComponent,
        userRoute: UserRoute,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        mediaRow: @Composable (
            AnimeMediaCompactListRow.Entry?,
            viewer: AniListViewer?,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
    ) {
        navGraphBuilder.sharedElementComposable<ActivityDestinations.ActivityDetails>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/activity/{activityId}"
                },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/activity/{activityId}/.*"
                },
            ),
        ) {
            val destination = it.toRoute<ActivityDestinations.ActivityDetails>()
            // TODO: Shared element doesn't actually work
            SharedTransitionKeyScope(destination.sharedTransitionScopeKey) {
                val navHostController = LocalNavHostController.current
                val viewModel = viewModel {
                    component.activityDetailsViewModel(createSavedStateHandle())
                }
                val viewer by viewModel.viewer.collectAsState()
                ActivityDetailsScreen(
                    component = component,
                    upIconOption = UpIconOption.Back(navHostController),
                    userRoute = userRoute,
                    mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                    mediaRow = { entry, onClickListEdit, modifier ->
                        mediaRow(entry, viewer, onClickListEdit, modifier)
                    },
                )
            }
        }
    }
}
