package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.skipToLookaheadSize
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import org.jetbrains.compose.resources.stringResource

@Composable
fun StampRallyTitle(
    year: DataYear,
    id: String,
    hostTable: String?,
    fandom: String?,
    useSharedElement: Boolean = true,
) {
    SelectionContainer {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val isCurrentYear = remember(year) { AlleyUtils.isCurrentYear(year) }
            if (!isCurrentYear) {
                Text(text = "${stringResource(year.shortName)} - ")
            }

            Text(
                text = hostTable.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .conditionally(useSharedElement) {
                        sharedElement("hostTable", id)
                    }
                    .placeholder(
                        visible = hostTable == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )

            Text(text = " - ", modifier = Modifier.skipToLookaheadSize())

            Text(
                text = fandom.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .conditionally(useSharedElement) {
                        sharedElement("fandom", id)
                    }
                    .weight(1f)
                    .placeholder(
                        visible = hostTable == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }
}

@Composable
fun StampRallySeriesImage(
    stampRallyId: String,
    seriesId: String?,
    startTable: String?,
    image: () -> String?,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxHeight()
            .width(72.dp)
            .heightIn(min = 80.dp)
    ) {
        val image = image()
        if (image != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(image)
                    .build(),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .sharedElement("seriesImage", seriesId)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        } else {
            val textStyle = MaterialTheme.typography.titleLarge
            Text(
                text = startTable.orEmpty(),
                style = textStyle.copy(fontFamily = FontFamily.Monospace),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 12.sp,
                    maxFontSize = textStyle.fontSize,
                ),
                modifier = Modifier
                    .sharedElement("hostTable", stampRallyId, zIndexInOverlay = 1f)
                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )
        }
    }
}
