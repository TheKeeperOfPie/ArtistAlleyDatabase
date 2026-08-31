package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.series.otherTitles
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagUtils
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.skipToLookaheadSize
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArtistTitle(
    year: DataYear,
    id: String?,
    booth: String?,
    profileImage: CatalogImage?,
    name: String?,
    useSharedElement: Boolean = true,
) {
    SelectionContainer {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            ArtistProfileImage(image = profileImage, artistId = id)
            Spacer(Modifier.width(12.dp))

            val isCurrentYear = remember(year) { AlleyUtils.isCurrentYear(year) }
            if (!isCurrentYear) {
                Text(text = "${stringResource(year.shortName)} - ")
            }

            if (name == null || booth != null) {
                Text(
                    text = if (name == null) "" else booth!!,
                    style = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .conditionally(useSharedElement) {
                            sharedElement("booth", id)
                        }
                        .placeholder(
                            visible = name == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )

                )
                Text(text = " - ", modifier = Modifier.skipToLookaheadSize())
            }

            Text(
                text = name.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .conditionally(useSharedElement) {
                        sharedElement("name", id)
                    }
                    .weight(1f)
                    .placeholder(
                        visible = name == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }
}

@Composable
fun ArtistProfileImage(
    image: CatalogImage?,
    modifier: Modifier = Modifier,
    booth: String? = null,
    useSharedElements: Boolean = false,
    artistId: String? = null,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.matchParentSize()
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    image?.color?.let(::Color) ?: MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(8.dp)
                )
        ) {
            if (image != null) {
                AsyncImage(
                    model = image.coilImageModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .conditionally(
                            useSharedElements,
                            Modifier.sharedElement("profile_image", artistId)
                        )
                        .matchParentSize()
                )
            }
        }

        if (booth != null) {
            val textStyle = MaterialTheme.typography.labelLarge
            Text(
                text = booth.replace("-", "\n"),
                style = textStyle,
                textAlign = TextAlign.End,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 12.sp,
                    maxFontSize = textStyle.fontSize,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(
                        shape = RoundedCornerShape(topStart = 8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                    )
                    .padding(horizontal = 2.dp)
            )
        }
    }
}


@Composable
internal fun MerchRow(
    merchHighlighted: List<String>,
    merchRemaining: List<String>,
    hasMoreMerch: Boolean,
    onMerchClick: (String) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .fadingEdgeEnd(
                startOpaque = 0.dp,
                startTransparent = 0.dp,
                endOpaque = 32.dp,
                endTransparent = 16.dp,
            )
    ) {
        val highlightedColors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primary,
            labelColor = MaterialTheme.colorScheme.onPrimary,
        )
        val remainingColors = AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        val highlightedBorder = AssistChipDefaults.assistChipBorder(enabled = true)
        val remainingBorder = AssistChipDefaults.assistChipBorder(enabled = false)
        merchHighlighted.take(TagUtils.TAGS_TO_SHOW).forEach {
            AssistChip(
                colors = highlightedColors,
                border = highlightedBorder,
                onClick = { onMerchClick(it) },
                label = { Text(text = it) },
                modifier = Modifier.height(24.dp)
            )
        }
        merchRemaining.take((TagUtils.TAGS_TO_SHOW - merchHighlighted.size).coerceAtLeast(0))
            .forEach {
                AssistChip(
                    colors = remainingColors,
                    border = remainingBorder,
                    onClick = { onMerchClick(it) },
                    label = { Text(text = it) },
                    modifier = Modifier.height(24.dp)
                )
            }
        if (hasMoreMerch) {
            AssistChip(
                colors = remainingColors,
                border = remainingBorder,
                onClick = onMoreClick,
                label = { Text("...") },
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

@Composable
fun SeriesPrediction(query: String, series: SeriesInfo, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        val languageOptionMedia = LocalLanguageOptionMedia.current
        val title = buildAnnotatedString {
            val name = series.name(languageOptionMedia)
            append(if (series.faked) "\"${name}\"" else name)
            if (!series.faked) {
                val startIndex = name.indexOf(query, ignoreCase = true)
                if (startIndex >= 0) {
                    addStyle(
                        style = SpanStyle(color = MaterialTheme.colorScheme.secondary),
                        start = startIndex,
                        end = startIndex + query.length,
                    )
                }
            }
        }
        Text(text = title)

        if (!series.faked) {
            val otherTitles = series.otherTitles(languageOptionMedia)
            if (otherTitles.isNotEmpty()) {
                val text = buildAnnotatedString {
                    val value = otherTitles.joinToString(" / ")
                    append(value)
                    val startIndex = value.indexOf(query, ignoreCase = true)
                    if (startIndex >= 0) {
                        addStyle(
                            style = SpanStyle(color = MaterialTheme.colorScheme.secondary),
                            start = startIndex,
                            end = startIndex + query.length,
                        )
                    }
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

