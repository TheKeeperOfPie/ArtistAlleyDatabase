@file:OptIn(ExperimentalComposeUiApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.annotation.RememberInComposition
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_tags_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_tags_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_confirmed_tags
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_search_clear_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_search_placeholder
import artistalleydatabase.modules.alley.generated.resources.alley_sort_label
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterState.Section
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryProvider
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagData
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagSection2
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagSectionState
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesAutocompleteSection.SeriesFilterEntry
import com.thekeeperofpie.artistalleydatabase.alley.series.ui.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.textRes
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.Clear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CommissionType
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.toggle
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.CustomFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterSection2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SwitchSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.SnapshotStateSetSerializer
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.TextFieldStateSerializer
import com.thekeeperofpie.artistalleydatabase.utils_preview.AlleyPreview
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ArtistSortFilterSheetContent(
    state: ArtistSortFilterState,
    seriesImage: (SeriesInfo) -> String?,
    seriesAutocompleteResults: () -> List<SeriesInfo>,
    merchTagData: () -> MerchTagData,
) {
    Column {
        SortSection(
            header = { Text(stringResource(Res.string.alley_sort_label)) },
            expanded = { Section.SORT in state.expandedSections },
            onExpandedChange = { state.expandedSections.toggle(Section.SORT) },
            sortOptions = { ArtistSearchSortOption.entries },
            sortOption = { state.sortOption },
            onSortClick = { state.sortOption = it },
            sortOptionLabel = { Text(stringResource(it.textRes)) },
            sortAscending = { state.sortAscending },
            onSortAscendingChange = { state.sortAscending = it },
        )

        HorizontalDivider()

        SeriesSection(
            expanded = { Section.SERIES in state.expandedSections },
            onExpandedChange = { state.expandedSections.toggle(Section.SERIES) },
            state = state.series,
            image = seriesImage,
            autocompleteResults = seriesAutocompleteResults,
            showOnlyConfirmedTagsSection = {
                ShowOnlyConfirmedTagsSection(
                    enabled = { state.showOnlyConfirmedTags },
                    onEnabledChanged = { state.showOnlyConfirmedTags = it },
                    modifier = Modifier.padding(start = 32.dp)
                )
            },
        )

        HorizontalDivider()

        MerchTagSection2(
            expanded = { Section.MERCH in state.expandedSections },
            onExpandedChange = { state.expandedSections.toggle(Section.MERCH) },
            state = state.merch,
            merchTagData = merchTagData,
            header = {
                ShowOnlyConfirmedTagsSection(
                    enabled = { state.showOnlyConfirmedTags },
                    onEnabledChanged = { state.showOnlyConfirmedTags = it },
                    modifier = Modifier.padding(start = 32.dp)
                )
            },
        )

        HorizontalDivider()

        FilterSection2(
            expanded = { Section.COMMISSIONS in state.expandedSections },
            onExpandedChange = { state.expandedSections.toggle(Section.COMMISSIONS) },
            options = CommissionType.entries,
            state = state.commissions,
            selectionMethod = SortFilterSectionState.Filter.SelectionMethod.ONLY_INCLUDE_WITH_EXCLUSIVE_FIRST,
            title = { Text(stringResource(Res.string.alley_commission_type_filter_label)) },
            titleDropdownContentDescriptionRes = Res.string.alley_commission_type_filter_content_description,
            optionLabel = { Text(stringResource(it.textRes)) },
        )

        HorizontalDivider()

        FilterSection2(
            expanded = { Section.ARTIST_TAGS in state.expandedSections },
            onExpandedChange = { state.expandedSections.toggle(Section.ARTIST_TAGS) },
            options = ArtistTag.entries,
            state = state.artistTags,
            selectionMethod = SortFilterSectionState.Filter.SelectionMethod.ALLOW_EXCLUDE,
            title = { Text(stringResource(Res.string.alley_artist_tags_filter_label)) },
            titleDropdownContentDescriptionRes = Res.string.alley_artist_tags_filter_content_description,
            optionLabel = { Text(stringResource(it.textRes)) },
        )
    }
}

@Composable
private fun ShowOnlyConfirmedTagsSection(
    enabled: () -> Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SwitchSection(
        title = { Text(stringResource(Res.string.alley_filter_show_only_confirmed_tags)) },
        enabled = enabled,
        onEnabledChanged = onEnabledChanged,
        modifier = modifier,
    )
}

@Composable
private fun SeriesSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    state: ArtistSortFilterState.SeriesState,
    image: (SeriesInfo) -> String?,
    autocompleteResults: () -> List<SeriesInfo>,
    showOnlyConfirmedTagsSection: (@Composable () -> Unit)? = null,
) {
    val expanded = expanded()
    CustomFilterSection(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        header = { Text(stringResource(Res.string.alley_series_filter_label)) },
        headerDropdownContentDescriptionRes = Res.string.alley_series_filter_content_description,
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            if (expanded) {
                if (showOnlyConfirmedTagsSection != null) {
                    showOnlyConfirmedTagsSection()
                }
                var dropdownExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    TextField(
                        state = state.query,
                        placeholder = {
                            Text(text = stringResource(Res.string.alley_series_filter_search_placeholder))
                        },
                        trailingIcon = {
                            IconButton(onClick = { state.query.clearText() }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = stringResource(
                                        Res.string.alley_series_filter_search_clear_content_description
                                    ),
                                )
                            }
                        },
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                            .fillMaxWidth()
                            .padding(start = 32.dp, end = 16.dp)
                    )

                    val focused by interactionSource.collectIsFocusedAsState()
                    val focusManager = LocalFocusManager.current
                    val isImeVisible = WindowInsets.isImeVisibleKmp
                    BackHandler(enabled = focused && !isImeVisible) {
                        focusManager.clearFocus()
                    }

                    val autocompleteResults = autocompleteResults()
                    ExposedDropdownMenu(
                        expanded = focused && autocompleteResults.isNotEmpty(),
                        onDismissRequest = {
                            // This callback is invoked whenever the query changes,
                            // which makes it unusable if the user is typing
                            if (!isImeVisible) {
                                focusManager.clearFocus()
                            }
                        },
                    ) {
                        autocompleteResults.forEach {
                            DropdownMenuItem(
                                onClick = {
                                    state.seriesIn += SeriesFilterEntry(it)
                                    focusManager.clearFocus(true)
                                },
                                text = { SeriesRow(series = it, image = { image(it) }) },
                                contentPadding = PaddingValues(
                                    horizontal = 12.dp,
                                    vertical = 4.dp,
                                ),
                            )
                        }
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 16.dp)
                    .animateContentSize(),
            ) {
                state.lockedSeries?.let {
                    FilterChip(
                        selected = true,
                        enabled = false,
                        label = { AutoHeightText(it.name(LocalLanguageOptionMedia.current)) },
                        onClick = {},
                        modifier = Modifier.animateContentSize()
                    )
                }
                state.seriesIn.forEach {
                    FilterChip(
                        selected = true,
                        enabled = true,
                        label = { AutoHeightText(it.name(LocalLanguageOptionMedia.current)) },
                        onClick = { state.seriesIn -= it },
                        modifier = Modifier.animateContentSize()
                    )
                }
            }
        }
    }
}

@Suppress("CanBeParameter")
@Serializable
internal class ArtistSortFilterState @RememberInComposition constructor(
    private val _sortOption: MutableState<ArtistSearchSortOption> =
        mutableStateOf(ArtistSearchSortOption.RANDOM),
    private val _sortAscending: MutableState<Boolean> = mutableStateOf(true),
    private val _showOnlyConfirmedTags: MutableState<Boolean> = mutableStateOf(false),
    val series: SeriesState = SeriesState(),
    val merch: MerchTagSectionState = MerchTagSectionState(),
    val commissions: FilterSectionState<CommissionType> = FilterSectionState(),
    val artistTags: FilterSectionState<ArtistTag> = FilterSectionState(),
    @Serializable(with = SnapshotStateSetSerializer::class)
    val expandedSections: SnapshotStateSet<Section> = mutableStateSetOf(),
) {
    var sortOption by _sortOption
    var sortAscending by _sortAscending
    var showOnlyConfirmedTags by _showOnlyConfirmedTags

    fun clear() {
        Snapshot.withMutableSnapshot {
            showOnlyConfirmedTags = false
            series.clear()
        }
    }

    @Serializable
    class SeriesState @RememberInComposition constructor(
        @Serializable(with = TextFieldStateSerializer::class)
        val query: TextFieldState = TextFieldState(),
        val lockedSeries: SeriesFilterEntry? = null,
        @Serializable(with = SnapshotStateListSerializer::class)
        val seriesIn: SnapshotStateList<SeriesFilterEntry> = mutableStateListOf(),
    ) {
        fun clear() {
            Snapshot.withMutableSnapshot {
                query.clearText()
                seriesIn.clear()
            }
        }
    }

    enum class Section {
        SORT, SERIES, MERCH, COMMISSIONS, ARTIST_TAGS,
    }
}

@Composable
private fun ArtistSortFilterSheetContentPreview(state: ArtistSortFilterState) {
    ArtistSortFilterSheetContent(
        state = state,
        seriesImage = { null },
        seriesAutocompleteResults = { emptyList() },
        merchTagData = { MerchTagData(MerchEntryProvider.values.toList()) }
    )
}

@AlleyPreview
@Composable
private fun ArtistSortFilterSheetContentPreview() {
    ArtistSortFilterSheetContentPreview(remember { ArtistSortFilterState() })
}

@AlleyPreview
@Composable
private fun ArtistSortFilterSheetContentExpandedPreview0() {
    val state = remember { ArtistSortFilterState() }.apply {
        expandedSections.addAll(Section.entries.take(4))
    }
    ArtistSortFilterSheetContentPreview(state)
}

@AlleyPreview
@Composable
private fun ArtistSortFilterSheetContentExpandedPreview1() {
    val scrollState = rememberScrollState(1000)
    Column(modifier = Modifier.verticalScroll(scrollState)) {
        val state = remember { ArtistSortFilterState() }.apply {
            expandedSections.addAll(Section.entries.drop(4))
        }
        ArtistSortFilterSheetContentPreview(state)
    }
}
