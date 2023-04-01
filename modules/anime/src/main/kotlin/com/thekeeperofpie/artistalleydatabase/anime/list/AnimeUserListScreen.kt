package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

object AnimeUserListScreen {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    operator fun invoke(
        content: AnimeUserListViewModel.ContentState,
        onRefresh: () -> Unit = {},
    ) {
        val refreshing = content is AnimeUserListViewModel.ContentState.Loading
        val pullRefreshState = rememberPullRefreshState(
            refreshing = refreshing,
            onRefresh = onRefresh
        )
        Scaffold(
            snackbarHost = {
                if (content is AnimeUserListViewModel.ContentState.Error) {
                    SnackbarErrorText(
                        content.errorRes,
                        content.exception,
                        onErrorDismiss = {}
                    )
                }
            },
            modifier = Modifier.pullRefresh(pullRefreshState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                when (content) {
                    is AnimeUserListViewModel.ContentState.Error -> Error(content)
                    AnimeUserListViewModel.ContentState.Loading -> Unit // pullRefresh handles this
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
                model = item.entry.media?.coverImage?.large,
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
                text = item.entry.media?.title?.userPreferred.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Preview
@Composable
fun Preview() {
    AnimeUserListScreen(
        AnimeUserListViewModel.ContentState.Success(
            listOf(
                MediaListEntry.Header("Completed", MediaListStatus.COMPLETED),
                MediaListEntry.Item(
                    UserMediaListQuery.Data.MediaListCollection.List.Entry(
                        1, UserMediaListQuery.Data.MediaListCollection.List.Entry.Media(
                            id = 1,
                            coverImage = null,
                            title = UserMediaListQuery.Data.MediaListCollection.List.Entry.Media.Title(
                                userPreferred = "Ano Hi Mita Hana no Namae wo Bokutachi wa Mada Shiranai.",
                                romaji = null,
                                english = null,
                                native = null,
                            ),
                            averageScore = null,
                            meanScore = null,
                        )
                    )
                )
            )
        )
    )
}