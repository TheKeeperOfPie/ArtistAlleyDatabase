package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_tags_filter_chip_state_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_tags_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_tags_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_filter_chip_state_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_exhibitor_tags_explanation
import artistalleydatabase.modules.alley.generated.resources.alley_exhibitor_tags_filter_chip_state_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_exhibitor_tags_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_exhibitor_tags_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_exhibitor_tags_icon_content_description
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
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkTagEntry
import com.thekeeperofpie.artistalleydatabase.alley.links.textRes
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchCache
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagData
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagSection
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesAutocompleteSection
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.alley.tags.textRes
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.Info
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.AnimeNycExhibitorTags
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CommissionType
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LinkCategory
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.category
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapState
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.IncludeExcludeIcon
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterExpandedState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterHeaderText
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource

@OptIn(
    ExperimentalCoroutinesApi::class, FlowPreview::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
class ArtistSortFilterController(
    scope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
    dataYear: StateFlow<DataYear>,
    lockedSeriesEntry: StateFlow<SeriesInfo?>,
    lockedMerchId: String?,
    dispatchers: CustomDispatchers,
    merchCache: MerchCache,
    seriesEntryDao: SeriesEntryDao,
    seriesImageLoader: SeriesImageLoader,
    val settings: ArtistAlleySettings,
    private val allowHideFavorited: Boolean = true,
    private val allowSettingsBasedToggles: Boolean = true,
) {
    val sortOption = settings.artistsSortOption
    val sortAscending = settings.artistsSortAscending
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.alley_sort_label,
        defaultSort = ArtistSearchSortOption.RANDOM,
        sortOption = sortOption,
        sortAscending = sortAscending,
        allowClear = false,
    )

    val showOnlyConfirmedTags = if (allowSettingsBasedToggles) {
        settings.showOnlyConfirmedTags
    } else {
        savedStateHandle.getMutableStateFlow(
            key = "showOnlyConfirmedTags",
            initialValue = settings.showOnlyConfirmedTags.value,
        )
    }
    val showOnlyConfirmedTagsSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_show_only_confirmed_tags,
        property = showOnlyConfirmedTags,
        default = false,
        allowClear = true,
    )

    private val seriesAutocompleteSection = SeriesAutocompleteSection(
        scope = scope,
        dispatchers = dispatchers,
        lockedSeriesEntry = lockedSeriesEntry,
        seriesEntryDao = seriesEntryDao,
        seriesImageLoader = seriesImageLoader,
        savedStateHandle = savedStateHandle,
        showOnlyConfirmedTagsSection = { state ->
            showOnlyConfirmedTagsSection.Content(state, false)
        }
    )

    private val merch = dataYear.flatMapLatest(merchCache::merchTags)
    private val merchIdsLockedIn = setOfNotNull(lockedMerchId)
    private val merchIdIn =
        savedStateHandle.getMutableStateFlow<List<String>, Set<String>>(
            scope = scope,
            key = "merchIdIn",
            initialValue = { merchIdsLockedIn },
            serialize = { it.toList() },
            deserialize = { it.toSet() },
        )
    private val merchSearchQuery = savedStateHandle.getMutableStateFlow("merchSearchQuery", "")
    private val merchSection = object : SortFilterSectionState.Custom("merch") {
        override fun clear() {
            merchIdIn.value = merchIdsLockedIn
            merchSearchQuery.value = ""
        }

        @Composable
        override fun isDefault() = merchIdIn.collectAsState().value == merchIdsLockedIn
                && merchSearchQuery.collectAsState().value.isEmpty()

        @Composable
        override fun Content(state: SortFilterExpandedState, showDivider: Boolean) {
            val merchTagData by merch.collectAsStateWithLifecycle(MerchTagData(emptyList()))
            var merchIdIn by merchIdIn.collectAsMutableStateWithLifecycle()
            var merchSearchQuery by merchSearchQuery.collectAsMutableStateWithLifecycle()

            MerchTagSection(
                merchTagData = { merchTagData },
                merchIdIn = { merchIdIn },
                onMerchIdInChange = { merchIdIn = it },
                merchIdsLockedIn = merchIdsLockedIn,
                searchQuery = { merchSearchQuery },
                onSearchQueryChange = { merchSearchQuery = it },
                expanded = {state.expandedState[id] == true},
                onExpandedChange = { state.expandedState[id] = it },
                showDivider = showDivider,
                header = {
                    if (state.expandedState[id] == true) {
                        // TODO: This padding isn't completely correct
                        Box(modifier = Modifier.padding(start = 32.dp)) {
                            showOnlyConfirmedTagsSection.Content(state, false)
                        }
                    }
                },
            )
        }
    }

    private val commissionsIn = savedStateHandle.getMutableStateFlow<String, Set<CommissionType>>(
        scope = scope,
        key = "commissionsIn",
        initialValue = { emptySet() },
        serialize = Json::encodeToString,
        deserialize = Json::decodeFromString,
    )
    private val commissionsSection = SortFilterSectionState.Filter(
        title = Res.string.alley_commission_type_filter_label,
        titleDropdownContentDescription = Res.string.alley_commission_type_filter_content_description,
        includeExcludeIconContentDescription = Res.string.alley_commission_type_filter_chip_state_content_description,
        options = MutableStateFlow(CommissionType.entries),
        filterIn = commissionsIn,
        filterNotIn = MutableStateFlow(emptySet()),
        valueToText = { stringResource(it.textRes) },
        selectionMethod = SortFilterSectionState.Filter.SelectionMethod.ONLY_INCLUDE_WITH_EXCLUSIVE_FIRST,
    )

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
    private val linkTypeIdIn =
        savedStateHandle.getMutableStateFlow<List<String>, Set<String>>(
            scope = scope,
            key = "linkTypeIdIn",
            initialValue = { emptySet() },
            serialize = { it.toList() },
            deserialize = { it.toSet() },
        )
    private val linkTypeSection = object : SortFilterSectionState.Custom("linkType") {
        override fun clear() {
            linkTypeIdIn.value = emptySet()
        }

        @Composable
        override fun isDefault() = linkTypeIdIn.collectAsState().value.isEmpty()

        @Composable
        override fun Content(state: SortFilterExpandedState, showDivider: Boolean) {
            var linkTypeIdIn by linkTypeIdIn.collectAsMutableStateWithLifecycle()
            TagSection(
                expanded = { state.expandedState[id] == true },
                onExpandedChange = { state.expandedState[id] = it },
                titleRes = Res.string.alley_link_type_filter_label,
                titleDropdownContentDescriptionRes = Res.string.alley_link_type_filter_content_description,
                tags = linkTypes,
                tagIdIn = linkTypeIdIn,
                tagIdNotIn = emptySet(),
                disabledOptions = emptySet(),
                query = "",
                onQueryChange = {},
                showDivider = showDivider,
                showSearch = false,
                showRootTagsWhenNotExpanded = false,
                showRootTagsAtBottom = true,
                categoryToName = { stringResource((it as LinkTagEntry.Category).category.textRes) },
                tagChip = { linkTag, selected, enabled, modifier ->
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val linkTypeId = linkTag.id
                            if (linkTypeIdIn.contains(linkTypeId)) {
                                linkTypeIdIn -= linkTypeId
                            } else {
                                linkTypeIdIn += linkTypeId
                            }
                        },
                        enabled = enabled,
                        label = {
                            AutoHeightText(stringResource((linkTag as LinkTagEntry.Tag).type.textRes))
                        },
                        leadingIcon = {
                            IncludeExcludeIcon(
                                enabled = when {
                                    linkTypeIdIn.contains(linkTag.id) -> true
                                    else -> null
                                },
                                contentDescriptionRes = Res.string.alley_link_type_filter_chip_state_content_description,
                            )
                        },
                        modifier = modifier
                    )
                }
            )
        }
    }

    private val exhibitorTagsIn = savedStateHandle.getMutableStateFlow<String, Set<String>>(
        scope = scope,
        key = "exhibitorTagsIn",
        initialValue = { emptySet() },
        serialize = Json::encodeToString,
        deserialize = Json::decodeFromString,
    )
    private val exhibitorTagsSection = SortFilterSectionState.Filter(
        id = Res.string.alley_exhibitor_tags_filter_label.key,
        title = { expanded ->
            var showPopup by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .run {
                        if (expanded) {
                            fillMaxWidth()
                        } else {
                            wrapContentWidth()
                        }
                    }
                    .clickable(interactionSource = null, indication = null) {
                        showPopup = !showPopup
                    }
            ) {
                SortFilterHeaderText(
                    expanded = expanded,
                    text = stringResource(Res.string.alley_exhibitor_tags_filter_label),
                    fillWidthOnExpand = false,
                    modifier = Modifier.alignByBaseline()
                )

                Box(modifier = Modifier.alignByBaseline()) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(
                            Res.string.alley_exhibitor_tags_icon_content_description
                        ),
                        modifier = Modifier
                            .fillMaxHeight()
                            .heightIn(max = 20.dp)
                    )

                    if (showPopup) {
                        Popup(onDismissRequest = { showPopup = false }) {
                            Text(
                                text = stringResource(Res.string.alley_exhibitor_tags_explanation),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .widthIn(max = 200.dp)
                            )
                        }
                    }
                }
            }
        },
        titleDropdownContentDescription = Res.string.alley_exhibitor_tags_filter_content_description,
        includeExcludeIconContentDescription = Res.string.alley_exhibitor_tags_filter_chip_state_content_description,
        options = MutableStateFlow(AnimeNycExhibitorTags.TAGS.toList()),
        filterIn = exhibitorTagsIn,
        filterNotIn = MutableStateFlow(emptySet()),
        valueToText = { it },
        selectionMethod = SortFilterSectionState.Filter.SelectionMethod.ONLY_INCLUDE,
    )

    private val artistTagSection = SortFilterSectionState.Filter(
        title = Res.string.alley_artist_tags_filter_label,
        titleDropdownContentDescription = Res.string.alley_artist_tags_filter_content_description,
        includeExcludeIconContentDescription = Res.string.alley_artist_tags_filter_chip_state_content_description,
        options = ReadOnlyStateFlow(ArtistTag.entries),
        filterIn = settings.artistTagsIn,
        filterNotIn = settings.artistTagsNotIn,
        valueToText = { stringResource(it.textRes) },
    )

    private val gridByDefaultSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_show_grid_by_default,
        property = settings.showGridByDefault,
        default = false,
        allowClear = true,
    )

    private val randomCatalogImageSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_show_random_catalog_image,
        property = settings.showRandomCatalogImage,
        default = false,
        allowClear = true,
    )

    val showOutdatedCatalogs = settings.showOutdatedCatalogs
    val showOutdatedCatalogsSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_show_outdated_catalogs,
        property = showOutdatedCatalogs,
        default = false,
        allowClear = false,
    )

    private val hideFavorited = if (allowHideFavorited) {
        savedStateHandle.getMutableStateFlow("hideFavorited", false)
    } else {
        MutableStateFlow(false)
    }
    private val hideFavoritedSection = SortFilterSectionState.Switch(
        title = Res.string.alley_filter_hide_favorited,
        defaultEnabled = false,
        enabled = hideFavorited,
    )

    private val hideIgnored = savedStateHandle.getMutableStateFlow("hideIgnored", false)
    private val hideIgnoredSection = SortFilterSectionState.Switch(
        title = Res.string.alley_filter_hide_ignored,
        defaultEnabled = false,
        enabled = hideIgnored,
    )

    private val forceOneDisplayColumnSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_force_one_display_column,
        property = settings.forceOneDisplayColumn,
        default = false,
        allowClear = true,
    )

    val advancedSection = SortFilterSectionState.Group(
        title = Res.string.alley_filter_advanced,
        titleDropdownContentDescription = Res.string.alley_filter_advanced_expand_content_description,
        children = MutableStateFlow(
            listOfNotNull(
                gridByDefaultSection,
                randomCatalogImageSection,
                showOnlyConfirmedTagsSection.takeIf { allowSettingsBasedToggles },
                showOutdatedCatalogsSection,
                hideFavoritedSection.takeIf { allowHideFavorited },
                hideIgnoredSection,
                forceOneDisplayColumnSection,
            )
        )
    )

    private val sections = dataYear.mapState(scope) { year ->
        listOfNotNull(
            sortSection,
            seriesAutocompleteSection.section,
            merchSection,
            commissionsSection,
            exhibitorTagsSection.takeIf { year == DataYear.ANIME_NYC_2025 },
            artistTagSection,
            linkTypeSection,
            advancedSection,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private val filterParams = combineStates(
        sortOption,
        settings.artistsSortAscending,
        lockedSeriesEntry,
        seriesAutocompleteSection.seriesIn,
        merchIdIn,
        commissionsIn,
        linkTypeIdIn,
        exhibitorTagsIn,
        settings.artistTagsIn,
        settings.artistTagsNotIn,
        showOnlyConfirmedTags,
        showOutdatedCatalogs,
        hideFavorited,
        hideIgnored,
    ) {
        FilterParams(
            sortOption = it[0] as ArtistSearchSortOption,
            sortAscending = it[1] as Boolean,
            seriesIn = setOfNotNull((it[2] as SeriesInfo?)?.id) +
                    (it[3] as List<SeriesAutocompleteSection.SeriesFilterEntry>).map { it.id },
            merchIn = (it[4] as Set<String>) + setOfNotNull(lockedMerchId),
            commissionsIn = it[5] as Set<CommissionType>,
            linkTypesIn = (it[6] as Set<String>).map(Link.Type::valueOf).toSet(),
            exhibitorTagsIn = it[7] as Set<String>,
            artistTagsIn = it[8] as Set<ArtistTag>,
            artistTagsNotIn = it[9] as Set<ArtistTag>,
            showOnlyConfirmedTags = it[10] as Boolean,
            showOutdatedCatalogs = it[11] as Boolean,
            hideFavorited = it[12] as Boolean,
            hideIgnored = it[13] as Boolean,
        )
    }

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = ReadOnlyStateFlow(false),
    )

    fun clear() {
        sections.value.forEach { it.clear() }
        showOnlyConfirmedTagsSection.clear()
        hideFavoritedSection.clear()
    }

    data class FilterParams(
        val sortOption: ArtistSearchSortOption,
        val sortAscending: Boolean,
        val seriesIn: Set<String>,
        val merchIn: Set<String>,
        val commissionsIn: Set<CommissionType>,
        val linkTypesIn: Set<Link.Type>,
        val exhibitorTagsIn: Set<String>,
        val artistTagsIn: Set<ArtistTag>,
        val artistTagsNotIn: Set<ArtistTag>,
        val showOnlyConfirmedTags: Boolean,
        val showOutdatedCatalogs: Boolean,
        val hideFavorited: Boolean,
        val hideIgnored: Boolean,
    )

}
