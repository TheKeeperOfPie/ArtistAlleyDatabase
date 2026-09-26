package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.annotation.RememberInComposition
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_tags_filter_chip_state_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_tags_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_tags_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_filter_advanced
import artistalleydatabase.modules.alley.generated.resources.alley_filter_advanced_expand_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_filter_force_one_display_column
import artistalleydatabase.modules.alley.generated.resources.alley_filter_hide_favorited
import artistalleydatabase.modules.alley.generated.resources.alley_filter_hide_ignored
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_grid_by_default
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_confirmed_tags
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_outdated_catalogs
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_random_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_filter_chip_state_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_sort_label
import artistalleydatabase.modules.utils_compose.generated.resources.clear
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterSaveableState.Section
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkTagEntry
import com.thekeeperofpie.artistalleydatabase.alley.links.textRes
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryProvider
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagData
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagSection2
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesFilterSection
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesFilterState
import com.thekeeperofpie.artistalleydatabase.alley.tags.textRes
import com.thekeeperofpie.artistalleydatabase.alley.ui.assertInPreview
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CommissionType
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LinkCategory
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.category
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.toggle
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterSection2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.IncludeExcludeIcon
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SectionGroup
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SectionsExpandIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffoldSheetContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SwitchSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagSection2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.SnapshotStateSetSerializer
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.rememberSerializable
import com.thekeeperofpie.artistalleydatabase.utils_preview.AlleyPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.utils_compose.generated.resources.Res as UtilsComposeRes

@Composable
internal fun ArtistSortFilterSheetContent(
    state: ArtistSortFilterState,
    sheetState: SheetState,
    seriesAutocompleteResults: () -> List<SeriesInfo>,
    scrollState: ScrollState = rememberScrollState(),
    showHideFavorited: Boolean = true,
) {
    SortFilterBottomScaffoldSheetContent(
        sheetState = sheetState,
        scrollState = scrollState,
        actions = {
            val expandedSections = state.saveableState.expandedSections
            val allSectionsExpanded by remember(expandedSections) {
                derivedStateOf {
                    expandedSections.containsAll(Section.entries)
                }
            }
            SectionsExpandIndicator(
                allSectionsExpanded = { allSectionsExpanded },
                onSectionsExpandedChanged = {
                    if (it) {
                        expandedSections.addAll(Section.entries)
                    } else {
                        expandedSections.clear()
                    }
                },
                activatedCount = { state.activeCount },
                targetValue = { sheetState.targetValue },
            )
        },
        footer = {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { state.clear() }) {
                    Text(text = stringResource(UtilsComposeRes.string.clear))
                }
            }
        }
    ) {
        val saveableState = state.saveableState
        val persistentState = state.persistentState
        var sortOption by persistentState.sortOption.collectAsMutableStateWithLifecycle()
        var sortAscending by persistentState.sortAscending.collectAsMutableStateWithLifecycle()
        SortSection(
            header = { Text(stringResource(Res.string.alley_sort_label)) },
            expanded = { Section.SORT in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.SORT) },
            sortOptions = { ArtistSearchSortOption.entries },
            sortOption = { sortOption },
            onSortChanged = { sortOption = it },
            sortOptionLabel = { Text(stringResource(it.textRes)) },
            sortAscending = { sortAscending },
            onSortAscendingChange = { sortAscending = it },
        )

        HorizontalDivider()

        var showOnlyConfirmedTags by persistentState.showOnlyConfirmedTags.collectAsMutableStateWithLifecycle()
        SeriesFilterSection(
            expanded = { Section.SERIES in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.SERIES) },
            state = saveableState.series,
            lockedSeries = state.lockedSeries,
            autocompleteResults = seriesAutocompleteResults,
            showOnlyConfirmedTagsSection = {
                ShowOnlyConfirmedTagsSection(
                    enabled = { showOnlyConfirmedTags },
                    onEnabledChanged = { showOnlyConfirmedTags = it },
                    modifier = Modifier.padding(start = 32.dp)
                )
            },
        )

        HorizontalDivider()

        MerchTagSection2(
            expanded = { Section.MERCH in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.MERCH) },
            state = saveableState.merch,
            merchTagData = state.merchTagData,
            merchIdsLockedIn = state.merchIdsLockedIn,
            header = {
                ShowOnlyConfirmedTagsSection(
                    enabled = { showOnlyConfirmedTags },
                    onEnabledChanged = { showOnlyConfirmedTags = it },
                    modifier = Modifier.padding(start = 32.dp)
                )
            },
        )

        HorizontalDivider()

        FilterSection2(
            expanded = { Section.COMMISSIONS in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.COMMISSIONS) },
            options = CommissionType.entries,
            state = saveableState.commissions,
            selectionMethod = SortFilterSectionState.Filter.SelectionMethod.ONLY_INCLUDE_WITH_EXCLUSIVE_FIRST,
            title = { Text(stringResource(Res.string.alley_commission_type_filter_label)) },
            titleDropdownContentDescriptionRes = Res.string.alley_commission_type_filter_content_description,
            optionLabel = { Text(stringResource(it.textRes)) },
        )

        HorizontalDivider()

        var artistTagsIn by persistentState.artistTagsIn.collectAsMutableStateWithLifecycle()
        var artistTagsNotIn by persistentState.artistTagsNotIn.collectAsMutableStateWithLifecycle()
        FilterSection2(
            expanded = { Section.ARTIST_TAGS in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.ARTIST_TAGS) },
            options = ArtistTag.entries,
            filterIn = { artistTagsIn },
            filterNotIn = { artistTagsNotIn },
            title = { Text(stringResource(Res.string.alley_artist_tags_filter_label)) },
            titleDropdownContentDescriptionRes = Res.string.alley_artist_tags_filter_content_description,
            onOptionClick = {
                val (newFilterIn, newFilterNotIn) = FilterSectionState.onClick(
                    options = ArtistTag.entries,
                    selectionMethod = SortFilterSectionState.Filter.SelectionMethod.ALLOW_EXCLUDE,
                    filterIn = artistTagsIn,
                    filterNotIn = artistTagsNotIn,
                    filter = it,
                )
                Snapshot.withMutableSnapshot {
                    artistTagsIn = newFilterIn
                    artistTagsNotIn = newFilterNotIn
                }
            },
            optionLabel = { Text(stringResource(it.textRes)) },
            optionLeadingIcon = { _, enabled ->
                IncludeExcludeIcon(
                    enabled = enabled,
                    contentDescriptionRes = Res.string.alley_artist_tags_filter_chip_state_content_description,
                )
            }
        )

        HorizontalDivider()

        LinksSection(
            expanded = { Section.LINKS in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.LINKS) },
            state = saveableState.links,
        )

        HorizontalDivider()

        AdvancedSection(
            expanded = { Section.ADVANCED in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.ADVANCED) },
            state = saveableState,
            persistentState = persistentState,
            showHideFavorited = showHideFavorited,
        )

        HorizontalDivider()
    }
}

@Composable
private fun AdvancedSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    state: ArtistSortFilterSaveableState,
    persistentState: ArtistSortFilterPersistentState,
    showHideFavorited: Boolean,
) {
    SectionGroup(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        header = { Text(stringResource(Res.string.alley_filter_advanced)) },
        headerDropdownContentDescriptionRes = Res.string.alley_filter_advanced_expand_content_description,
    ) { expandedState ->
        if (expandedState) {
            var showGridByDefault by persistentState.showGridByDefault.collectAsMutableStateWithLifecycle()
            SwitchSection(
                title = { Text(stringResource(Res.string.alley_filter_show_grid_by_default)) },
                enabled = { showGridByDefault },
                onEnabledChanged = { showGridByDefault = it },
            )

            HorizontalDivider()

            var showRandomCatalogImage by persistentState.showRandomCatalogImage.collectAsMutableStateWithLifecycle()
            SwitchSection(
                title = { Text(stringResource(Res.string.alley_filter_show_random_catalog_image)) },
                enabled = { showRandomCatalogImage },
                onEnabledChanged = { showRandomCatalogImage = it },
            )

            HorizontalDivider()

            var showOnlyConfirmedTags by persistentState.showOnlyConfirmedTags.collectAsMutableStateWithLifecycle()
            ShowOnlyConfirmedTagsSection(
                enabled = { showOnlyConfirmedTags },
                onEnabledChanged = { showOnlyConfirmedTags = it },
            )

            HorizontalDivider()

            var showOutdatedCatalogs by persistentState.showOutdatedCatalogs.collectAsMutableStateWithLifecycle()
            SwitchSection(
                title = { Text(stringResource(Res.string.alley_filter_show_outdated_catalogs)) },
                enabled = { showOutdatedCatalogs },
                onEnabledChanged = { showOutdatedCatalogs = it },
            )

            HorizontalDivider()

            var forceOneDisplayColumn by persistentState.forceOneDisplayColumn.collectAsMutableStateWithLifecycle()
            SwitchSection(
                title = { Text(stringResource(Res.string.alley_filter_force_one_display_column)) },
                enabled = { forceOneDisplayColumn },
                onEnabledChanged = { forceOneDisplayColumn = it },
            )

            HorizontalDivider()
        }

        if (showHideFavorited && (expandedState || state.hideFavorited)) {
            SwitchSection(
                title = { Text(stringResource(Res.string.alley_filter_hide_favorited)) },
                enabled = { state.hideFavorited },
                onEnabledChanged = { state.hideFavorited = it },
            )
        }

        if (showHideFavorited && (expandedState || (state.hideFavorited && !state.hideIgnored))) {
            HorizontalDivider()
        }

        if (expandedState || state.hideIgnored) {
            SwitchSection(
                title = { Text(stringResource(Res.string.alley_filter_hide_ignored)) },
                enabled = { state.hideIgnored },
                onEnabledChanged = { state.hideIgnored = it },
            )
        }
    }
}

@Composable
internal fun ShowOnlyConfirmedTagsSection(
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

private val linkTypes =
    Link.Type.entries.groupBy { it.category }
        .mapValues { (category, types) ->
            if (category == LinkCategory.OTHER) {
                LinkTagEntry.Tag(Link.Type.OTHER_NON_STORE)
            } else {
                LinkTagEntry.Category(
                    category = category,
                    children = types.associate { it.name to LinkTagEntry.Tag(it) },
                )
            } as TagEntry
        }
        .map { it.key.name to it.value }
        .sortedBy { it.first }

@Composable
private fun LinksSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    state: TagSectionState,
) {
    TagSection2(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        sectionHeader = { Text(stringResource(Res.string.alley_link_type_filter_label)) },
        sectionHeaderDropdownContentDescriptionRes = Res.string.alley_link_type_filter_content_description,
        tags = linkTypes,
        state = state,
        showSearch = false,
        showRootTagsWhenNotExpanded = false,
        showRootTagsAtBottom = true,
        categoryToName = { stringResource((it as LinkTagEntry.Category).category.textRes) },
        tagChip = { linkTag, selected, enabled, modifier ->
            FilterChip(
                selected = selected,
                onClick = {
                    val linkTypeId = linkTag.id
                    state.tagIdIn.toggle(linkTypeId)
                },
                enabled = enabled,
                label = {
                    AutoHeightText(stringResource((linkTag as LinkTagEntry.Tag).type.textRes))
                },
                leadingIcon = {
                    IncludeExcludeIcon(
                        enabled = if (state.tagIdIn.contains(linkTag.id)) true else null,
                        contentDescriptionRes = Res.string.alley_link_type_filter_chip_state_content_description,
                    )
                },
                modifier = modifier
            )
        }
    )
}

class ArtistSortFilterState(
    val persistentState: ArtistSortFilterPersistentState,
    val saveableState: ArtistSortFilterSaveableState,
    val lockedSeries: () -> SeriesInfo?,
    val merchTagData: () -> MerchTagData,
    val merchIdsLockedIn: () -> Set<String>,
) {
    val activeCount: Int
        @Composable get() {
            return persistentState.activeCount + saveableState.activeCount
        }

    fun clear() {
        Snapshot.withMutableSnapshot {
            persistentState.clear()
            saveableState.clear()
        }
    }

    companion object {
        @Composable
        fun rememberForPreview(): ArtistSortFilterState {
            assertInPreview()
            val persistentState = ArtistSortFilterPersistentState.rememberForPreview()
            val saveableState = rememberSerializable { ArtistSortFilterSaveableState() }
            return remember(persistentState, saveableState) {
                ArtistSortFilterState(
                    persistentState = persistentState,
                    saveableState = saveableState,
                    lockedSeries = { null },
                    merchTagData = { MerchTagData(MerchEntryProvider.values.toList()) },
                    merchIdsLockedIn = { emptySet() },
                )
            }
        }
    }
}

/** Everything backed by an externally owned source */
class ArtistSortFilterPersistentState(
    val sortOption: MutableStateFlow<ArtistSearchSortOption>,
    val sortAscending: MutableStateFlow<Boolean>,
    val artistTagsIn: MutableStateFlow<Set<ArtistTag>>,
    val artistTagsNotIn: MutableStateFlow<Set<ArtistTag>>,
    val showGridByDefault: MutableStateFlow<Boolean>,
    val showRandomCatalogImage: MutableStateFlow<Boolean>,
    val showOnlyConfirmedTags: MutableStateFlow<Boolean>,
    val showOutdatedCatalogs: MutableStateFlow<Boolean>,
    val forceOneDisplayColumn: MutableStateFlow<Boolean>,
) {
    val activeCount: Int
        @Composable get() {
            // Sort and advanced options are not reset on clear and thus don't count as active
            var count = 0
            if (artistTagsIn.collectAsState().value.isNotEmpty()) count++
            if (artistTagsNotIn.collectAsState().value.isNotEmpty()) count++
            return count
        }

    fun clear() {
        artistTagsIn.value = emptySet()
        artistTagsNotIn.value = emptySet()
    }

    companion object {
        @Composable
        fun rememberForPreview(): ArtistSortFilterPersistentState {
            assertInPreview()
            return ArtistSortFilterPersistentState(
                sortOption = MutableStateFlow(ArtistSearchSortOption.RANDOM),
                sortAscending = MutableStateFlow(true),
                artistTagsIn = MutableStateFlow(setOf(ArtistTag.HAS_CATALOG)),
                artistTagsNotIn = MutableStateFlow(setOf(ArtistTag.VERIFIED)),
                showGridByDefault = MutableStateFlow(false),
                showRandomCatalogImage = MutableStateFlow(true),
                showOnlyConfirmedTags = MutableStateFlow(false),
                showOutdatedCatalogs = MutableStateFlow(true),
                forceOneDisplayColumn = MutableStateFlow(false),
            )
        }
    }
}

@Serializable
class ArtistSortFilterSaveableState @RememberInComposition constructor(
    val series: SeriesFilterState = SeriesFilterState(),
    val merch: TagSectionState = TagSectionState(),
    val commissions: FilterSectionState<CommissionType> = FilterSectionState(),
    val links: TagSectionState = TagSectionState(),
    @Serializable(SnapshotStateSetSerializer::class)
    val expandedSections: SnapshotStateSet<Section> = mutableStateSetOf(),
    // TODO: Store hides persistently?
    @Serializable(MutableStateSerializer::class)
    private val _hideFavorited: MutableState<Boolean> = mutableStateOf(false),
    @Serializable(MutableStateSerializer::class)
    private val _hideIgnored: MutableState<Boolean> = mutableStateOf(false),
) {
    var hideFavorited by _hideFavorited
    var hideIgnored by _hideIgnored

    val activeCount by derivedStateOf {
        var count = 0
        if (!series.isDefault) count++
        if (!merch.isDefault) count++
        if (!commissions.isDefault) count++
        if (!links.isDefault) count++
        if (hideFavorited) count++
        if (hideIgnored) count++
        count
    }

    fun clear() {
        Snapshot.withMutableSnapshot {
            series.clear()
            merch.clear()
            commissions.clear()
            links.clear()
            hideFavorited = false
            hideIgnored = false
        }
    }

    enum class Section {
        SORT,
        SERIES,
        MERCH,
        COMMISSIONS,
        ARTIST_TAGS,
        LINKS,
        ADVANCED,
    }
}

@Composable
private fun ArtistSortFilterSheetContentPreview(
    state: ArtistSortFilterState,
    scrollState: ScrollState = rememberScrollState(),
) {
    ArtistSortFilterSheetContent(
        state = state,
        scrollState = scrollState,
        sheetState = rememberBottomSheetState(SheetValue.PartiallyExpanded),
        seriesAutocompleteResults = { emptyList() },
    )
}

@AlleyPreview
@Composable
private fun ArtistSortFilterSheetContentPreview() {
    ArtistSortFilterSheetContentPreview(ArtistSortFilterState.rememberForPreview())
}

@AlleyPreview
@Composable
private fun ArtistSortFilterSheetContentExpandedPreview0() {
    val state = ArtistSortFilterState.rememberForPreview().apply {
        saveableState.expandedSections.addAll(Section.entries.take(4))
    }
    ArtistSortFilterSheetContentPreview(state)
}

@AlleyPreview
@Composable
private fun ArtistSortFilterSheetContentExpandedPreview1() {
    val scrollState = rememberScrollState(1000)
    val state = ArtistSortFilterState.rememberForPreview().apply {
        saveableState.expandedSections.addAll(Section.entries.drop(4))
    }
    ArtistSortFilterSheetContentPreview(state, scrollState)
}
