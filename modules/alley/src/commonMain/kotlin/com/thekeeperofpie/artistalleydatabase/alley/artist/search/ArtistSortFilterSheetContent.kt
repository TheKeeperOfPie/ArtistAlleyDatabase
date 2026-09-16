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
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterState.Section
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkTagEntry
import com.thekeeperofpie.artistalleydatabase.alley.links.textRes
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
    persistentState: ArtistSortFilterPersistentState,
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

        HorizontalDivider()

        LinksSection(
            expanded = { Section.LINKS in state.expandedSections },
            onExpandedChange = { state.expandedSections.toggle(Section.LINKS) },
            linksState = state.links,
        )

        HorizontalDivider()

        AdvancedSection(
            expanded = { Section.ADVANCED in state.expandedSections },
            onExpandedChange = { state.expandedSections.toggle(Section.ADVANCED) },
            state = state,
            persistentState = persistentState,
        )
    }
}

@Composable
private fun AdvancedSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    state: ArtistSortFilterState,
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

        var showRandomCatalogImage by persistentState.showRandomCatalogImage.collectAsMutableStateWithLifecycle()
        SwitchSection(
            title = { Text(stringResource(Res.string.alley_filter_show_random_catalog_image)) },
            enabled = { showRandomCatalogImage },
            onEnabledChanged = { showRandomCatalogImage = it },
        )
        ShowOnlyConfirmedTagsSection(
            enabled = { state.showOnlyConfirmedTags },
            onEnabledChanged = { state.showOnlyConfirmedTags = it },
        )

        var showOutdatedCatalogs by persistentState.showOutdatedCatalogs.collectAsMutableStateWithLifecycle()
        SwitchSection(
            title = { Text(stringResource(Res.string.alley_filter_show_outdated_catalogs)) },
            enabled = { showOutdatedCatalogs },
            onEnabledChanged = { showOutdatedCatalogs = it },
        )
        SwitchSection(
            title = { Text(stringResource(Res.string.alley_filter_hide_favorited)) },
            enabled = { state.hideFavorited },
            onEnabledChanged = { state.hideFavorited = it },
        )
        SwitchSection(
            title = { Text(stringResource(Res.string.alley_filter_hide_ignored)) },
            enabled = { state.hideIgnored },
            onEnabledChanged = { state.hideIgnored = it },
        )

        var forceOneDisplayColumn by persistentState.forceOneDisplayColumn.collectAsMutableStateWithLifecycle()
        SwitchSection(
            title = { Text(stringResource(Res.string.alley_filter_force_one_display_column)) },
            enabled = { forceOneDisplayColumn },
            onEnabledChanged = { forceOneDisplayColumn = it },
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
    linksState: ArtistSortFilterState.LinksState,
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

/** Everything backed by an externally owned source */
class ArtistSortFilterPersistentState(
    val showGridByDefault: MutableStateFlow<Boolean>,
    val showRandomCatalogImage: MutableStateFlow<Boolean>,
    val showOutdatedCatalogs: MutableStateFlow<Boolean>,
    val forceOneDisplayColumn: MutableStateFlow<Boolean>,
) {
    fun clear() {
        showGridByDefault.value = false
        showRandomCatalogImage.value = false
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
    val links: LinksState = LinksState(),
    @Serializable(with = SnapshotStateSetSerializer::class)
    val expandedSections: SnapshotStateSet<Section> = mutableStateSetOf(),
    // TODO: Store hides persistently?
    private val _hideFavorited: MutableState<Boolean> = mutableStateOf(false),
    private val _hideIgnored: MutableState<Boolean> = mutableStateOf(false),
) {
    var sortOption by _sortOption
    var sortAscending by _sortAscending
    var showOnlyConfirmedTags by _showOnlyConfirmedTags

    var hideFavorited by _hideFavorited
    var hideIgnored by _hideIgnored

    fun clear() {
        Snapshot.withMutableSnapshot {
            showOnlyConfirmedTags = false
            series.clear()
            merch.clear()
            commissions.clear()
            artistTags.clear()
            links.clear()
            hideFavorited = false
            hideIgnored = false
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

    @Serializable
    internal class LinksState(
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
        persistentState = ArtistSortFilterPersistentState(
            showGridByDefault = MutableStateFlow(false),
            showRandomCatalogImage = MutableStateFlow(true),
            showOutdatedCatalogs = MutableStateFlow(true),
            forceOneDisplayColumn = MutableStateFlow(false),
        ),
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
