package com.thekeeperofpie.artistalleydatabase.anime.recommendations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import artistalleydatabase.modules.anime.recommendations.generated.resources.Res
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendations_header
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.type.RecommendationRating
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object RecommendationsScreen {

    @Composable
    operator fun <MediaEntry> invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        sortFilterState: SortFilterState<*>,
        viewer: () -> AniListViewer?,
        upIconOption: UpIconOption?,
        recommendations: LazyPagingItems<RecommendationEntry<MediaEntry>>,
        mediaRows: @Composable (
            media: MediaEntry?,
            mediaRecommendation: MediaEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        userRoute: UserRoute,
        onUserRecommendationRating: (
            recommendation: RecommendationData,
            newRating: RecommendationRating,
        ) -> Unit,
    ) {
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            val scrollBehavior =
                TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
            SortFilterBottomScaffold2(
                state = sortFilterState,
                topBar = {
                    EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                        TopAppBar(
                            title = { Text(text = stringResource(Res.string.anime_recommendations_header)) },
                            navigationIcon = { upIconOption?.let { UpIconButton(upIconOption) } },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    lerp(0.dp, 16.dp, scrollBehavior.state.overlappedFraction)
                                )
                            ),
                        )
                    }
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                VerticalList(
                    itemHeaderText = null,
                    items = recommendations,
                    itemKey = { it.id },
                    onRefresh = {},
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 72.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    item = {
                        SharedTransitionKeyScope("anime_recommendation_card", it?.id.orEmpty()) {
                            RecommendationCard(
                                viewer = viewer(),
                                user = it?.user,
                                media = it?.media,
                                recommendation = it?.data,
                                onUserRecommendationRating = onUserRecommendationRating,
                                mediaRows = {
                                    mediaRows(it?.media, it?.mediaRecommendation, onClickListEdit)
                                },
                                userRoute = userRoute,
                            )
                        }
                    },
                    modifier = Modifier.padding(it)
                )
            }
        }
    }
}
