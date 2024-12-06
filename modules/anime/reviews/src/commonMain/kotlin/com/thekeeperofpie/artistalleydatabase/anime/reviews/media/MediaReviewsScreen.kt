package com.thekeeperofpie.artistalleydatabase.anime.reviews.media

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.reviews.generated.resources.Res
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_reviews_header
import com.anilist.data.MediaAndReviewsQuery
import com.anilist.data.fragment.MediaAndReviewsReview
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.reviews.ReviewDestinations
import com.thekeeperofpie.artistalleydatabase.anime.reviews.ReviewSmallCard
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavHostController
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems

@OptIn(ExperimentalMaterial3Api::class)
object MediaReviewsScreen {

    @Composable
    operator fun invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        coverImageState: CoilImageState,
        userRoute: UserRoute,
        onRefresh: () -> Unit,
        entry: LoadingResult<Entry>,
        items: LazyPagingItems<MediaAndReviewsReview>,
        sortFilterState: () -> SortFilterController<*>.State,
        mediaHeader: @Composable (progress: Float) -> Unit,
        favorite: () -> Boolean?,
    ) {
        val media = entry.result?.media
        val mediaTitle = media?.title?.primaryTitle()
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
            )
            SortFilterBottomScaffold(
                state = sortFilterState,
                topBar = {
                    CollapsingToolbar(
                        maxHeight = 356.dp,
                        pinnedHeight = 120.dp,
                        scrollBehavior = scrollBehavior,
                    ) {
                        mediaHeader(it)
                    }
                },
                modifier = Modifier.Companion.padding(padding)
            ) {
                val gridState = rememberLazyGridState()
                sortFilterState().ImmediateScrollResetEffect(gridState)
                VerticalList(
                    gridState = gridState,
                    onRefresh = onRefresh,
                    itemHeaderText = Res.string.anime_reviews_header,
                    itemKey = { it.id },
                    items = items,
                    item = {
                        val navHostController = LocalNavHostController.current
                        ReviewSmallCard(
                            review = it,
                            userRoute = userRoute,
                            onClick = {
                                if (it != null) {
                                    navHostController.navigate(
                                        ReviewDestinations.ReviewDetails(
                                            reviewId = it.id.toString(),
                                            headerParams = MediaHeaderParams(
                                                title = mediaTitle,
                                                coverImage = coverImageState.toImageState(),
                                                media = media,
                                                favorite = favorite() ?: media?.isFavourite,
                                            )
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.Companion.padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp
                            )
                        )
                    },
                    nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                    modifier = Modifier.Companion.padding(it)
                )
            }
        }
    }

    data class Entry(
        val media: MediaAndReviewsQuery.Data.Media,
    ) {
        val titlesUnique = media.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()
    }
}
