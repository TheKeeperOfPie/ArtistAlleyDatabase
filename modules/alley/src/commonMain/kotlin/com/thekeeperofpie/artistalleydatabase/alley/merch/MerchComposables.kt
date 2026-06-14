package com.thekeeperofpie.artistalleydatabase.alley.merch

import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_merch_chip_state_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_merch_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_merch_filter_label
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.IncludeExcludeIcon
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagSection
import org.jetbrains.compose.resources.StringResource

@Composable
internal fun MerchTagSection(
    merchTagData: () -> MerchTagData,
    merchIdIn: () -> Set<String>,
    onMerchIdInChange: (Set<String>) -> Unit,
    merchIdsLockedIn: Set<String>,
    searchQuery: () -> String,
    onSearchQueryChange: (String) -> Unit,
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    showDivider: Boolean,
    titleRes: StringResource = Res.string.alley_merch_filter_label,
    titleDropdownContentDescriptionRes: StringResource = Res.string.alley_merch_filter_content_description,
    header: (@Composable () -> Unit)? = null,
) {
    val merchData = merchTagData()
    val merchIdIn = merchIdIn()
    val merchSearchQuery = searchQuery()
    TagSection(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        titleRes = titleRes,
        titleDropdownContentDescriptionRes = titleDropdownContentDescriptionRes,
        header = header,
        tags = merchData.tags,
        tagIdIn = merchIdIn,
        tagIdNotIn = emptySet(),
        disabledOptions = merchIdsLockedIn,
        query = merchSearchQuery,
        onQueryChange = onSearchQueryChange,
        showDivider = showDivider,
        showRootTagsWhenNotExpanded = false,
        categoryToName = { it.id },
        tagChip = { merch, selected, enabled, modifier ->
            val merchId = merch.id
            val selected = merchData.selected(merchIdIn, merchId)
            FilterChip(
                selected = selected,
                onClick = {
                    onMerchIdInChange(
                        merchData.toggle(
                            merchIdsLockedIn = merchIdsLockedIn,
                            merchIdIn = merchIdIn,
                            merchId = merchId,
                            wasSelected = selected,
                        )
                    )
                },
                enabled = enabled,
                label = {
                    AutoHeightText(if (merchId.startsWith("all")) "All" else merchId)
                },
                leadingIcon = {
                    IncludeExcludeIcon(
                        enabled = when {
                            merchIdIn.contains(merchId) -> true
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
