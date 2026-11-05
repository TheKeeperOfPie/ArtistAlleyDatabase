package com.thekeeperofpie.artistalleydatabase.alley.browse

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_browse_tab_merch
import artistalleydatabase.modules.alley.generated.resources.alley_browse_tab_series
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_confirmed_tags
import artistalleydatabase.modules.alley.generated.resources.alley_search_no_results
import artistalleydatabase.modules.alley.generated.resources.alley_settings_series_language
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.data.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterSheetContent
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterState
import com.thekeeperofpie.artistalleydatabase.alley.series.ui.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.BrowseViewModel
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.AlleySearchBar
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.alley.ui.InfiniteProgressIndicator
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.DisplayConstants
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopBarScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.DropdownFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SwitchSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
object BrowseScreen {

    @Composable
    operator fun invoke(browseViewModel: BrowseViewModel,) {
        val series = browseViewModel.series.collectAsLazyPagingItems()
        val merch = browseViewModel.merch.collectAsLazyPagingItems()
        BrowseScreen(
            state = browseViewModel.state,
            series = series,
            merch = merch,
            onEvent = browseViewModel::onEvent,
        )
    }

    @Composable
    operator fun invoke(
        state: State,
        series: LazyPagingItems<SeriesWithUserData>,
        merch: LazyPagingItems<MerchWithUserData>,
        onEvent: (Event) -> Unit,
    ) {
        val dataYearHeaderState = rememberDataYearHeaderState(
            dataYear = state.dataYear,
            onDataYearChange = state.onDataYearChange,
            lockedYear = null,
        )
        val selectedTabIndex = state.selectedTabIndex()

        EnterAlwaysTopBarScaffold(
            topBar = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PrimaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier
                            .widthIn(max = DisplayConstants.MAX_CONTENT_WIDTH)
                    ) {
                        Tab.entries.forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { state.onSelectedTabIndexChange(index) },
                                text = { Text(stringResource(tab.textRes)) },
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .widthIn(max = DisplayConstants.MAX_CONTENT_WIDTH)
                            .padding(bottom = 8.dp)
                    ) {
                        val query = when (selectedTabIndex) {
                            0 -> state.seriesQuery
                            else -> state.merchQuery
                        }
                        val entries = when (selectedTabIndex) {
                            0 -> series
                            else -> merch
                        }
                        val isNotEmpty by remember {
                            derivedStateOf { query.text.isNotEmpty() }
                        }
                        BackHandler(isNotEmpty && !WindowInsets.isImeVisibleKmp) {
                            query.clearText()
                        }

                        AlleySearchBar(
                            onClickBack = null,
                            query = query,
                            itemCount = { entries.itemCount },
                            title = { null },
                            modifier = Modifier
                                .widthIn(max = DisplayConstants.MAX_CONTENT_WIDTH)
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                        )
                    }
                }
            }
        ) { contentPadding ->
            val saveableStateHolder = rememberSaveableStateHolder()
            AnimatedContent(
                targetState = Tab.entries[selectedTabIndex],
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                label = "Tag screen",
            ) {
                saveableStateHolder.SaveableStateProvider(it) {
                    when (it) {
                        Tab.SERIES -> {
                            TabScreen(
                                dataYearHeaderState = dataYearHeaderState,
                                baseContentPadding = contentPadding,
                                values = series,
                                itemKey = { it.series.id },
                                item = { data ->
                                    SeriesRow(
                                        data = data,
                                        textStyle = LocalTextStyle.current,
                                        onFavoriteToggle = {
                                            if (data != null) {
                                                onEvent(Event.ToggleSeriesFavorite(data, it))
                                            }
                                        },
                                        onClick = {
                                            data?.let {
                                                onEvent(Event.OpenSeries(it.series))
                                            }
                                        },
                                    )
                                },
                                onOpenChangelog = { onEvent(Event.OpenSeriesChangelog) },
                                onOpenSettings = { onEvent(Event.OpenSettings) },
                                additionalHeader = {
                                    item(key = "seriesFilterOptions") {
                                        Column {
                                            DropdownFilterSection(
                                                options = AniListLanguageOption.entries,
                                                selected = state.languageOption,
                                                onSelectedChanged = state.onLanguageOptionChange,
                                                label = {
                                                    Text(stringResource(Res.string.alley_settings_series_language))
                                                },
                                            )

                                            HorizontalDivider()

                                            val saveableState = state.seriesSortFilterState.saveableState
                                            SwitchSection(
                                                title = { Text(stringResource(Res.string.alley_filter_show_only_confirmed_tags)) },
                                                enabled = saveableState::showOnlyConfirmedTags,
                                                onEnabledChanged = {
                                                    saveableState.showOnlyConfirmedTags = it
                                                },
                                            )
                                        }
                                    }
                                }
                            )
                        }
                        Tab.MERCH -> TabScreen(
                            dataYearHeaderState = dataYearHeaderState,
                            baseContentPadding = contentPadding,
                            values = merch,
                            itemKey = { it.merch.name },
                            item = { data ->
                                MerchRow(
                                    data = data,
                                    onFavoriteToggle = {
                                        if (data != null) {
                                            onEvent(Event.ToggleMerchFavorite(data, it))
                                        }
                                    },
                                    onClick = {
                                        if (data != null) {
                                            onEvent(Event.OpenMerch(data.merch))
                                        }
                                    },
                                )
                            },
                            onOpenChangelog = { onEvent(Event.OpenMerchChangelog) },
                            onOpenSettings = { onEvent(Event.OpenSettings) },
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun SheetContent(state: State, bottomSheetState: SheetState) {
        when (state.selectedTabIndex()) {
            0 -> SeriesSortFilterSheetContent(
                state = state.seriesSortFilterState,
                sheetState = bottomSheetState,
            )
            // TODO: Real merch sort filter?
            else -> Box {}
        }
    }

    @Composable
    private fun <T : Any> TabScreen(
        dataYearHeaderState: DataYearHeaderState,
        baseContentPadding: () -> PaddingValues,
        values: LazyPagingItems<T>,
        itemKey: (T) -> Any,
        item: @Composable (T?) -> Unit,
        onOpenChangelog: (DataYear) -> Unit,
        onOpenSettings: () -> Unit,
        additionalHeader: LazyListScope.() -> Unit = {},
    ) {
        val listState = rememberLazyListState()
        val scrollAreaState = rememberScrollAreaState(listState)
        ScrollArea(
            state = scrollAreaState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            BoxWithConstraints {
                val horizontalContentPadding =
                    if (maxWidth > DisplayConstants.MAX_CONTENT_WIDTH) {
                        (maxWidth - DisplayConstants.MAX_CONTENT_WIDTH) / 2
                    } else {
                        0.dp
                    }
                LazyColumn(
                    state = listState,
                    contentPadding = baseContentPadding() + PaddingValues(
                        start = horizontalContentPadding,
                        end = horizontalContentPadding,
                        bottom = 200.dp,
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter)
                ) {
                    item(key = "dataYearHeader") {
                        DataYearHeader(
                            state = dataYearHeaderState,
                            onOpenChangelog = onOpenChangelog,
                            onOpenSettings = onOpenSettings,
                        )
                    }

                    additionalHeader()

                    if (values.itemCount == 0) {
                        if (values.loadState.refresh is LoadState.Loading) {
                            item("loadingIndicator") {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    InfiniteProgressIndicator()
                                }
                            }
                        } else {
                            item("noResults") {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(Res.string.alley_search_no_results),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            count = values.itemCount,
                            key = values.itemKey(itemKey),
                            contentType = values.itemContentType { "tag_entry" },
                        ) { index ->
                            val value = values[index]
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item(value)
                                HorizontalDivider()
                            }
                        }
                    }
                }

                PrimaryVerticalScrollbar(listState)
            }
        }
    }

    enum class Tab(val textRes: StringResource) {
        SERIES(Res.string.alley_browse_tab_series),
        MERCH(Res.string.alley_browse_tab_merch),
    }

    @Stable
    class State(
        val dataYear: () -> DataYear,
        val onDataYearChange: (DataYear) -> Unit,
        val selectedTabIndex: () -> Int,
        val onSelectedTabIndexChange: (Int) -> Unit,
        val seriesQuery: TextFieldState,
        val merchQuery: TextFieldState,
        val languageOption: () -> AniListLanguageOption,
        val onLanguageOptionChange: (AniListLanguageOption) -> Unit,
        val seriesSortFilterState: SeriesSortFilterState,
    )

    sealed interface Event {
        data class OpenSeries(val seriesInfo: SeriesInfo) : Event
        data class OpenMerch(val merch: MerchEntry) : Event
        data object OpenSeriesChangelog : Event
        data object OpenMerchChangelog : Event
        data object OpenSettings : Event
        data class ToggleMerchFavorite(val data: MerchWithUserData, val favorite: Boolean) : Event
        data class ToggleSeriesFavorite(val data: SeriesWithUserData, val favorite: Boolean) : Event
    }
}
