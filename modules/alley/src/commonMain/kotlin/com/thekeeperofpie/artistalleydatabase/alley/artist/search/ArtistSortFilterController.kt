package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_filter_force_one_display_column
import artistalleydatabase.modules.alley.generated.resources.alley_filter_hide_ignored
import artistalleydatabase.modules.alley.generated.resources.alley_filter_only_catalogs
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_grid_by_default
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_confirmed_tags
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_has_commissions
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_random_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_merch_chip_state_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_merch_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_merch_filter_label
import artistalleydatabase.modules.alley.generated.resources.alley_sort_label
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.AlleyTagEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.IncludeExcludeIcon
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterExpandedState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistSortFilterController(
    scope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
    dataYear: StateFlow<DataYear>,
    lockedMerchId: String?,
    dispatchers: CustomDispatchers,
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

    // TODO: Consider storing this in a singleton instead?
    private val merch = dataYear
        .mapLatest { tagEntryDao.getMerchIds(it) }
        .mapLatest { it.map { AlleyTagEntry.Tag(it) }.associateBy { it.id } }
        .flowOn(dispatchers.io)
        .stateIn(scope, SharingStarted.Lazily, emptyMap())
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
    )

    private val randomCatalogImageSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_show_random_catalog_image,
        property = settings.showRandomCatalogImage,
        default = false,
    )

    private val onlyConfirmedTagsSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_show_only_confirmed_tags,
        property = settings.showOnlyConfirmedTags,
        default = false,
    )

    private val onlyHasCommissionsSection = SortFilterSectionState.SwitchBySetting(
        Res.string.alley_filter_show_only_has_commissions,
        settings.showOnlyHasCommissions,
        default = false,
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
    )

    private val sections = listOf(
        sortSection,
        merchSection,
        onlyCatalogImagesSection,
        gridByDefaultSection,
        randomCatalogImageSection,
        onlyConfirmedTagsSection,
        onlyHasCommissionsSection,
        hideIgnoredSection,
        forceOneDisplayColumnSection,
    )

    @Suppress("UNCHECKED_CAST")
    private val filterParams = combineStates(
        merchIdIn,
        sortOption,
        settings.artistsSortAscending,
        onlyCatalogImages,
        settings.showOnlyConfirmedTags,
        settings.showOnlyHasCommissions,
        hideIgnored,
    ) {
        FilterParams(
            merchIn = (it[0] as Set<String>) + setOfNotNull(lockedMerchId),
            sortOption = it[1] as ArtistSearchSortOption,
            sortAscending = it[2] as Boolean,
            showOnlyWithCatalog = it[3] as Boolean,
            showOnlyConfirmedTags = it[4] as Boolean,
            showOnlyHasCommissions = it[5] as Boolean,
            hideIgnored = it[6] as Boolean,
        )
    }

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = ReadOnlyStateFlow(true),
    )

    data class FilterParams(
        val merchIn: Set<String>,
        val sortOption: ArtistSearchSortOption,
        val sortAscending: Boolean,
        val showOnlyWithCatalog: Boolean,
        val showOnlyConfirmedTags: Boolean,
        val showOnlyHasCommissions: Boolean,
        val hideIgnored: Boolean,
    )
}
