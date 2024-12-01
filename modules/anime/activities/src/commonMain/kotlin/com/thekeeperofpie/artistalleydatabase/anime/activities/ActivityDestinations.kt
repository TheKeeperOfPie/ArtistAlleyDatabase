package com.thekeeperofpie.artistalleydatabase.anime.activities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.anilist.data.fragment.MediaCompactWithTags
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.activities.details.ActivityDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.primaryTitle
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavHostController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import kotlinx.serialization.Serializable

object ActivityDestinations {

    @Serializable
    data object Activity : NavDestination

    @Serializable
    data class ActivityDetails(
        val activityId: String,
        val sharedTransitionScopeKey: String?,
    ) : NavDestination

    fun <MediaEntry> addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: AnimeActivitiesComponent,
        userRoute: UserRoute,
        mediaDetailsRoute: MediaDetailsRoute,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        mediaRow: @Composable (
            MediaEntry?,
            viewer: AniListViewer?,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
        mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
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
                    component.activityDetailsViewModelFactory(createSavedStateHandle())
                        .create(mediaEntryProvider)
                }
                val viewer by viewModel.viewer.collectAsState()
                ActivityDetailsScreen(
                    upIconOption = UpIconOption.Back(navHostController),
                    viewer = { viewer },
                    refresh = viewModel.refresh.updates,
                    state = viewModel.state,
                    eventSink = viewModel::onEvent,
                    userRoute = userRoute,
                    mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                    mediaRow = { entry, onClickListEdit, modifier ->
                        mediaRow(entry, viewer, onClickListEdit, modifier)
                    },
                )
            }
        }

        navGraphBuilder.sharedElementComposable<Activity>(navigationTypeMap) {
            val viewModel = viewModel {
                component.animeActivityViewModelFactory()
                    .create(mediaEntryProvider, mediaDetailsRoute)
            }
            val viewer by viewModel.viewer.collectAsState()
            AnimeActivityScreen(
                viewer = { viewer },
                userRoute = userRoute,
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                sortFilterState = { viewModel.sortFilterController.state },
                mediaTitle = {
                    viewModel.sortFilterController.selectedMedia()?.title?.primaryTitle()
                },
                ownActivity = { viewModel.ownActivity().collectAsLazyPagingItems() },
                globalActivity = { viewModel.globalActivity().collectAsLazyPagingItems() },
                followingActivity = { viewModel.followingActivity().collectAsLazyPagingItems() },
                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                mediaRow = { entry, onClickListEdit, modifier ->
                    mediaRow(entry, viewer, onClickListEdit, modifier)
                },
            )
        }
    }
}