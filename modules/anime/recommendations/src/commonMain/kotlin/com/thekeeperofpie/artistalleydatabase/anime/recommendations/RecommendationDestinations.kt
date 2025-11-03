package com.thekeeperofpie.artistalleydatabase.anime.recommendations

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.paging.compose.collectAsLazyPagingItems
import com.anilist.data.fragment.MediaCompactWithTags
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import kotlinx.serialization.Serializable

object RecommendationDestinations {

    @Serializable
    data object Recommendations : NavDestination

    fun <MediaEntry : Any> addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: RecommendationsComponent,
        mediaDetailsRoute: MediaDetailsRoute,
        userRoute: UserRoute,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        mediaRow: @Composable (
            MediaEntry?,
            viewer: AniListViewer?,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
        mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
    ) {
        navGraphBuilder.sharedElementComposable<Recommendations>(navigationTypeMap) {
            val recommendationsSortFilterViewModel = viewModel {
                component.recommendationsSortFilterViewModelFactory
                    .create(createSavedStateHandle(), mediaDetailsRoute)
            }
            val viewModel = viewModel {
                component.recommendationsViewModelFactoryFactory
                    .create(recommendationsSortFilterViewModel)
                    .create(mediaEntryProvider)
            }
            val viewer by viewModel.viewer.collectAsState()
            val navigationController = LocalNavigationController.current
            RecommendationsScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                sortFilterState = recommendationsSortFilterViewModel.state,
                viewer = { viewer },
                upIconOption = UpIconOption.Back(navigationController),
                recommendations = viewModel.recommendations.collectAsLazyPagingItems(),
                mediaRows = { media, mediaRecommendation, onClickListEdit ->
                    SharedTransitionKeyScope(
                        "recommendation",
                        media?.let(mediaEntryProvider::mediaFilterable)?.mediaId,
                        mediaRecommendation?.let(mediaEntryProvider::mediaFilterable)?.mediaId,
                    ) {
                        mediaRow(
                            media,
                            viewer,
                            onClickListEdit,
                            Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                        )
                        mediaRow(
                            mediaRecommendation,
                            viewer,
                            onClickListEdit,
                            Modifier.padding(horizontal = 8.dp)
                        )
                    }
                },
                userRoute = userRoute,
                onUserRecommendationRating = viewModel.recommendationToggleHelper::toggle,
            )
        }
    }
}
