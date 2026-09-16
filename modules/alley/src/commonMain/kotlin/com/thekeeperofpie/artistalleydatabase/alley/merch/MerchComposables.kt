package com.thekeeperofpie.artistalleydatabase.alley.merch

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateSet
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_merch_chip_state_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_merch_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_merch_filter_label
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.IncludeExcludeIcon
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagSection2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.SnapshotStateSetSerializer
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.TextFieldStateSerializer
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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

@Serializable
internal class MerchTagSectionState(
    @Serializable(with = SnapshotStateSetSerializer::class)
    val merchIdsLockedIn: SnapshotStateSet<String> = mutableStateSetOf(),
    @Serializable(with = TextFieldStateSerializer::class)
    val query: TextFieldState = TextFieldState(),
    val tags: TagSectionState = TagSectionState(),
) {
    fun clear() {
        Snapshot.withMutableSnapshot {
            query.clearText()
            tags.clear()
        }
    }
}

@Composable
internal fun MerchTagSection2(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    state: MerchTagSectionState,
    merchTagData: () -> MerchTagData,
    sectionHeader: @Composable () -> Unit = { Text(stringResource(Res.string.alley_merch_filter_label)) },
    sectionHeaderDropdownContentDescriptionRes: StringResource = Res.string.alley_merch_filter_content_description,
    header: (@Composable () -> Unit)? = null,
) {
    val merchData = merchTagData()
    TagSection2(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        sectionHeader = sectionHeader,
        sectionHeaderDropdownContentDescriptionRes = sectionHeaderDropdownContentDescriptionRes,
        header = header,
        tags = merchData.tags,
        state = state.tags,
        disabledOptions = state.merchIdsLockedIn,
        showRootTagsWhenNotExpanded = false,
        categoryToName = { it.id },
        tagChip = { merch, selected, enabled, modifier ->
            val merchId = merch.id
            val selected = merchData.selected(state.tags.tagIdIn, merchId)
            FilterChip(
                selected = selected,
                onClick = {
                    state.tags.tagIdIn.replaceAll(
                        merchData.toggle(
                            merchIdsLockedIn = state.merchIdsLockedIn,
                            merchIdIn = state.tags.tagIdIn,
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
                        enabled = if (state.tags.tagIdIn.contains(merchId)) true else null,
                        contentDescriptionRes = Res.string.alley_merch_chip_state_content_description,
                    )
                },
                modifier = modifier
            )
        }
    )
}
