package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.anilist.MediaDetailsQuery
import com.anilist.fragment.GeneralMediaTag
import com.anilist.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.AssistChip
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.assistChipColors

data class AnimeMediaTagEntry(
    val id: String,
    val name: String,
    val shouldHide: Boolean = false,
    val leadingIconVector: ImageVector? = null,
    val leadingIconContentDescription: Int? = null,
    val textHiddenRes: Int? = null,
    val rank: Int? = null,
) {
    companion object {
        @Composable
        fun Chip(
            tag: AnimeMediaTagEntry,
            modifier: Modifier = Modifier,
            title: @Composable () -> String = { tag.name },
            onTagClick: (mediatagId: String, tagName: String) -> Unit = { _, _ -> },
            containerColor: Color,
            textColor: Color,
            textStyle: TextStyle? = null,
            autoResize: Boolean = true,
        ) {
            val shouldHide = tag.shouldHide
            var hidden by remember(tag.id) { mutableStateOf(shouldHide) }
            val mediaTagDialogController = LocalMediaTagDialogController.current
            AssistChip(
                onClick = {
                    if (!hidden) {
                        onTagClick(tag.id, tag.name)
                    } else {
                        hidden = false
                    }
                },
                onLongClickLabel = stringResource(
                    R.string.anime_media_tag_long_click_content_description
                ),
                onLongClick = { mediaTagDialogController?.onLongClickTag(tag.id) },
                colors = assistChipColors(
                    containerColor = containerColor
                        .takeOrElse { MaterialTheme.colorScheme.surfaceVariant },
                    labelColor = textColor
                        .takeOrElse { MaterialTheme.colorScheme.onSurfaceVariant },
                ),
                border = null,
                leadingIcon = {
                    if (tag.leadingIconVector != null
                        && tag.leadingIconContentDescription != null
                    ) {
                        Icon(
                            painter = rememberVectorPainter(tag.leadingIconVector),
                            contentDescription = stringResource(
                                tag.leadingIconContentDescription
                            ),
                            tint = textColor,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .size(16.dp)
                        )
                    }
                },
                label = {
                    val style = textStyle ?: LocalTextStyle.current
                    val text = if (hidden && tag.textHiddenRes != null) {
                        stringResource(tag.textHiddenRes)
                    } else {
                        title()
                    }
                    if (autoResize) {
                        AutoResizeHeightText(text = text, style = style)
                    } else {
                        Text(text = text, style = style)
                    }
                },
                modifier = modifier,
            )
        }
    }

    constructor(tag: GeneralMediaTag, isMediaSpoiler: Boolean?, rank: Int? = null) : this(
        id = tag.id.toString(),
        name = tag.name,
        shouldHide = (tag.isAdult ?: false)
                || (tag.isGeneralSpoiler ?: false)
                || (isMediaSpoiler ?: false),
        leadingIconVector = MediaUtils.tagLeadingIcon(
            isAdult = tag.isAdult,
            isGeneralSpoiler = tag.isGeneralSpoiler,
            isMediaSpoiler = isMediaSpoiler,
        ),
        leadingIconContentDescription = MediaUtils.tagLeadingIconContentDescription(
            isAdult = tag.isAdult,
            isGeneralSpoiler = tag.isGeneralSpoiler,
            isMediaSpoiler = isMediaSpoiler,
        ),
        textHiddenRes = when {
            tag.isAdult ?: false -> R.string.anime_media_tag_adult
            (tag.isGeneralSpoiler ?: false) || (isMediaSpoiler ?: false) ->
                R.string.anime_media_tag_spoiler
            else -> null
        },
        rank = rank,
    )

    constructor(tag: MediaPreview.Tag) : this(
        tag,
        isMediaSpoiler = tag.isMediaSpoiler,
        rank = tag.rank
    )

    constructor(tag: MediaDetailsQuery.Data.Media.Tag) : this(
        tag,
        isMediaSpoiler = tag.isMediaSpoiler,
        rank = tag.rank
    )
}
