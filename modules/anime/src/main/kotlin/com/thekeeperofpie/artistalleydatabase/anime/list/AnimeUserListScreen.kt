package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anilist.UserMediaListQuery.Data.MediaListCollection.List.Entry.Media
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeListMediaRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaFilterOptionsBottomPanel
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
object AnimeUserListScreen {

    @Composable
    operator fun invoke(
        onClickNav: () -> Unit = {},
        filterData: () -> AnimeMediaFilterController.Data<MediaListSortOption>,
        onRefresh: () -> Unit = {},
        content: () -> AnimeUserListViewModel.ContentState,
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(R.string.anime_home_title),
                    onClickNav = onClickNav
                )
            },
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
        content: () -> AnimeUserListViewModel.ContentState,
        modifier: Modifier = Modifier,
        onRefresh: () -> Unit = {},
    ) {
        @Suppress("NAME_SHADOWING")
        val content = content()
        val refreshing = when (content) {
            AnimeUserListViewModel.ContentState.LoadingEmpty -> true
            is AnimeUserListViewModel.ContentState.Success -> content.loading
            is AnimeUserListViewModel.ContentState.Error -> false
        }

        val pullRefreshState = rememberPullRefreshState(refreshing, onRefresh)

        Box(
            modifier = modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            when (content) {
                is AnimeUserListViewModel.ContentState.Error -> Error(content)
                AnimeUserListViewModel.ContentState.LoadingEmpty -> Unit // pullRefresh handles this
                is AnimeUserListViewModel.ContentState.Success ->
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(content.entries, { it.id.scopedId }) {
                            when (it) {
                                is Entry.Header -> Header(it)
                                is Entry.Item -> AnimeListMediaRow(it)
                                is Entry.LoadMore -> TODO()
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
    private fun Header(header: Entry.Header) {
        Text(
            text = header.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 24.dp, end = 24.dp, bottom = 10.dp)
        )
    }

    sealed interface Entry {

        val id: EntryId

        data class Header(val name: String, val status: MediaListStatus?) : Entry {
            override val id = EntryId("header", name)
        }

        class Item(media: Media) : Entry, AnimeListMediaRow.MediaEntry(media)

        data class LoadMore(val valueId: String) : Entry {
            override val id = EntryId("load_more", valueId)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    AnimeUserListScreen(
        content = {
            AnimeUserListViewModel.ContentState.Success(
                listOf(
                    AnimeUserListScreen.Entry.Header("Completed", MediaListStatus.COMPLETED),
                    AnimeUserListScreen.Entry.Item(
                        Media(
                            title = Media.Title(
                                userPreferred = "Ano Hi Mita Hana no Namae wo Bokutachi wa Mada Shiranai.",
                            ),
                        )
                    )
                )
            )
        },
        filterData = { AnimeMediaFilterController.Data.forPreview() }
    )
}