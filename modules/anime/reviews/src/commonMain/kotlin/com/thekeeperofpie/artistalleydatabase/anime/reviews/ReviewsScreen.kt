package com.thekeeperofpie.artistalleydatabase.anime.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import artistalleydatabase.modules.anime.reviews.generated.resources.Res
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_reviews_header
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_reviews_header_with_media
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_reviews_tab_anime
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_reviews_tab_manga
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object ReviewsScreen {

    @Composable
    operator fun <MediaEntry> invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        upIconOption: UpIconOption,
        sortFilterStateAnime: () -> SortFilterController<*>.State,
        sortFilterStateManga: () -> SortFilterController<*>.State,
        selectedMediaAnime: () -> MediaNavigationData?,
        selectedMediaManga: () -> MediaNavigationData?,
        preferredMediaType: MediaType,
        anime: MutableStateFlow<PagingData<ReviewEntry<MediaEntry>>>,
        manga: MutableStateFlow<PagingData<ReviewEntry<MediaEntry>>>,
        userRoute: UserRoute,
        mediaTitle: @Composable (MediaEntry) -> String?,
        mediaHeaderParams: (MediaEntry, title: String?, ImageState) -> MediaHeaderParams,
        mediaImageUri: (MediaEntry?) -> String?,
        mediaRow: @Composable (
            MediaEntry?,
            coverImageState: CoilImageState,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
    ) {
        var selectedIsAnime by rememberSaveable {
            mutableStateOf(preferredMediaType == MediaType.ANIME)
        }
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            val scrollBehavior =
                TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
            SortFilterBottomScaffold(
                state = if (selectedIsAnime) sortFilterStateAnime else sortFilterStateManga,
                topBar = {
                    TopBar(
                        upIconOption,
                        scrollBehavior,
                        selectedMediaAnime,
                        selectedMediaManga,
                        selectedIsAnime = { selectedIsAnime },
                        onChangeSelectedIsAnime = { selectedIsAnime = it },
                    )
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                val anime = anime.collectAsLazyPagingItems()
                val manga = manga.collectAsLazyPagingItems()
                val reviews = if (selectedIsAnime) anime else manga
                val selectedMedia = if (selectedIsAnime) {
                    selectedMediaAnime()
                } else {
                    selectedMediaManga()
                }

                Content(
                    scaffoldPadding = it,
                    reviews = reviews,
                    showMedia = selectedMedia == null,
                    sortFilterState = {
                        if (selectedIsAnime) {
                            sortFilterStateAnime()
                        } else {
                            sortFilterStateManga()
                        }
                    },
                    userRoute = userRoute,
                    mediaTitle = mediaTitle,
                    mediaHeaderParams = mediaHeaderParams,
                    mediaImageUri = mediaImageUri,
                    mediaRow = { entry, coverImageState, modifier ->
                        mediaRow(entry, coverImageState, onClickListEdit, modifier)
                    },
                )
            }
        }
    }

    @Composable
    private fun TopBar(
        upIconOption: UpIconOption?,
        scrollBehavior: TopAppBarScrollBehavior,
        selectedMediaAnime: () -> MediaNavigationData?,
        selectedMediaManga: () -> MediaNavigationData?,
        selectedIsAnime: () -> Boolean,
        onChangeSelectedIsAnime: (Boolean) -> Unit,
    ) {
        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
            Column {
                val mediaTitle = if (selectedIsAnime()) {
                    selectedMediaAnime()
                } else {
                    selectedMediaManga()
                }?.title?.primaryTitle()

                AppBar(
                    text = if (mediaTitle == null) {
                        stringResource(Res.string.anime_reviews_header)
                    } else {
                        stringResource(Res.string.anime_reviews_header_with_media, mediaTitle)
                    },
                    upIconOption = upIconOption,
                )

                val selectedIsAnime = selectedIsAnime()
                TabRow(
                    selectedTabIndex = if (selectedIsAnime) 0 else 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Tab(
                        selected = selectedIsAnime,
                        onClick = { onChangeSelectedIsAnime(true) },
                        text = { Text(stringResource(Res.string.anime_reviews_tab_anime)) },
                    )
                    Tab(
                        selected = !selectedIsAnime,
                        onClick = { onChangeSelectedIsAnime(false) },
                        text = { Text(stringResource(Res.string.anime_reviews_tab_manga)) },
                    )
                }
            }
        }
    }

    @Composable
    private fun <MediaEntry> Content(
        scaffoldPadding: PaddingValues,
        reviews: LazyPagingItems<ReviewEntry<MediaEntry>>,
        showMedia: Boolean,
        sortFilterState: () -> SortFilterController<*>.State,
        userRoute: UserRoute,
        mediaTitle: @Composable (MediaEntry) -> String?,
        mediaHeaderParams: (MediaEntry, title: String?, ImageState) -> MediaHeaderParams,
        mediaImageUri: (MediaEntry?) -> String?,
        mediaRow: @Composable (
            MediaEntry?,
            coverImageState: CoilImageState,
            Modifier,
        ) -> Unit,
    ) {
        val refreshState = reviews.loadState.refresh
        val refreshing = refreshState is LoadState.Loading
        val pullRefreshState = rememberPullRefreshState(
            refreshing = refreshing,
            onRefresh = reviews::refresh,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .pullRefresh(pullRefreshState)
        ) {
            val gridState = rememberLazyGridState()
            sortFilterState().ImmediateScrollResetEffect(gridState)
            LazyVerticalGrid(
                state = gridState,
                columns = GridUtils.standardWidthAdaptiveCells,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 72.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    count = reviews.itemCount,
                    key = reviews.itemKey { it.review.id },
                    contentType = reviews.itemContentType { "review" }
                ) {
                    val entry = reviews[it]
                    SharedTransitionKeyScope("anime_review_card", entry?.review?.id.toString()) {
                        val mediaTitle = entry?.media?.let { mediaTitle(it) }
                        val navigationController = LocalNavigationController.current
                        ReviewCard(
                            review = entry?.review,
                            media = entry?.media,
                            userRoute = userRoute,
                            onClick = { coverImageState ->
                                if (entry != null) {
                                    navigationController.navigate(
                                        ReviewDestinations.ReviewDetails(
                                            reviewId = entry.review.id.toString(),
                                            headerParams = mediaHeaderParams(
                                                entry.media,
                                                mediaTitle,
                                                coverImageState.toImageState()
                                            )
                                        )
                                    )
                                }
                            },
                            mediaImageUri = mediaImageUri,
                            mediaRow = mediaRow,
                            showMedia = showMedia,
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
