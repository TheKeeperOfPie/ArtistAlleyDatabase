package com.thekeeperofpie.artistalleydatabase.browse

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryColumn
import kotlinx.coroutines.launch

object BrowseScreen {

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    operator fun invoke(
        tabs: List<TabContent>,
        onClick: (column: ArtEntryColumn, value: String) -> Unit = { _, _ -> },
    ) {
        Column {
            val pagerState = rememberPagerState()
            val selectedTabIndex = pagerState.currentPage
            TabRow(selectedTabIndex = selectedTabIndex) {
                val coroutineScope = rememberCoroutineScope()
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        text = { Text(stringResource(tab.textRes())) },
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                    )
                }
            }

            HorizontalPager(
                count = tabs.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(Modifier.fillMaxSize()) {
                    val tab = tabs[it]
                    val content = tab.content()
                    items(content.size) {
                        val value = content[it]
                        TextRow(text = value, onClick = {
                            onClick(tab.type, value)
                        })
                    }
                }
            }
        }
    }

    @Composable
    private fun TextRow(text: String, onClick: () -> Unit) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
        )
    }

    @Immutable
    data class TabContent(
        val type: ArtEntryColumn,
        @StringRes val textRes: () -> Int,
        val content: () -> List<String>,
    )
}