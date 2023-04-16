package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anilist.UserMediaListQuery.Data.MediaListCollection.List.Entry.Media
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterOptionsBottomPanel
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@OptIn(ExperimentalMaterial3Api::class)
object AnimeUserListScreen {

    @Composable
    operator fun invoke(
        onClickNav: () -> Unit = {},
        filterData: () -> AnimeMediaFilterController.Data<MediaListSortOption>,
        onRefresh: () -> Unit = {},
        content: () -> AnimeUserListViewModel.ContentState,
        tagShown: () -> AnimeMediaFilterController.TagSection.Tag? = { null },
        onTagDismiss: () -> Unit = {},
        onTagClick: (tagId: String, tagName: String) -> Unit = { _, _ -> },
        onTagLongClick: (tagId: String) -> Unit = {},
    ) {
        AnimeMediaFilterOptionsBottomPanel(
            topBar = {
                AppBar(
                    text = stringResource(R.string.anime_home_title),
                    onClickNav = onClickNav
                )
            },
            filterData = filterData,
            onTagLongClicked = onTagLongClick,
        ) {
            @Suppress("NAME_SHADOWING")
            val content = content()
            val refreshing = when (content) {
                AnimeUserListViewModel.ContentState.LoadingEmpty -> true
                is AnimeUserListViewModel.ContentState.Success -> content.loading
                is AnimeUserListViewModel.ContentState.Error -> false
            }

            AnimeMediaListScreen(
                refreshing = refreshing,
                onRefresh = onRefresh,
                tagShown = tagShown,
                onTagDismiss = onTagDismiss,
                modifier = Modifier.padding(it),
            ) { onLongPressImage ->
                when (content) {
                    is AnimeUserListViewModel.ContentState.Error -> AnimeMediaListScreen.Error()
                    AnimeUserListViewModel.ContentState.LoadingEmpty ->
                        // Empty column to allow pull refresh to work
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {}
                    is AnimeUserListViewModel.ContentState.Success ->
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(content.entries, { it.id.scopedId }) {
                                when (it) {
                                    is Entry.Header -> Header(it)
                                    is Entry.Item -> AnimeMediaListRow(
                                        entry = it,
                                        onTagClick = onTagClick,
                                        onTagLongClick = onTagLongClick,
                                        onLongPressImage = onLongPressImage
                                    )
                                }
                            }
                        }
                }
            }
        }
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

        class Item(media: Media) : Entry, AnimeMediaListRow.MediaEntry(media)
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