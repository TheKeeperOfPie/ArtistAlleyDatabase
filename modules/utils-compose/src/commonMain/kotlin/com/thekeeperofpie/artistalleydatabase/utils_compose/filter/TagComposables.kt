package com.thekeeperofpie.artistalleydatabase.utils_compose.filter

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.tag_expand_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.tag_search_clear_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.tag_search_placeholder
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagEntry.Category
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagEntry.Tag
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TagSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    titleRes: StringResource,
    titleDropdownContentDescriptionRes: StringResource,
    summaryText: (@Composable () -> String?)? = null,
    onSummaryClick: () -> Unit = {},
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    tags: Map<String, TagEntry>,
    tagIdIn: Set<String>,
    tagIdNotIn: Set<String>,
    disabledOptions: Set<String>,
    query: String,
    onQueryChange: (String) -> Unit,
    showDivider: Boolean,
    showRootTagsWhenNotExpanded: Boolean = true,
    categoryToName: (Category) -> String,
    tagChip: @Composable (tag: Tag, selected: Boolean, enabled: Boolean, Modifier) -> Unit,
) {
    @Suppress("NAME_SHADOWING")
    val expanded = expanded()
    CustomFilterSection(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        titleRes = titleRes,
        titleDropdownContentDescriptionRes = titleDropdownContentDescriptionRes,
        summaryText = summaryText,
        onSummaryClick = onSummaryClick,
        showDivider = showDivider,
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            val subcategoriesToShow = when {
                query.isNotBlank() -> tags.values.mapNotNull {
                    it.filter {
                        tagIdIn.contains(it.id) || tagIdNotIn.contains(it.id) || it.matches(query)
                    }
                }
                expanded -> tags.values
                else -> tags.values.mapNotNull {
                    it.filter { tagIdIn.contains(it.id) || tagIdNotIn.contains(it.id) }
                }
            }.filterIsInstance<Category>()

            if (header != null && (expanded || subcategoriesToShow.isNotEmpty())) {
                header()
            }

            if (expanded) {
                TextField(
                    value = query,
                    placeholder = {
                        Text(text = stringResource(Res.string.tag_search_placeholder))
                    },
                    trailingIcon = {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(
                                    Res.string.tag_search_clear_content_description
                                ),
                            )
                        }
                    },
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 16.dp)
                )
            }

            val children = when {
                showRootTagsWhenNotExpanded ->  tags.values
                query.isNotBlank() -> tags.values.mapNotNull {
                    it.filter {
                        tagIdIn.contains(it.id) || tagIdNotIn.contains(it.id) || it.matches(query)
                    }
                }
                expanded -> tags.values
                else -> tags.values.mapNotNull {
                    it.filter { tagIdIn.contains(it.id) || tagIdNotIn.contains(it.id) }
                }
            }.filterIsInstance<Tag>()
            if (children.isNotEmpty()) {
                TagChips(
                    tags = children,
                    tagIdIn = tagIdIn,
                    tagIdNotIn = tagIdNotIn,
                    disabledOptions = disabledOptions,
                    level = 0,
                    tagChip = tagChip,
                )
            }

            subcategoriesToShow.forEachIndexed { index, section ->
                TagSubsection(
                    category = section,
                    categoryToName = categoryToName,
                    tagIdIn = tagIdIn,
                    tagIdNotIn = tagIdNotIn,
                    disabledOptions = disabledOptions,
                    parentExpanded = expanded,
                    level = 0,
                    query = query,
                    showDivider = expanded || index != subcategoriesToShow.size - 1,
                    tagChip = tagChip,
                )
            }

            if (footer != null && expanded) {
                footer()
            }
        }
    }
}

@Composable
private fun TagSubsection(
    category: Category,
    categoryToName: (Category) -> String,
    tagIdIn: Set<String>,
    tagIdNotIn: Set<String>,
    disabledOptions: Set<String>,
    parentExpanded: Boolean,
    level: Int,
    query: String,
    showDivider: Boolean,
    tagChip: @Composable (tag: Tag, selected: Boolean, enabled: Boolean, Modifier) -> Unit,
) {
    var expanded by remember(category.id) { mutableStateOf(false) }
    val startPadding = 16.dp * level + 32.dp
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .run {
                if (parentExpanded) {
                    clickable { expanded = !expanded }
                } else this
            }
    ) {
        val name = categoryToName(category)
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = startPadding, top = 8.dp, end = 16.dp, bottom = 8.dp)
                .weight(1f)
        )

        if (parentExpanded) {
            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = stringResource(
                    Res.string.tag_expand_content_description,
                    name
                ),
                onClick = { expanded = !expanded },
            )
        }
    }

    // Need to show if exists in either set or otherwise user can't see the enabled/disabled state
    fun shouldShowBasedOnId(id: String) = tagIdIn.contains(id) || tagIdNotIn.contains(id)

    val tags = category.children.values.filterIsInstance<Tag>()
    val tagsToShow = if (query.isNotBlank()) {
        tags.filter { shouldShowBasedOnId(it.id) || it.matches(query) }
    } else if (expanded) {
        tags
    } else {
        tags.filter { shouldShowBasedOnId(it.id) }
    }

    val subcategories = category.children.values.filterIsInstance<Category>()
    val subcategoriesToShow = if (query.isNotBlank()) {
        subcategories.mapNotNull {
            it.filter { shouldShowBasedOnId(it.id) || it.matches(query) } as? Category
        }
    } else if (expanded) {
        subcategories
    } else {
        subcategories.mapNotNull {
            it.filter { shouldShowBasedOnId(it.id) } as? Category
        }
    }

    val dividerStartPadding = startPadding - 8.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        if (tagsToShow.isNotEmpty()) {
            TagChips(
                tags = tagsToShow,
                tagIdIn = tagIdIn,
                tagIdNotIn = tagIdNotIn,
                disabledOptions = disabledOptions,
                level = level,
                tagChip = tagChip,
            )
        }

        subcategoriesToShow.forEachIndexed { index, section ->
            TagSubsection(
                category = section,
                categoryToName = categoryToName,
                tagIdIn = tagIdIn,
                tagIdNotIn = tagIdNotIn,
                disabledOptions = disabledOptions,
                parentExpanded = parentExpanded && expanded,
                level = level + 1,
                query = query,
                showDivider = index != subcategoriesToShow.size - 1,
                tagChip = tagChip,
            )
        }
    }

    if (showDivider) {
        HorizontalDivider(modifier = Modifier.padding(start = dividerStartPadding))
    }
}

@Composable
private fun TagChips(
    tags: List<Tag>,
    tagIdIn: Set<String>,
    tagIdNotIn: Set<String>,
    disabledOptions: Set<String>,
    level: Int,
    tagChip: @Composable (tag: Tag, selected: Boolean, enabled: Boolean, Modifier) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp * level + 48.dp, end = 16.dp)
            .animateContentSize(),
    ) {
        tags.forEach {
            val selected = tagIdIn.contains(it.id) || tagIdNotIn.contains(it.id)
            val enabled = it.id !in disabledOptions
            tagChip(it, selected, enabled, Modifier.animateContentSize())
        }
    }
}
