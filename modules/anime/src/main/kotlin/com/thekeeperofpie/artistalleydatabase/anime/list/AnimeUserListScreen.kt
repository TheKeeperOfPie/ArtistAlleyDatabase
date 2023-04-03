package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anilist.UserMediaListQuery
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.bottomBorder

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
object AnimeUserListScreen {

    @Composable
    operator fun invoke(
        content: AnimeUserListViewModel.ContentState,
        onRefresh: () -> Unit = {},
        sort: @Composable () -> MediaListSortOption = { MediaListSortOption.STATUS },
        onSortChanged: (MediaListSortOption) -> Unit = {},
        sortAscending: @Composable () -> Boolean = { false },
        onSortAscendingChanged: (Boolean) -> Unit = {},
    ) {
        val scaffoldState = rememberBottomSheetScaffoldState()
        LaunchedEffect(true) {
            // An initial value of HIDE crashes, so just hide it manually
            scaffoldState.bottomSheetState.hide()
        }

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                AnimeMediaListSortOptionsPanel(
                    sort = sort,
                    onSortChanged = onSortChanged,
                    sortAscending = sortAscending,
                    onSortAscendingChanged = onSortAscendingChanged,
                )
            },
            snackbarHost = {
                if (content is AnimeUserListViewModel.ContentState.Error) {
                    SnackbarErrorText(
                        content.errorRes,
                        content.exception,
                        onErrorDismiss = {}
                    )
                } else {
                    // Bottom sheet requires at least one measurable component
                    Spacer(modifier = Modifier.size(0.dp))
                }
            },
        ) {
            MainContent(
                parentPaddingValues = it,
                content = content,
                onRefresh = onRefresh,
            )
        }
    }

    @Composable
    private fun MainContent(
        parentPaddingValues: PaddingValues,
        content: AnimeUserListViewModel.ContentState,
        onRefresh: () -> Unit = {},
    ) {
        val refreshing = when (content) {
            AnimeUserListViewModel.ContentState.LoadingEmpty -> true
            is AnimeUserListViewModel.ContentState.Success -> content.loading
            is AnimeUserListViewModel.ContentState.Error -> false
        }

        val pullRefreshState = rememberPullRefreshState(refreshing, onRefresh)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(parentPaddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            when (content) {
                is AnimeUserListViewModel.ContentState.Error -> Error(content)
                AnimeUserListViewModel.ContentState.LoadingEmpty -> Unit // pullRefresh handles this
                is AnimeUserListViewModel.ContentState.Success -> LazyColumn {
                    items(content.entries) {
                        when (it) {
                            is MediaListEntry.Header -> Header(it)
                            is MediaListEntry.Item -> Item(it)
                            is MediaListEntry.LoadMore -> TODO()
                        }
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

    @Composable
    private fun Error(error: AnimeUserListViewModel.ContentState.Error) {
        Text(
            error.errorRes?.let { stringResource(it) }
                ?: stringResource(id = R.string.anime_media_list_error_loading),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }

    @Composable
    private fun Header(header: MediaListEntry.Header) {
        Text(
            text = header.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 24.dp, end = 24.dp, bottom = 10.dp)
        )
    }

    @Composable
    private fun Item(item: MediaListEntry.Item) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .bottomBorder(1.dp, MaterialTheme.colorScheme.onSurface)
        ) {
            AsyncImage(
                model = item.media.coverImage?.large,
                contentScale = ContentScale.Crop,
                fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                contentDescription = stringResource(R.string.anime_media_cover_image),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .heightIn(120.dp)
                    .width(80.dp)
                    .fillMaxHeight()
            )

            Text(
                text = item.media.title?.userPreferred.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    AnimeUserListScreen(
        AnimeUserListViewModel.ContentState.Success(
            listOf(
                MediaListEntry.Header("Completed", MediaListStatus.COMPLETED),
                MediaListEntry.Item(
                    UserMediaListQuery.Data.MediaListCollection.List.Entry.Media(
                        title = UserMediaListQuery.Data.MediaListCollection.List.Entry.Media.Title(
                            userPreferred = "Ano Hi Mita Hana no Namae wo Bokutachi wa Mada Shiranai.",
                        ),
                    )
                )
            )
        )
    )
}