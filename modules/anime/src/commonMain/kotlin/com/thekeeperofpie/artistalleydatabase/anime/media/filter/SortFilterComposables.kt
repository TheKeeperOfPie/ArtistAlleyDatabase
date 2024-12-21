@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_tag_category_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_tag_chip_state_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_tag_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_tag_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_tag_rank_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_tag_rank_query_summary
import artistalleydatabase.modules.anime.generated.resources.anime_media_tag_long_click_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_tag_search_clear_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_tag_search_placeholder
import artistalleydatabase.modules.anime.generated.resources.anime_media_tag_search_show_when_spoiler
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaTagSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.CustomOutlinedTextField
import com.thekeeperofpie.artistalleydatabase.utils_compose.FilterChip
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.CustomFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.IncludeExcludeIcon
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun TagSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    showMediaWithTagSpoiler: () -> Boolean,
    onShowMediaWithTagSpoilerChange: (Boolean) -> Unit,
    tags: @Composable () -> Map<String, MediaTagSection>,
    onTagClick: (String) -> Unit,
    tagRank: @Composable () -> String,
    onTagRankChange: (String) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    showDivider: Boolean,
) {
    @Suppress("NAME_SHADOWING")
    val expanded = expanded()
    CustomFilterSection(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        titleRes = Res.string.anime_media_filter_tag_label,
        titleDropdownContentDescriptionRes = Res.string.anime_media_filter_tag_content_description,
        summaryText = {
            @Suppress("NAME_SHADOWING")
            val rank = when (val tagRank = tagRank().toIntOrNull()?.coerceIn(0, 100)) {
                null, 0 -> null
                100 -> "== 100"
                else -> "≥ $tagRank"
            }
            if (query.isEmpty()) {
                rank
            } else if (rank == null) {
                null
            } else {
                stringResource(Res.string.anime_media_filter_tag_rank_query_summary, rank, query)
            }
        },
        onSummaryClick = { onTagRankChange("") },
        showDivider = showDivider,
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            @Suppress("NAME_SHADOWING")
            val tags = tags()
            val subcategoriesToShow = (if (query.isNotBlank()) {
                tags.values.mapNotNull {
                    it.filter {
                        it.state != FilterIncludeExcludeState.DEFAULT
                                || it.name.contains(query, ignoreCase = true)
                    }
                }
            } else if (expanded) {
                tags.values
            } else {
                tags.values.mapNotNull {
                    it.filter { it.state != FilterIncludeExcludeState.DEFAULT }
                }
            }).filterIsInstance<MediaTagSection.Category>()

            if (expanded || subcategoriesToShow.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShowMediaWithTagSpoilerChange(!showMediaWithTagSpoiler()) }
                ) {
                    Text(
                        text = stringResource(Res.string.anime_media_tag_search_show_when_spoiler),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(start = 32.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
                            .weight(1f)
                    )

                    Switch(
                        checked = showMediaWithTagSpoiler(),
                        onCheckedChange = onShowMediaWithTagSpoilerChange,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                }
            }

            if (expanded) {
                TextField(
                    value = query,
                    placeholder = {
                        Text(text = stringResource(Res.string.anime_media_tag_search_placeholder))
                    },
                    trailingIcon = {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(
                                    Res.string.anime_media_tag_search_clear_content_description
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

            val children =
                tags.values.filterIsInstance<MediaTagSection.Tag>()
            if (children.isNotEmpty()) {
                TagChips(
                    tags = children,
                    level = 0,
                    onTagClick = onTagClick,
                )
            }

            subcategoriesToShow.forEachIndexed { index, section ->
                TagSubsection(
                    name = section.name,
                    children = section.children.values,
                    parentExpanded = expanded,
                    level = 0,
                    onTagClick = onTagClick,
                    query = query,
                    showDivider = expanded || index != subcategoriesToShow.size - 1,
                )
            }

            if (expanded) {
                Text(
                    text = stringResource(Res.string.anime_media_filter_tag_rank_label),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    @Suppress("NAME_SHADOWING")
                    val tagRank = tagRank()
                    Slider(
                        value = tagRank.toIntOrNull()?.coerceIn(0, 100)?.toFloat() ?: 0f,
                        valueRange = 0f..100f,
                        steps = 100,
                        onValueChange = { onTagRankChange(it.roundToInt().toString()) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                    )

                    CustomOutlinedTextField(
                        value = tagRank,
                        onValueChange = { onTagRankChange(it) },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            autoCorrectEnabled = KeyboardOptions.Default.autoCorrectEnabled,
                        ),
                        contentPadding = OutlinedTextFieldDefaults.contentPadding(
                            start = 12.dp,
                            top = 8.dp,
                            end = 12.dp,
                            bottom = 8.dp
                        ),
                        modifier = Modifier.width(64.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TagSubsection(
    name: String,
    children: Collection<MediaTagSection>,
    parentExpanded: Boolean,
    level: Int,
    onTagClick: (String) -> Unit,
    query: String,
    showDivider: Boolean,
) {
    var expanded by remember(name) { mutableStateOf(false) }
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
                    Res.string.anime_media_filter_tag_category_expand_content_description,
                    name
                ),
                onClick = { expanded = !expanded },
            )
        }
    }

    val tags = children.filterIsInstance<MediaTagSection.Tag>()
    val tagsToShow = if (query.isNotBlank()) {
        tags.filter {
            it.state != FilterIncludeExcludeState.DEFAULT
                    || it.name.contains(query, ignoreCase = true)
        }
    } else if (expanded) {
        tags
    } else {
        tags.filter { it.state != FilterIncludeExcludeState.DEFAULT }
    }

    val subcategories =
        children.filterIsInstance<MediaTagSection.Category>()
    val subcategoriesToShow = if (query.isNotBlank()) {
        subcategories.mapNotNull {
            it.filter {
                it.state != FilterIncludeExcludeState.DEFAULT
                        || it.name.contains(query, ignoreCase = true)
            } as? MediaTagSection.Category
        }
    } else if (expanded) {
        subcategories
    } else {
        subcategories.mapNotNull {
            it.filter { it.state != FilterIncludeExcludeState.DEFAULT } as? MediaTagSection.Category
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
                tagsToShow,
                level,
                onTagClick,
            )
        }

        subcategoriesToShow.forEachIndexed { index, section ->
            TagSubsection(
                section.name,
                section.children.values,
                parentExpanded = parentExpanded && expanded,
                level + 1,
                onTagClick,
                query = query,
                showDivider = index != subcategoriesToShow.size - 1,
            )
        }
    }

    if (showDivider) {
        HorizontalDivider(modifier = Modifier.padding(start = dividerStartPadding))
    }
}

@Composable
private fun TagChips(
    tags: List<MediaTagSection.Tag>,
    level: Int,
    onTagClick: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp * level + 48.dp, end = 16.dp)
            .animateContentSize(),
    ) {
        val mediaTagDialogController = LocalMediaTagDialogController.current
        tags.forEach {
            FilterChip(
                selected = it.state != FilterIncludeExcludeState.DEFAULT,
                onClick = { onTagClick(it.id) },
                onLongClickLabel = stringResource(
                    Res.string.anime_media_tag_long_click_content_description
                ),
                onLongClick = { mediaTagDialogController?.onLongClickTag(it.id) },
                enabled = it.clickable,
                label = { AutoHeightText(it.value.name) },
                leadingIcon = {
                    IncludeExcludeIcon(
                        it,
                        Res.string.anime_media_filter_tag_chip_state_content_description,
                    )
                },
                modifier = Modifier.animateContentSize()
            )
        }
    }
}
