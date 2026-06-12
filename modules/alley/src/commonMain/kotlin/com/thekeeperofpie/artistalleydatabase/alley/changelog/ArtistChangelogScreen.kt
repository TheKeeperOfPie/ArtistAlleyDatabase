package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_catalogs_only
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_catalogs_only_warning
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_title
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

internal object ArtistChangelogScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        route: AlleyDestination.ArtistChangelog,
        onClickBack: () -> Unit,
        onClickArtist: (ArtistChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickImage: (ArtistChangelogEntry, CatalogImage) -> Unit,
        viewModel: ArtistChangelogViewModel = viewModel {
            graph.artistChangelogViewModelFactory.create(createSavedStateHandle())
        },
    ) {
        val changes by viewModel.changes.collectAsStateWithLifecycle()
        val seriesTitles by viewModel.seriesEntryCache.series.collectAsStateWithLifecycle()
        var catalogsOnly by viewModel.catalogsOnly.collectAsMutableStateWithLifecycle()
        ArtistChangelogScreen(
            changes = { changes },
            seriesTitles = { seriesTitles },
            catalogsOnly = { catalogsOnly },
            onChangeCatalogsOnly = { catalogsOnly = it },
            onClickBack = onClickBack,
            onClickArtist = onClickArtist,
            onClickSeries = onClickSeries,
            onClickMerch = onClickMerch,
            onClickImage = onClickImage,
        )
    }

    @Composable
    operator fun invoke(
        changes: () -> List<DayChange>,
        seriesTitles: () -> Map<String, GetSeriesTitles>,
        catalogsOnly: () -> Boolean,
        onChangeCatalogsOnly: (Boolean) -> Unit,
        onClickBack: () -> Unit,
        onClickArtist: (ArtistChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickImage: (ArtistChangelogEntry, CatalogImage) -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    title = { Text(stringResource(Res.string.alley_changelog_title)) },
                )
            },
        ) {
            val listState = rememberLazyListState()
            val scrollAreaState = rememberScrollAreaState(listState)
            ScrollArea(state = scrollAreaState, modifier = Modifier.fillMaxSize().padding(it)) {
                Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 200.dp),
                        modifier = Modifier.widthIn(max = 960.dp)
                    ) {
                        item("changelogFilterHeader") {
                            FilterHeader(
                                catalogsOnly = { catalogsOnly() },
                                onChangeCatalogsOnly = onChangeCatalogsOnly,
                            )
                        }

                        changes().forEach {
                            changelogDayHeader(listState, it.date)
                            artistChangelogDay(
                                date = it.date,
                                added = it.added,
                                updated = it.updated,
                                seriesTitles = seriesTitles,
                                onClickArtist = onClickArtist,
                                onClickSeries = onClickSeries,
                                onClickMerch = onClickMerch,
                                onClickImage = onClickImage,
                            )
                        }
                    }

                    PrimaryVerticalScrollbar(listState)
                }
            }
        }
    }

    @Composable
    fun FilterHeader(catalogsOnly: () -> Boolean, onChangeCatalogsOnly: (Boolean) -> Unit) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                FilterChip(
                    selected = catalogsOnly(),
                    label = {
                        Text(stringResource(Res.string.alley_changelog_catalogs_only))
                    },
                    onClick = { onChangeCatalogsOnly(!catalogsOnly()) },
                )
            }

            if (catalogsOnly()) {
                Text(
                    text = stringResource(Res.string.alley_changelog_catalogs_only_warning),
                )
            }
        }
    }

    data class DayChange(
        val date: LocalDate,
        val added: List<ArtistChangelogEntry>,
        val updated: List<ArtistChangelogEntry>,
    )
}
