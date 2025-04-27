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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_filter_chip_state_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_filter_advanced
import artistalleydatabase.modules.alley.generated.resources.alley_filter_advanced_expand_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_filter_force_one_display_column
import artistalleydatabase.modules.alley.generated.resources.alley_filter_hide_ignored
import artistalleydatabase.modules.alley.generated.resources.alley_filter_only_catalogs
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_grid_by_default
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_confirmed_tags
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_random_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_filter_chip_state_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_merch_chip_state_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_merch_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_merch_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_search_clear_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_search_placeholder
import artistalleydatabase.modules.alley.generated.resources.alley_sort_label
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkCategory
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkTagEntry
import com.thekeeperofpie.artistalleydatabase.alley.links.category
import com.thekeeperofpie.artistalleydatabase.alley.links.textRes
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.AlleyTagEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.CommissionType
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.tags.name
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.CustomFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.IncludeExcludeIcon
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterExpandedState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@OptIn(
    ExperimentalCoroutinesApi::class, FlowPreview::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
class ArtistSortFilterController(
    scope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
    dataYear: StateFlow<DataYear>,
    lockedSeriesEntry: StateFlow<SeriesEntry?>,
    lockedMerchId: String?,
    dispatchers: CustomDispatchers,
    seriesEntryDao: SeriesEntryDao,
    seriesImagesStore: SeriesImagesStore,
    tagEntryDao: TagEntryDao,
    val settings: ArtistAlleySettings,
) {
    val sortOption = settings.artistsSortOption
    val sortAscending = settings.artistsSortAscending
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.alley_sort_label,
        defaultSort = ArtistSearchSortOption.RANDOM,
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private var seriesQuery by mutableStateOf("")
    private val seriesIn =
        savedStateHandle.getMutableStateFlow(
            scope = scope,
            json = Json,
            key = "seriesIn",
            initialValue = { emptyList<SeriesFilterEntry>() },
        )

    @Serializable
    private data class SeriesFilterEntry(
        val id: String,
        val titlePreferred: String,
        val titleEnglish: String,
        val titleRomaji: String,
        val titleNative: String,
    ) {
        constructor(entry: SeriesEntry) : this(
            id = entry.id,
            titlePreferred = entry.titlePreferred,
            titleEnglish = entry.titleEnglish,
            titleRomaji = entry.titleRomaji,
            titleNative = entry.titleNative,
        )

        fun name(languageOption: AniListLanguageOption) = when (languageOption) {
            AniListLanguageOption.DEFAULT -> titlePreferred
            AniListLanguageOption.ENGLISH -> titleEnglish
            AniListLanguageOption.NATIVE -> titleNative
            AniListLanguageOption.ROMAJI -> titleRomaji
        }
    }

    private val seriesResults = snapshotFlow { seriesQuery }
        .debounce(500.milliseconds)
        .mapLatest(seriesEntryDao::searchSeriesForAutocomplete)
        .flowOn(dispatchers.io)
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val seriesImageLoader = SeriesImageLoader(dispatchers, scope, seriesImagesStore)
    private val seriesSection = object : SortFilterSectionState.Custom("series") {
        override fun clear() {
            seriesIn.value = emptyList()
        }

        @Composable
        override fun isDefault(): Boolean {
            val seriesIn by seriesIn.collectAsStateWithLifecycle()
            return remember { derivedStateOf { seriesIn.isEmpty() } }.value
        }

        @Composable
        override fun Content(
            state: SortFilterExpandedState,
            showDivider: Boolean,
        ) {
            val expanded = state.expandedState[id] == true
            CustomFilterSection(
                expanded = expanded,
                onExpandedChange = { state.expandedState[id] = it },
                titleRes = Res.string.alley_series_filter_label,
                titleDropdownContentDescriptionRes = Res.string.alley_series_filter_content_description,
                showDivider = showDivider,
            ) {
                Column(modifier = Modifier.animateContentSize()) {
                    if (expanded) {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = it },
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            val interactionSource = remember { MutableInteractionSource() }
                            TextField(
                                value = seriesQuery,
                                placeholder = {
                                    Text(text = stringResource(Res.string.alley_series_filter_search_placeholder))
                                },
                                trailingIcon = {
                                    IconButton(onClick = { seriesQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = stringResource(
                                                Res.string.alley_series_filter_search_clear_content_description
                                            ),
                                        )
                                    }
                                },
                                onValueChange = { seriesQuery = it },
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

                            val results by seriesResults.collectAsStateWithLifecycle()
                            ExposedDropdownMenu(
                                expanded = focused && results.isNotEmpty(),
                                onDismissRequest = {
                                    // This callback is invoked whenever the query changes,
                                    // which makes it unusable if the user is typing
                                    if (!isImeVisible) {
                                        focusManager.clearFocus()
                                    }
                                },
                            ) {
                                var seriesIn by seriesIn.collectAsMutableStateWithLifecycle()
                                results.forEach {
                                    DropdownMenuItem(
                                        onClick = {
                                            seriesIn += SeriesFilterEntry(it)
                                            focusManager.clearFocus(true)
                                        },
                                        text = {
                                            SeriesRow(
                                                series = it,
                                                image = { seriesImageLoader.getSeriesImage(it) },
                                            )
                                        },
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
                        val lockedSeries by lockedSeriesEntry.collectAsStateWithLifecycle()
                        lockedSeries?.let {
                            FilterChip(
                                selected = true,
                                enabled = false,
                                label = { AutoHeightText(it.name(LocalLanguageOptionMedia.current)) },
                                onClick = {},
                                modifier = Modifier.animateContentSize()
                            )
                        }
                        var seriesIn by seriesIn.collectAsMutableStateWithLifecycle()
                        seriesIn.forEach {
                            FilterChip(
                                selected = true,
                                enabled = true,
                                label = { AutoHeightText(it.name(LocalLanguageOptionMedia.current)) },
                                onClick = { seriesIn -= it },
                                modifier = Modifier.animateContentSize()
                            )
                        }
                    }
                }
            }
        }

    }

    // TODO: Consider storing this in a singleton instead?
    private val merch = dataYear
        .mapLatest { tagEntryDao.getMerchIds(it) }
        .mapLatest { it.map { it to AlleyTagEntry.Tag(it) } }
        .flowOn(dispatchers.io)
        .stateIn(scope, SharingStarted.Lazily, emptyList())
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
            val merch by merch.collectAsStateWithLifecycle()
            var merchIdIn by merchIdIn.collectAsMutableStateWithLifecycle()
            var merchSearchQuery by merchSearchQuery.collectAsMutableStateWithLifecycle()
            TagSection(
                expanded = { state.expandedState[id] == true },
                onExpandedChange = { state.expandedState[id] = it },
                titleRes = Res.string.alley_merch_filter_label,
                titleDropdownContentDescriptionRes = Res.string.alley_merch_filter_content_description,
                tags = merch,
                tagIdIn = merchIdIn,
                tagIdNotIn = emptySet(),
                disabledOptions = merchIdsLockedIn,
                query = merchSearchQuery,
                onQueryChange = { merchSearchQuery = it },
                showDivider = showDivider,
                showRootTagsWhenNotExpanded = false,
                categoryToName = { it.id },
                tagChip = { merch, selected, enabled, modifier ->
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val merchId = merch.id
                            if (merchId !in merchIdsLockedIn) {
                                if (merchIdIn.contains(merchId)) {
                                    merchIdIn -= merchId
                                } else {
                                    merchIdIn += merchId
                                }
                            }
                        },
                        enabled = enabled,
                        label = { AutoHeightText(merch.id) },
                        leadingIcon = {
                            IncludeExcludeIcon(
                                enabled = when {
                                    merchIdIn.contains(merch.id) -> true
                                    else -> null
                                },
                                contentDescriptionRes = Res.string.alley_merch_chip_state_content_description,
                            )
                        },
                        modifier = modifier
                    )
                }
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

    val onlyCatalogImages = savedStateHandle.getMutableStateFlow("onlyCatalogImages", false)
    private val onlyCatalogImagesSection = SortFilterSectionState.Switch(
        title = Res.string.alley_filter_only_catalogs,
        defaultEnabled = false,
        enabled = onlyCatalogImages
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

    private val onlyConfirmedTagsSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_show_only_confirmed_tags,
        property = settings.showOnlyConfirmedTags,
        default = false,
        allowClear = true,
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
            listOf(
                onlyCatalogImagesSection,
                gridByDefaultSection,
                randomCatalogImageSection,
                onlyConfirmedTagsSection,
                hideIgnoredSection,
                forceOneDisplayColumnSection,
            )
        )
    )

    private val sections = listOf(
        sortSection,
        seriesSection,
        merchSection,
        commissionsSection,
        linkTypeSection,
        advancedSection,
    )

    @Suppress("UNCHECKED_CAST")
    private val filterParams = combineStates(
        sortOption,
        settings.artistsSortAscending,
        lockedSeriesEntry,
        seriesIn,
        merchIdIn,
        commissionsIn,
        linkTypeIdIn,
        onlyCatalogImages,
        settings.showOnlyConfirmedTags,
        hideIgnored,
    ) {
        FilterParams(
            sortOption = it[0] as ArtistSearchSortOption,
            sortAscending = it[1] as Boolean,
            seriesIn = setOfNotNull((it[2] as SeriesEntry?)?.id) + (it[3] as List<SeriesFilterEntry>).map { it.id },
            merchIn = (it[4] as Set<String>) + setOfNotNull(lockedMerchId),
            commissionsIn = it[5] as Set<CommissionType>,
            linkTypesIn = (it[6] as Set<String>).map(Link.Type::valueOf).toSet(),
            showOnlyWithCatalog = it[7] as Boolean,
            showOnlyConfirmedTags = it[8] as Boolean,
            hideIgnored = it[9] as Boolean,
        )
    }

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = ReadOnlyStateFlow(false),
    )

    data class FilterParams(
        val sortOption: ArtistSearchSortOption,
        val sortAscending: Boolean,
        val seriesIn: Set<String>,
        val merchIn: Set<String>,
        val commissionsIn: Set<CommissionType>,
        val linkTypesIn: Set<Link.Type>,
        val showOnlyWithCatalog: Boolean,
        val showOnlyConfirmedTags: Boolean,
        val hideIgnored: Boolean,
    )
}
