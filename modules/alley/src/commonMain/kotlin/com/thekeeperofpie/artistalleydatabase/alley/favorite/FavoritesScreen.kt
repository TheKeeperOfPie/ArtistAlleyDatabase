package com.thekeeperofpie.artistalleydatabase.alley.favorite

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_con_upcoming_show_qr
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_artists
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_artists
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_go_to_artists
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_go_to_merch
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_go_to_series
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_go_to_stamp_rallies
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_merch
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_series
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_stamp_rallies
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_merch
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_rallies
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_search
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_series
import artistalleydatabase.modules.alley.generated.resources.alley_user_login_action
import artistalleydatabase.modules.alley.generated.resources.alley_user_login_prompt
import com.composeunstyled.rememberScrollbarState
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreenContent
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterSheetContent
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterState
import com.thekeeperofpie.artistalleydatabase.alley.links.Logo
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreenContent
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterSheetContent
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterState
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchDisplayType
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterSheetContent
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterState
import com.thekeeperofpie.artistalleydatabase.alley.series.ui.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.AlleySearchBar
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.alley.ui.InfiniteProgressIndicator
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.QrCode2
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopBarScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.FilledTonalButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderMaybeInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_scrollbars.VerticalScrollableWithBar
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object FavoritesScreen {

    @Composable
    operator fun invoke(
        onNavigateToArtists: () -> Unit,
        onNavigateToRallies: () -> Unit,
        onNavigateToSeries: () -> Unit,
        onNavigateToMerch: () -> Unit,
        viewModel: FavoritesViewModel,
    ) {
        val uriHandler = LocalUriHandler.current
        FavoritesScreen(
            state = viewModel.state,
            series = viewModel.seriesEntryCache::series,
            eventSink = {
                viewModel.onEvent(
                    event = it,
                    onNavigateToArtists = onNavigateToArtists,
                    onNavigateToRallies = onNavigateToRallies,
                    onNavigateToSeries = onNavigateToSeries,
                    onNavigateToMerch = onNavigateToMerch,
                    uriHandler = uriHandler,
                )
            },
        )
    }

    @Composable
    operator fun invoke(
        state: State,
        series: () -> Map<String, GetSeriesTitles>,
        eventSink: (Event) -> Unit,
    ) {
        CompositionLocalProvider(LocalStableRandomSeed provides state.randomSeed) {
            val artistsEntries = state.artistsEntries.collectAsLazyPagingItems()
            val ralliesEntries = state.ralliesEntries.collectAsLazyPagingItems()
            val seriesEntries = state.seriesEntries.collectAsLazyPagingItems()
            val merchEntries = state.merchEntries.collectAsLazyPagingItems()

            val dataYearHeaderState = rememberDataYearHeaderState(
                dataYear = state.year,
                onDataYearChange = state.onYearChange,
                lockedYear = null,
            )
            val entries = when (state.tab()) {
                EntryTab.ARTISTS -> artistsEntries
                EntryTab.RALLIES -> ralliesEntries
                EntryTab.SERIES -> seriesEntries
                EntryTab.MERCH -> merchEntries
            }
            EnterAlwaysTopBarScaffold(
                topBar = {
                    val title = stringResource(Res.string.alley_favorites_search)
                    AlleySearchBar(
                        onClickBack = null,
                        query = state.query,
                        title = { title },
                        itemCount = { entries.itemCount },
                        modifier = Modifier
                            .animateEnterExit(
                                enter = slideInVertically { -it },
                                exit = slideOutVertically { -it },
                            )
                            .renderMaybeInSharedTransitionScopeOverlay(1f)
                    )
                },
            ) { contentPadding ->
                val saveableStateHolder = rememberSaveableStateHolder()
                val tab = state.tab()
                val header: @Composable () -> Unit =
                    remember(state, dataYearHeaderState, eventSink) {
                        movableContentOf {
                            Header(
                                tab = state.tab,
                                onTabChange = state.onTabChange,
                                isLoggedIn = state.isLoggedIn,
                                dataYearHeaderState = dataYearHeaderState,
                                onEvent = eventSink,
                            )
                        }
                    }
                saveableStateHolder.SaveableStateProvider(tab) {
                    when (tab) {
                        EntryTab.ARTISTS -> {
                            ArtistContent(
                                state = state,
                                baseContentPadding = contentPadding,
                                sortFilterState = state.artistsSortFilterState,
                                entries = artistsEntries,
                                series = series,
                                eventSink = eventSink,
                                header = header,
                                noResultsItem = { NoResultsItem(EntryTab.ARTISTS, eventSink) },
                            )
                        }
                        EntryTab.RALLIES ->
                            RalliesContent(
                                state = state,
                                baseContentPadding = contentPadding,
                                sortFilterState = state.ralliesSortFilterState,
                                entries = ralliesEntries,
                                eventSink = eventSink,
                                header = header,
                                noResultsItem = { NoResultsItem(EntryTab.RALLIES, eventSink) },
                            )
                        EntryTab.SERIES -> SeriesContent(
                            baseContentPadding = contentPadding,
                            series = seriesEntries,
                            header = header,
                            eventSink = eventSink,
                        )
                        EntryTab.MERCH -> MerchContent(
                            baseContentPadding = contentPadding,
                            merch = merchEntries,
                            header = header,
                            eventSink = eventSink,
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun SheetContent(state: State, bottomSheetState: SheetState) {
        when (state.tab()) {
            EntryTab.ARTISTS -> ArtistSortFilterSheetContent(
                state = state.artistsSortFilterState,
                sheetState = bottomSheetState,
                showHideFavorited = false,
            )
            EntryTab.RALLIES -> StampRallySortFilterSheetContent(
                state = state.ralliesSortFilterState,
                sheetState = bottomSheetState,
                showHideFavorited = false,
            )
            EntryTab.SERIES -> SeriesSortFilterSheetContent(
                state = state.seriesSortFilterState,
                sheetState = bottomSheetState,
            )
            // TODO: Real merch sort filter?
            EntryTab.MERCH -> Box {}
        }
    }

    @Composable
    private fun ArtistContent(
        state: State,
        sortFilterState: ArtistSortFilterState,
        baseContentPadding: () -> PaddingValues,
        entries: LazyPagingItems<ArtistEntryGridModel>,
        series: () -> Map<String, GetSeriesTitles>,
        eventSink: (Event) -> Unit,
        header: @Composable () -> Unit,
        noResultsItem: @Composable () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        ArtistSearchScreenContent(
            sortFilterState = sortFilterState,
            baseContentPadding = baseContentPadding,
            header = header,
            entries = entries,
            series = series,
            unfilteredCount = state.artistsUnfilteredCount,
            displayType = state.displayType,
            eventSink = { eventSink(Event.ArtistSearchEvent(it)) },
            noResultsItem = noResultsItem,
            modifier = modifier,
        )
    }

    @Composable
    private fun RalliesContent(
        state: State,
        sortFilterState: StampRallySortFilterState,
        baseContentPadding: () -> PaddingValues,
        entries: LazyPagingItems<StampRallyWithUserData>,
        eventSink: (Event) -> Unit,
        header: @Composable () -> Unit,
        noResultsItem: @Composable () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        StampRallySearchScreenContent(
            sortFilterState = sortFilterState,
            baseContentPadding = baseContentPadding,
            header = header,
            entries = entries,
            unfilteredCount = state.ralliesUnfilteredCount,
            displayType = state.displayType,
            onEvent = { eventSink(Event.StampRallySearchEvent(it)) },
            noResultsItem = noResultsItem,
            modifier = modifier,
        )
    }

    @Composable
    private fun SeriesContent(
        baseContentPadding: () -> PaddingValues,
        series: LazyPagingItems<SeriesWithUserData>,
        header: @Composable () -> Unit,
        eventSink: (Event) -> Unit,
    ) {
        val listState = rememberLazyListState()
        VerticalScrollableWithBar(
            scrollbarState = rememberScrollbarState(listState),
        ) { horizontalContentPadding ->
            LazyColumn(
                state = listState,
                contentPadding = baseContentPadding() + PaddingValues(
                    start = horizontalContentPadding,
                    end = horizontalContentPadding,
                    bottom = 80.dp
                ),
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item("header") { header() }

                if (series.itemCount == 0) {
                    if (series.loadState.refresh is LoadState.Loading) {
                        item("loadingIndicator") {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                InfiniteProgressIndicator()
                            }
                        }
                    } else {
                        item("noResults") {
                            NoResultsItem(
                                tab = EntryTab.SERIES,
                                eventSink = eventSink,
                            )
                        }
                    }
                } else {
                    items(
                        count = series.itemCount,
                        key = series.itemKey { it.series.id },
                        contentType = series.itemContentType { "series" },
                    ) { index ->
                        val data = series[index]
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SeriesRow(
                                data = data,
                                textStyle = LocalTextStyle.current,
                                onFavoriteToggle = {
                                    if (data != null) {
                                        eventSink(Event.SeriesFavoriteToggle(data, it))
                                    }
                                },
                                onClick = {
                                    data?.let {
                                        eventSink(Event.OpenSeries(it.series.id))
                                    }
                                },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MerchContent(
        baseContentPadding: () -> PaddingValues,
        merch: LazyPagingItems<MerchWithUserData>,
        header: @Composable () -> Unit,
        eventSink: (Event) -> Unit,
    ) {
        val listState = rememberLazyListState()
        VerticalScrollableWithBar(
            scrollbarState = rememberScrollbarState(listState),
        ) { horizontalContentPadding ->
            LazyColumn(
                state = listState,
                contentPadding = baseContentPadding() + PaddingValues(
                    start = horizontalContentPadding,
                    end = horizontalContentPadding,
                    bottom = 80.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                item("header") { header() }

                if (merch.itemCount == 0) {
                    if (merch.loadState.refresh is LoadState.Loading) {
                        item("loadingIndicator") {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                InfiniteProgressIndicator()
                            }
                        }
                    } else {
                        item("noResults") {
                            NoResultsItem(
                                tab = EntryTab.MERCH,
                                eventSink = eventSink,
                            )
                        }
                    }
                } else {
                    items(
                        count = merch.itemCount,
                        key = merch.itemKey { it.merch.name },
                        contentType = merch.itemContentType { "merch" },
                    ) { index ->
                        val data = merch[index]
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MerchRow(
                                data = data,
                                showNotes = false,
                                onFavoriteToggle = {
                                    if (data != null) {
                                        eventSink(Event.MerchFavoriteToggle(data, it))
                                    }
                                },
                                onClick = {
                                    if (data != null) {
                                        eventSink(Event.OpenMerch(data.merch.name))
                                    }
                                },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Header(
        tab: () -> EntryTab,
        onTabChange: (EntryTab) -> Unit,
        dataYearHeaderState: DataYearHeaderState,
        isLoggedIn: () -> Boolean,
        onEvent: (Event) -> Unit,
    ) {
        Column {
            DataYearHeader(
                state = dataYearHeaderState,
                onOpenChangelog = { onEvent(Event.OpenChangelog) },
                onOpenSettings = { onEvent(Event.OpenSettings) },
            ) {
                IconButton(onClick = { onEvent(Event.OpenExport(dataYearHeaderState.year)) }) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = stringResource(Res.string.alley_con_upcoming_show_qr),
                    )
                }
            }
            val tab = tab()
            PrimaryScrollableTabRow(EntryTab.entries.indexOf(tab)) {
                EntryTab.entries.forEach {
                    Tab(
                        selected = tab == it,
                        text = { Text(text = stringResource(it.text)) },
                        onClick = { onTabChange(it) },
                    )
                }
            }

            if (!isLoggedIn()) {
                Spacer(Modifier.height(4.dp))
                OutlinedCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            stringResource(Res.string.alley_user_login_prompt),
                            modifier = Modifier.weight(1f)
                        )
                        FilledTonalButton(
                            icon = Logo.DISCORD.icon,
                            text = stringResource(Res.string.alley_user_login_action),
                            onClick = { onEvent(Event.Login) },
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun NoResultsItem(
        tab: EntryTab,
        eventSink: (Event) -> Unit,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            val textRes = when (tab) {
                EntryTab.ARTISTS -> Res.string.alley_favorites_empty_artists
                EntryTab.RALLIES -> Res.string.alley_favorites_empty_stamp_rallies
                EntryTab.SERIES -> Res.string.alley_favorites_empty_series
                EntryTab.MERCH -> Res.string.alley_favorites_empty_merch
            }
            Text(
                text = stringResource(textRes),
                modifier = Modifier.padding(16.dp)
            )
            FilledTonalButton(onClick = {
                when (tab) {
                    EntryTab.ARTISTS -> eventSink(Event.NavigateToArtists)
                    EntryTab.RALLIES -> eventSink(Event.NavigateToRallies)
                    EntryTab.SERIES -> eventSink(Event.NavigateToSeries)
                    EntryTab.MERCH -> eventSink(Event.NavigateToMerch)
                }
            }) {
                val buttonTextRes = when (tab) {
                    EntryTab.ARTISTS -> Res.string.alley_favorites_empty_go_to_artists
                    EntryTab.RALLIES -> Res.string.alley_favorites_empty_go_to_stamp_rallies
                    EntryTab.SERIES -> Res.string.alley_favorites_empty_go_to_series
                    EntryTab.MERCH -> Res.string.alley_favorites_empty_go_to_merch
                }
                Text(stringResource(buttonTextRes))
            }
        }
    }

    @Serializable
    enum class EntryTab(val text: StringResource) {
        ARTISTS(Res.string.alley_favorites_artists),
        RALLIES(Res.string.alley_favorites_rallies),
        SERIES(Res.string.alley_favorites_series),
        MERCH(Res.string.alley_favorites_merch),
    }

    @Stable
    class State(
        val randomSeed: Int,
        val tab: () -> EntryTab,
        val onTabChange: (EntryTab) -> Unit,
        val query: TextFieldState,
        val displayType: () -> SearchDisplayType,
        val year: () -> DataYear,
        val onYearChange: (DataYear) -> Unit,
        val isLoggedIn: () -> Boolean,
        val artistsEntries: Flow<PagingData<ArtistEntryGridModel>>,
        val artistsSortFilterState: ArtistSortFilterState,
        val artistsUnfilteredCount: () -> Int,
        val ralliesEntries: Flow<PagingData<StampRallyWithUserData>>,
        val ralliesSortFilterState: StampRallySortFilterState,
        val ralliesUnfilteredCount: () -> Int,
        val seriesEntries: Flow<PagingData<SeriesWithUserData>>,
        val seriesSortFilterState: SeriesSortFilterState,
        val merchEntries: Flow<PagingData<MerchWithUserData>>,
    )

    sealed interface Event {
        data class ArtistSearchEvent(val event: ArtistSearchScreen.Event) : Event
        data class StampRallySearchEvent(val event: StampRallySearchScreen.Event) : Event
        data class OpenSeries(val series: String) : Event
        data class OpenMerch(val merch: String) : Event
        data class OpenExport(val dataYear: DataYear) : Event
        data object OpenChangelog : Event
        data object OpenSettings : Event
        data object NavigateToArtists : Event
        data object NavigateToRallies : Event
        data object NavigateToSeries : Event
        data object NavigateToMerch : Event
        data class SeriesFavoriteToggle(
            val series: SeriesWithUserData,
            val favorite: Boolean,
        ) : Event

        data class MerchFavoriteToggle(
            val merch: MerchWithUserData,
            val favorite: Boolean,
        ) : Event

        data object Login : Event
    }
}
