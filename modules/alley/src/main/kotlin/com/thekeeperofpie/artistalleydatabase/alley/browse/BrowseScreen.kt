package com.thekeeperofpie.artistalleydatabase.alley.browse

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagsViewModel
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver

@OptIn(ExperimentalMaterial3Api::class)
object BrowseScreen {

    @Composable
    operator fun invoke(
        onSeriesClick: (SeriesEntry) -> Unit,
        onMerchClick: (MerchEntry) -> Unit,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(stringResource(tab.textRes)) },
                    )
                }
            }
            val scrollPositions = ScrollStateSaver.scrollPositions()
            val viewModel = hiltViewModel<TagsViewModel>()
            AnimatedContent(
                targetState = Tab.entries[selectedTabIndex],
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                label = "Tag screen",
            ) {
                val scrollStateSaver = ScrollStateSaver.fromMap(it.name, scrollPositions)
                when (it) {
                    Tab.SERIES -> TabScreen(
                        values = viewModel.series.collectAsLazyPagingItems(),
                        itemKey = { it.name },
                        itemToText = { it.name },
                        onItemClick = onSeriesClick,
                        scrollStateSaver = scrollStateSaver
                    )
                    Tab.MERCH -> TabScreen(
                        values = viewModel.merch.collectAsLazyPagingItems(),
                        itemKey = { it.name },
                        itemToText = { it.name },
                        onItemClick = onMerchClick,
                        scrollStateSaver = scrollStateSaver
                    )
                }
            }
        }
    }

    @Composable
    private fun <T : Any> TabScreen(
        values: LazyPagingItems<T>,
        itemKey: (T) -> Any,
        itemToText: (T) -> String,
        onItemClick: (T) -> Unit,
        scrollStateSaver: ScrollStateSaver,
    ) {
        LazyColumn(
            state = scrollStateSaver.lazyListState(),
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 400.dp)
        ) {
            items(
                count = values.itemCount,
                key = values.itemKey(itemKey),
                contentType = values.itemContentType { "tag_entry" },
            ) { index ->
                val value = values[index]
                val text = value?.let(itemToText).orEmpty()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { value?.let(onItemClick) }
                ) {
                    Text(
                        text = text,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    enum class Tab(@StringRes val textRes: Int) {
        SERIES(R.string.alley_browse_tab_series),
        MERCH(R.string.alley_browse_tab_merch),
    }
}
