@file:OptIn(ExperimentalComposeUiApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.annotation.RememberInComposition
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
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
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_search_clear_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_search_placeholder
import artistalleydatabase.modules.alley.generated.resources.alley_sort_label
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterSaveableState.Section
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkTagEntry
import com.thekeeperofpie.artistalleydatabase.alley.links.textRes
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryProvider
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagData
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagSection2
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagSectionState
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesAutocompleteSection.SeriesFilterEntry
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.series.ui.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.textRes
import com.thekeeperofpie.artistalleydatabase.alley.ui.assertInPreview
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.Clear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CommissionType
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LinkCategory
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.category
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.toggle
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.CustomFilterSection
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
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.SnapshotStateSetSerializer
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.TextFieldStateSerializer
import com.thekeeperofpie.artistalleydatabase.utils_preview.AlleyPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ArtistSortFilterSheetContent(
    state: ArtistSortFilterState,
    sheetState: SheetState,
    seriesImage: (SeriesInfo) -> String?,
    seriesAutocompleteResults: () -> List<SeriesInfo>,
) {
    SortFilterBottomScaffoldSheetContent(
        sheetState = sheetState,
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
                activatedCount = { 0 }, // TODO
                targetValue = { sheetState.targetValue },
            )
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
            onSortClick = { sortOption = it },
            sortOptionLabel = { Text(stringResource(it.textRes)) },
            sortAscending = { sortAscending },
            onSortAscendingChange = { sortAscending = it },
        )

        HorizontalDivider()

        var showOnlyConfirmedTags by persistentState.showOnlyConfirmedTags.collectAsMutableStateWithLifecycle()
        SeriesSection(
            expanded = { Section.SERIES in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.SERIES) },
            state = saveableState.series,
            image = seriesImage,
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
            linksState = saveableState.links,
        )

        HorizontalDivider()

        AdvancedSection(
            expanded = { Section.ADVANCED in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.ADVANCED) },
            state = saveableState,
            persistentState = persistentState,
        )

        HorizontalDivider()

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun AdvancedSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    state: ArtistSortFilterSaveableState,
    persistentState: ArtistSortFilterPersistentState,
) {
    SectionGroup(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        header = { Text(stringResource(Res.string.alley_filter_advanced)) },
        headerDropdownContentDescriptionRes = Res.string.alley_filter_advanced_expand_content_description,
    ) {
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

        SwitchSection(
            title = { Text(stringResource(Res.string.alley_filter_hide_favorited)) },
            enabled = { state.hideFavorited },
            onEnabledChanged = { state.hideFavorited = it },
        )

        HorizontalDivider()
        SwitchSection(
            title = { Text(stringResource(Res.string.alley_filter_hide_ignored)) },
            enabled = { state.hideIgnored },
            onEnabledChanged = { state.hideIgnored = it },
        )

        HorizontalDivider()

        var forceOneDisplayColumn by persistentState.forceOneDisplayColumn.collectAsMutableStateWithLifecycle()
        SwitchSection(
            title = { Text(stringResource(Res.string.alley_filter_force_one_display_column)) },
            enabled = { forceOneDisplayColumn },
            onEnabledChanged = { forceOneDisplayColumn = it },
        )
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

@Composable
private fun SeriesSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    state: ArtistSortFilterSaveableState.SeriesState,
    image: (SeriesInfo) -> String?,
    lockedSeries: () -> SeriesInfo?,
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
                lockedSeries()?.let {
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
    linksState: ArtistSortFilterSaveableState.LinksState,
) {
    val tagsState = linksState.links
    TagSection2(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        sectionHeader = { Text(stringResource(Res.string.alley_link_type_filter_label)) },
        sectionHeaderDropdownContentDescriptionRes = Res.string.alley_link_type_filter_content_description,
        tags = linkTypes,
        state = tagsState,
        showSearch = false,
        showRootTagsWhenNotExpanded = false,
        showRootTagsAtBottom = true,
        categoryToName = { stringResource((it as LinkTagEntry.Category).category.textRes) },
        tagChip = { linkTag, selected, enabled, modifier ->
            FilterChip(
                selected = selected,
                onClick = {
                    val linkTypeId = linkTag.id
                    tagsState.tagIdIn.toggle(linkTypeId)
                },
                enabled = enabled,
                label = {
                    AutoHeightText(stringResource((linkTag as LinkTagEntry.Tag).type.textRes))
                },
                leadingIcon = {
                    IncludeExcludeIcon(
                        enabled = if (tagsState.tagIdIn.contains(linkTag.id)) true else null,
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
            val saveableState = rememberSaveable { ArtistSortFilterSaveableState() }
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
    fun clear() {
        showGridByDefault.value = false
        showRandomCatalogImage.value = false
        showOnlyConfirmedTags.value = false
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
    val series: SeriesState = SeriesState(),
    val merch: MerchTagSectionState = MerchTagSectionState(),
    val commissions: FilterSectionState<CommissionType> = FilterSectionState(),
    val links: LinksState = LinksState(),
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

    @Serializable
    class SeriesState @RememberInComposition constructor(
        @Serializable(TextFieldStateSerializer::class)
        val query: TextFieldState = TextFieldState(),
        @Serializable(SnapshotStateListSerializer::class)
        val seriesIn: SnapshotStateList<SeriesFilterEntry> = mutableStateListOf(),
    ) {
        fun clear() {
            Snapshot.withMutableSnapshot {
                query.clearText()
                seriesIn.clear()
            }
        }
    }

    @Serializable
    class LinksState(
        val links: TagSectionState = TagSectionState(),
    ) {
        fun clear() {
            links.clear()
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
private fun ArtistSortFilterSheetContentPreview(state: ArtistSortFilterState) {
    ArtistSortFilterSheetContent(
        state = state,
        sheetState = rememberBottomSheetState(SheetValue.PartiallyExpanded),
        seriesImage = { null },
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
    Column(modifier = Modifier.verticalScroll(scrollState)) {
        val state = ArtistSortFilterState.rememberForPreview().apply {
            saveableState.expandedSections.addAll(Section.entries.drop(4))
        }
        ArtistSortFilterSheetContentPreview(state)
    }
}
