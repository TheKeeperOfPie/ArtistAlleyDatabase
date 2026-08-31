package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexBoxConfig
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_expand_series
import coil3.request.crossfade
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchUtils
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesDisplayInfo
import com.thekeeperofpie.artistalleydatabase.alley.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.ThemeAwareElevatedCard
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeBottom
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.optionalClickable
import org.jetbrains.compose.resources.stringResource

@Composable
fun MerchRow(
    data: MerchWithUserData?,
    onFavoriteToggle: (Boolean) -> Unit,
    showNotes: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .optionalClickable(onClick)
            .padding(end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        val merch = data?.merch
        FavoriteIconButton(
            entryText = { merch?.name },
            favorite = { data?.userEntry?.favorite },
            onFavoriteToggle = onFavoriteToggle,
        )

        val name = merch?.name
        val icon = name?.let { MerchUtils.toIcon(it, required = true) }
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .sharedElement("merchIcon", merch.name)
            )
            Spacer(Modifier.width(16.dp))
        }

        Column {
            Text(
                text = name.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(
                        visible = merch == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
            if (showNotes && (merch == null || !merch.notes.isNullOrEmpty())) {
                Text(
                    text = merch?.notes.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(
                            visible = merch == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }
        }
    }
}

@Composable
fun SmallSeriesCard(
    seriesId: String,
    seriesTitle: String,
    image: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    faded: Boolean = false,
) {
    val imageState = rememberCoilImageState(image)
    ThemeAwareElevatedCard(
        onClick = onClick,
        modifier = modifier
            .conditionally(faded, Modifier.fadingEdgeBottom(firstStop = 0.2f))
    ) {
        val colors = imageState.colors
        val containerColor = colors.containerColor
            .takeOrElse { MaterialTheme.colorScheme.surfaceVariant }
        Column(modifier = Modifier.background(containerColor)) {
            CoilImage(
                state = imageState,
                model = imageState.request()
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.66f)
                    .sharedElement("seriesImage", seriesId)
            )

            val textColor = colors.textColor
                .takeOrElse { MaterialTheme.colorScheme.onSurfaceVariant }
            Text(
                text = seriesTitle,
                style = MaterialTheme.typography.bodyMediumEmphasized,
                color = textColor,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

fun LazyGridScope.series(
    key: String,
    series: List<SeriesDisplayInfo>,
    image: (SeriesDisplayInfo) -> String?,
    columnCount: Int,
    randomizedIndexes: List<Int>,
    expanded: () -> Boolean,
    onExpanded: () -> Unit,
    onClick: (SeriesDisplayInfo) -> Unit,
) {
    val canExpand = series.size > (2 * columnCount)
    items(
        count = if (expanded() || !canExpand) {
            series.size
        } else {
            (columnCount * 2).coerceAtMost(series.size)
        },
        key = { "$key-series-${series[it].id}" },
    ) {
        val expanded = expanded() || !canExpand
        val series = series[if (expanded) it else randomizedIndexes[it]]
        val faded = !expanded && it >= columnCount
        SmallSeriesCard(
            seriesId = series.id,
            seriesTitle = series.title,
            image = image(series),
            faded = faded,
            onClick = { onClick(series) },
            modifier = Modifier.animateItem()
        )
    }

    if (!expanded() && canExpand) {
        item("$key-seriesExpand", GridUtils.maxSpanFunction) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(onClick = onExpanded) {
                    Text(stringResource(Res.string.alley_expand_series))
                }
            }
        }
    }
}

@OptIn(ExperimentalFlexBoxApi::class)
@Composable
fun MerchChips(
    merch: List<String>,
    onClick: (String) -> Unit,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 32.dp) {
        FlexBox(
            config = FlexBoxConfig {
                wrap(FlexWrap.Wrap)
                gap(8.dp)
            },
        ) {
            merch.forEach {
                val icon = MerchUtils.toIcon(it, required = false)
                AssistChip(
                    leadingIcon = if (icon == null) null else {
                        {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(40.dp)
                                    .sharedElement("merchIcon", it)
                            )
                        }
                    },
                    label = { Text(it) },
                    onClick = { onClick(it) },
                )
            }
        }
    }
}
