@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_tag_chip_state_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_tag_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_tag_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_tag_rank_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_tag_rank_query_summary
import artistalleydatabase.modules.anime.generated.resources.anime_media_tag_long_click_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_tag_search_show_when_spoiler
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaTagEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.CustomOutlinedTextField
import com.thekeeperofpie.artistalleydatabase.utils_compose.FilterChip
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.IncludeExcludeIcon
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagSection
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import artistalleydatabase.modules.anime.media.data.generated.resources.Res as MediaDataRes

@Composable
fun MediaTagSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    showMediaWithTagSpoiler: () -> Boolean,
    onShowMediaWithTagSpoilerChange: (Boolean) -> Unit,
    tags: List<Pair<String, TagEntry>>,
    tagIdIn: Set<String>,
    tagIdNotIn: Set<String>,
    disabledOptions: Set<String>,
    onTagClick: (String) -> Unit,
    tagRank: @Composable () -> String,
    onTagRankChange: (String) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    showDivider: Boolean,
) {
    val mediaTagDialogController = LocalMediaTagDialogController.current
    TagSection(
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
        header = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowMediaWithTagSpoilerChange(!showMediaWithTagSpoiler()) }
            ) {
                Text(
                    text = stringResource(MediaDataRes.string.anime_media_tag_search_show_when_spoiler),
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
        },
        footer = {
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
        },
        tags = tags,
        tagIdIn = tagIdIn,
        tagIdNotIn = tagIdNotIn,
        disabledOptions = disabledOptions,
        query = query,
        onQueryChange = onQueryChange,
        showDivider = showDivider,
        categoryToName = { (it as MediaTagEntry.Category).name },
        tagChip = { tag, selected, enabled, modifier ->
            tag as MediaTagEntry.Tag
            FilterChip(
                selected = selected,
                onClick = { onTagClick(tag.id) },
                onLongClickLabel = stringResource(
                    Res.string.anime_media_tag_long_click_content_description
                ),
                onLongClick = { mediaTagDialogController?.onLongClickTag(tag.id) },
                enabled = enabled,
                label = { AutoHeightText(tag.value.name) },
                leadingIcon = {
                    IncludeExcludeIcon(
                        enabled = when {
                            tagIdIn.contains(tag.id) -> true
                            tagIdNotIn.contains(tag.id) -> false
                            else -> null
                        },
                        contentDescriptionRes = Res.string.anime_media_filter_tag_chip_state_content_description,
                        leadingIconVector = MediaDataUtils.tagLeadingIcon(
                            isAdult = tag.isAdult,
                            isGeneralSpoiler = tag.value.isGeneralSpoiler,
                        ),
                        leadingIconContentDescription = MediaDataUtils.tagLeadingIconContentDescription(
                            isAdult = tag.isAdult,
                            isGeneralSpoiler = tag.value.isGeneralSpoiler,
                        ),
                    )
                },
                modifier = modifier
            )
        }
    )
}
