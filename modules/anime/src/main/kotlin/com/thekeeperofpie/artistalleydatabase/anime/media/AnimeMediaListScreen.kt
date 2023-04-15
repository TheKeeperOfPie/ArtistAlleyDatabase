package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anilist.fragment.AniListListRowMedia
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@OptIn(ExperimentalMaterialApi::class)
object AnimeMediaListScreen {

    @Composable
    operator fun invoke(
        refreshing: Boolean,
        onRefresh: () -> Unit,
        modifier: Modifier = Modifier,
        listContent: @Composable () -> Unit,
    ) {
        val pullRefreshState = rememberPullRefreshState(refreshing, onRefresh)

        Box(
            modifier = modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            listContent()

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    @Composable
    fun Error() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                stringResource(id = R.string.anime_media_list_error_loading),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }

    @Composable
    fun NoResults() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                stringResource(id = R.string.anime_media_list_no_results),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }

    @Composable
    fun AppendError(onRetry: () -> Unit) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onRetry),
        ) {
            Text(
                stringResource(id = R.string.anime_media_list_error_loading),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }

    @Composable
    fun LoadingMore() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            CircularProgressIndicator()
        }
    }

    sealed interface Entry {

        val id: EntryId

        class Item(media: AniListListRowMedia) : Entry, AnimeMediaListRow.MediaEntry(media)
    }
}