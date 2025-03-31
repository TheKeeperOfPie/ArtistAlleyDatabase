package com.thekeeperofpie.artistalleydatabase.alley.browse

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_browse_tab_merch
import artistalleydatabase.modules.alley.generated.resources.alley_browse_tab_series
import artistalleydatabase.modules.entry.generated.resources.entry_search_clear
import artistalleydatabase.modules.entry.generated.resources.entry_search_hint
import artistalleydatabase.modules.entry.generated.resources.entry_search_hint_with_entry_count
import com.thekeeperofpie.artistalleydatabase.alley.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesFilterOption
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.tags.name
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItemsWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.VerticalScrollbar
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.entry.generated.resources.Res as EntryRes

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class
)
object BrowseScreen {

    @Composable
    operator fun invoke(
        tagsViewModel: TagsViewModel,
        dataYearHeaderState: DataYearHeaderState,
        seriesFiltersState: () -> List<Pair<SeriesFilterOption, Boolean>>,
        onSeriesFilterClick: (SeriesFilterOption) -> Unit,
        onSeriesClick: (SeriesEntry) -> Unit,
        onMerchClick: (MerchEntry) -> Unit,
    ) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 1200.dp)
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
                val series = tagsViewModel.series.collectAsLazyPagingItemsWithLifecycle()
                val merch = tagsViewModel.merch.collectAsLazyPagingItemsWithLifecycle()
                AnimatedContent(
                    targetState = Tab.entries[selectedTabIndex],
                    transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                    label = "Tag screen",
                ) {
                    val scrollStateSaver = ScrollStateSaver.fromMap(it.name, scrollPositions)
                    when (it) {
                        Tab.SERIES -> {
                            val languageOption = LocalLanguageOptionMedia.current
                            TabScreen(
                                dataYearHeaderState = dataYearHeaderState,
                                query = { tagsViewModel.seriesQuery },
                                onQueryChange = { tagsViewModel.seriesQuery = it },
                                entriesSize = { series.itemCount },
                                values = series,
                                itemKey = { it.id },
                                itemToText = { it.name(languageOption) },
                                onItemClick = onSeriesClick,
                                scrollStateSaver = scrollStateSaver,
                                additionalHeader = {
                                    item(key = "seriesLanguageOption") {
                                        tagsViewModel.seriesLanguageSection
                                            .Content(Modifier.fillMaxWidth())
                                    }
                                    item(key = "seriesFilters") {
                                        SeriesFilters(seriesFiltersState(), onSeriesFilterClick)
                                    }
                                }
                            )
                        }
                        Tab.MERCH -> TabScreen(
                            dataYearHeaderState = dataYearHeaderState,
                            query = { tagsViewModel.merchQuery },
                            onQueryChange = { tagsViewModel.merchQuery = it },
                            entriesSize = { merch.itemCount },
                            values = merch,
                            itemKey = { it.name },
                            itemToText = { it.name },
                            onItemClick = onMerchClick,
                            scrollStateSaver = scrollStateSaver
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun <T : Any> TabScreen(
        dataYearHeaderState: DataYearHeaderState,
        query: () -> String,
        onQueryChange: (String) -> Unit,
        entriesSize: () -> Int,
        values: LazyPagingItems<T>,
        itemKey: (T) -> Any,
        itemToText: (T) -> String,
        onItemClick: (T) -> Unit,
        scrollStateSaver: ScrollStateSaver,
        additionalHeader: LazyListScope.() -> Unit = {},
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        Scaffold(
            topBar = {
                EnterAlwaysTopAppBar(scrollBehavior = scrollBehavior) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        val isNotEmpty by remember {
                            derivedStateOf { query().isNotEmpty() }
                        }
                        BackHandler(isNotEmpty && !WindowInsets.isImeVisibleKmp) {
                            onQueryChange("")
                        }

                        StaticSearchBar(
                            query = query(),
                            onQueryChange = onQueryChange,
                            placeholder = {
                                @Suppress("NAME_SHADOWING")
                                val entriesSize = entriesSize()
                                Text(
                                    if (entriesSize > 0) {
                                        stringResource(
                                            EntryRes.string.entry_search_hint_with_entry_count,
                                            entriesSize,
                                        )
                                    } else {
                                        stringResource(EntryRes.string.entry_search_hint)
                                    }
                                )
                            },
                            trailingIcon = {
                                AnimatedVisibility(isNotEmpty) {
                                    IconButton(onClick = { onQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = stringResource(
                                                EntryRes.string.entry_search_clear
                                            ),
                                        )
                                    }
                                }
                            },
                            onSearch = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                        )
                    }
                }
            },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                val listState = scrollStateSaver.lazyListState()
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 600.dp)
                        .align(Alignment.TopCenter)
                ) {
                    item(key = "dataYearHeader") {
                        DataYearHeader(dataYearHeaderState)
                    }

                    additionalHeader()

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

                VerticalScrollbar(
                    state = listState,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(bottom = 72.dp)
                )
            }
        }
    }

    @Composable
    private fun SeriesFilters(
        seriesFiltersState: List<Pair<SeriesFilterOption, Boolean>>,
        onSeriesFilterClick: (SeriesFilterOption) -> Unit,
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth(),
        ) {
            seriesFiltersState.forEach { (option, selected) ->
                FilterChip(
                    selected = selected,
                    label = { Text(stringResource(option.title)) },
                    onClick = { onSeriesFilterClick(option) },
                )
            }
        }
    }

    enum class Tab(val textRes: StringResource) {
        SERIES(Res.string.alley_browse_tab_series),
        MERCH(Res.string.alley_browse_tab_merch),
    }
}
