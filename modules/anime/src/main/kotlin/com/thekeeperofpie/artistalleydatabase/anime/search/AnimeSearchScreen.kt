package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.anilist.MediaAdvancedSearchQuery.Data.Page.Medium
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeListMediaRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaFilterOptionsBottomPanel
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.compose.NavMenuIconButton
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
object AnimeSearchScreen {

    @Composable
    operator fun invoke(
        onClickNav: () -> Unit = {},
        query: @Composable () -> String = { "" },
        onQueryChange: (String) -> Unit = {},
        filterData: () -> AnimeMediaFilterController.Data<MediaSortOption>,
        onRefresh: () -> Unit = {},
        content: @Composable () -> LazyPagingItems<Entry> = {
            flowOf(PagingData.empty<Entry>()).collectAsLazyPagingItems()
        },
    ) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth()
                ) {
                    TextField(
                        query(),
                        placeholder = { Text(stringResource(id = R.string.anime_search)) },
                        onValueChange = onQueryChange,
                        leadingIcon = { NavMenuIconButton(onClickNav) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            },
            modifier = Modifier.background(Color.Red)
        ) {
            AnimeMediaFilterOptionsBottomPanel(
                filterData = filterData,
                modifier = Modifier.padding(it),
            ) {
                MainContent(
                    content = content,
                    onRefresh = onRefresh,
                    modifier = Modifier.padding(it),
                )
            }
        }
    }

    @Composable
    private fun MainContent(
        content: @Composable () -> LazyPagingItems<Entry>,
        modifier: Modifier = Modifier,
        onRefresh: () -> Unit = {},
    ) {
        @Suppress("NAME_SHADOWING")
        val content = content()
        val refreshing = content.loadState.refresh is LoadState.Loading
        val pullRefreshState = rememberPullRefreshState(refreshing, onRefresh)

        Box(
            modifier = modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn {
                items(content, { it.id.scopedId }) {
                    when (it) {
                        is Entry.Item -> AnimeListMediaRow(it)
                        is Entry.LoadMore -> TODO()
                        null -> AnimeListMediaRow(AnimeListMediaRow.Entry.Loading)
                    }
                }

                when (content.loadState.append) {
                    is LoadState.Loading -> item(key = "load_more_append") { LoadingMore() }
                    is LoadState.Error -> item(key = "load_more_error") { Error() }
                    is LoadState.NotLoading -> Unit
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
    private fun Error() {
        Text(
            stringResource(id = R.string.anime_media_list_error_loading),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }

    @Composable
    private fun LoadingMore() {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            CircularProgressIndicator()
        }
    }

    sealed interface Entry {

        val id: EntryId

        data class Item(
            val media: Medium,
        ) : Entry, AnimeListMediaRow.Entry {
            override val id = EntryId("item", media.id.toString())
            override val image = media.coverImage?.large
            override val title = media.title?.userPreferred
        }

        data class LoadMore(val valueId: String) : Entry {
            override val id = EntryId("load_more", valueId)
        }
    }
}


@Preview
@Composable
private fun Preview() {
    AnimeSearchScreen(
        content = {
            flowOf(
                PagingData.from<AnimeSearchScreen.Entry>(
                    listOf(
                        AnimeSearchScreen.Entry.Item(
                            Medium(
                                title = Medium.Title(
                                    userPreferred = "Ano Hi Mita Hana no Namae wo Bokutachi wa Mada Shiranai.",
                                ),
                            )
                        )
                    )
                )
            ).collectAsLazyPagingItems()
        },
        filterData = { AnimeMediaFilterController.Data.forPreview() }
    )
}