@file:OptIn(ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.utils_compose.charts

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.times
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoWidthText
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.bottomBorder
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeBottom
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd

@Composable
fun <Key, Value> PieChart(
    slices: List<Value>,
    sliceToKey: (Value) -> Key,
    sliceToAmount: (Value) -> Int,
    sliceToColor: (Value) -> Color,
    sliceToText: @Composable (Value) -> String,
    keySave: (Key) -> String,
    keyRestore: (String) -> Key,
    pieMaxHeight: Dp = Dp.Unspecified,
) {
    Row {
        val sliceVisibility = rememberSaveable(
            saver = mapSaver(
                save = { it.mapKeys { keySave(it.key) } },
                restore = {
                    it.mapKeys { keyRestore(it.key) }.entries
                        .map { it.key to it.value as Boolean }
                        .toMutableStateMap()
                }
            )
        ) { mutableStateMapOf() }

        val sliceValues = sliceVisibility.values.toList()
        val total = remember(sliceValues) {
            slices.sumOf {
                val visible = sliceVisibility[sliceToKey(it)] ?: true
                sliceToAmount(it).takeIf { visible } ?: 0
            }.toFloat()
        }

        val brush = remember(sliceValues) {
            val colorStops = slices
                .fold(mutableListOf<Pair<Float, Color>>()) { list, slice ->
                    val color = sliceToColor(slice)
                    val lastValue = list.lastOrNull()?.first ?: 0f
                    val visible = sliceVisibility[sliceToKey(slice)] ?: true
                    val amount = sliceToAmount(slice).takeIf { visible } ?: 0
                    if (amount == 0) return@fold list

                    val portion = amount / total
                    list += lastValue to color
                    list += lastValue + portion to color
                    list
                }
                .ifEmpty {
                    listOf(
                        0f to Color.Transparent,
                        1f to Color.Transparent,
                    )
                }
                .toTypedArray()

            Brush.sweepGradient(*colorStops)
        }

        Box(
            modifier = Modifier
                .padding(16.dp)
                .clip(CircleShape)
                .background(brush, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                .widthIn(max = 280.dp)
                .weight(0.5f, fill = false)
                .heightIn(max = pieMaxHeight)
                .aspectRatio(1f)
        )

        var height by remember { mutableIntStateOf(-1) }
        val showFadingEdge = LocalDensity.current.run { height.toDp() } >= pieMaxHeight
                && pieMaxHeight.isSpecified

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp, bottom = 12.dp)
                .heightIn(max = pieMaxHeight)
                .weight(0.5f, fill = false)
                .fillMaxWidth()
                .onSizeChanged { height = it.height }
                .fadingEdgeBottom(showFadingEdge)
                .run {
                    if (pieMaxHeight.isSpecified) {
                        verticalScroll(rememberScrollState())
                    } else this
                }
        ) {
            slices.forEach { slice ->
                val key = sliceToKey(slice)
                val visible = sliceVisibility[key] ?: true
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .combinedClickable(
                            onClick = { sliceVisibility[key] = !visible },
                            onLongClick = {
                                slices.forEach {
                                    sliceVisibility[sliceToKey(it)] = slice == it
                                }
                            }
                        ),
                ) {
                    val sliceColor = sliceToColor(slice)
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(color = sliceColor)
                            .layout { measurable, constraints ->
                                // Enforce square 1:1 aspect ratio
                                val placeable = measurable.measure(constraints)
                                val boxWidth = placeable.width
                                    .coerceAtLeast(48.dp.toPx().toInt())
                                layout(boxWidth, boxWidth) {
                                    val position = Alignment.Center.align(
                                        IntSize(placeable.width, placeable.height),
                                        IntSize(boxWidth, boxWidth),
                                        layoutDirection
                                    )
                                    placeable.place(position)
                                }
                            }
                    ) {

                        if (total != 0f) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.matchParentSize()
                            ) {
                                val amount = sliceToAmount(slice).takeIf { visible } ?: 0
                                Text(
                                    text = formatWholePercentage(amount / total) + "%",
                                    color = ComposeColorUtils.bestTextColor(sliceColor)
                                        ?: Color.Unspecified,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }

                        // Determines the size of the box regardless of the value shown
                        // TODO: Find a better way to maintain box size?
                        Text(
                            text = "99%",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.alpha(0f),
                        )
                    }

                    Text(
                        text = sliceToText(slice),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .alpha(if (visible) 1f else 0.38f)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            if (showFadingEdge) {
                Spacer(Modifier.height(pieMaxHeight * 0.2f))
            }
        }
    }
}

/**
 * Only supports equally spaced, directly adjacent values.
 */
@Suppress("UnusedReceiverParameter")
@Composable
fun <Value> ColumnScope.BarChart(
    slices: List<Value>,
    sliceToAmount: (Value) -> Int,
    sliceToColor: @Composable (index: Int, value: Value) -> Color,
    sliceToText: @Composable (Value) -> String,
    showBarPadding: Boolean = true,
) {
    if (slices.size > 10) {
        ScrollableBarChart(
            slices = slices,
            sliceToAmount = sliceToAmount,
            sliceToColor = sliceToColor,
            sliceToText = sliceToText,
            showBarPadding = showBarPadding,
        )
    } else {
        FixedBarChart(
            slices = slices,
            sliceToAmount = sliceToAmount,
            sliceToColor = sliceToColor,
            sliceToText = sliceToText,
            showBarPadding = showBarPadding,
        )
    }
}

@Composable
private fun <Value> ScrollableBarChart(
    slices: List<Value>,
    sliceToAmount: (Value) -> Int,
    sliceToColor: @Composable (index: Int, value: Value) -> Color,
    sliceToText: @Composable (Value) -> String,
    showBarPadding: Boolean = true,
) {
    val maxAmount = slices.maxOf(sliceToAmount) * 1.1f
    LazyRow(contentPadding = PaddingValues(end = 32.dp), modifier = Modifier.fadingEdgeEnd()) {
        itemsIndexed(slices) { index, slice ->
            BarChartBar(
                index = index,
                slice = slice,
                maxAmount = maxAmount,
                split = 0,
                onSplitChange = { },
                sliceToAmount = sliceToAmount,
                sliceToColor = sliceToColor,
                sliceToText = sliceToText,
                showBarPadding = showBarPadding,
                modifier = Modifier.width(80.dp)
            )
        }
    }
}

@Composable
private fun <Value> FixedBarChart(
    slices: List<Value>,
    sliceToAmount: (Value) -> Int,
    sliceToColor: @Composable (index: Int, value: Value) -> Color,
    sliceToText: @Composable (Value) -> String,
    showBarPadding: Boolean = true,
) {
    var split by rememberSaveable { mutableIntStateOf(0) }
    var nonZeroSplit by rememberSaveable { mutableIntStateOf(0) }
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .animateContentSize()
    ) {
        val total = slices.sumOf(sliceToAmount).toFloat()
        val alpha by animateFloatAsState(
            targetValue = if (split == 0) 0f else 1f,
            label = "Bar chart segment alpha"
        )

        val firstWeight by animateFloatAsState(
            nonZeroSplit / slices.size.toFloat(),
            label = "Bar chart 1st segment weight"
        )
        val firstPortion =
            slices.take(nonZeroSplit).fold(0) { acc, slice -> acc + sliceToAmount(slice) }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .alpha(alpha)
                .weight(firstWeight.coerceAtLeast(0.001f))
                .bottomBorder(MaterialTheme.colorScheme.onSurface)
                .padding(bottom = 1.5.dp) // Offset the other portion's border width
        ) {
            AutoWidthText(
                text = formatWholePercentage(
                    if (total == 0f) {
                        0f
                    } else {
                        firstPortion / total
                    }
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .fillMaxWidth()
            )
        }

        val spacerWeight by animateFloatAsState(
            if (split == 0) 1f else 0f,
            label = "Bar chart spacer weight"
        )
        if (spacerWeight > 0f) {
            Spacer(modifier = Modifier.weight(spacerWeight))
        }

        val secondWeight by animateFloatAsState(
            (slices.size - nonZeroSplit) / slices.size.toFloat(),
            label = "Bar chart 2nd segment weight"
        )
        val secondPortion = slices.drop(nonZeroSplit)
            .fold(0) { acc, slice -> acc + sliceToAmount(slice) }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .alpha(alpha)
                .weight(secondWeight.coerceAtLeast(0.001f))
                .bottomBorder(MaterialTheme.colorScheme.surfaceTint, width = 2.dp)
        ) {
            AutoWidthText(
                text = formatWholePercentage(
                    if (total == 0f) {
                        0f
                    } else {
                        secondPortion / total
                    }
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .fillMaxWidth()
            )
        }
    }

    Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
        val maxAmount = slices.maxOf(sliceToAmount) * 1.1f
        slices.forEachIndexed { index, it ->
            BarChartBar(
                index = index,
                slice = it,
                maxAmount = maxAmount,
                split = split,
                onSplitChange = {
                    split = it
                    if (it != 0) {
                        nonZeroSplit = it
                    }
                },
                sliceToAmount = sliceToAmount,
                sliceToColor = sliceToColor,
                sliceToText = sliceToText,
                showBarPadding = showBarPadding,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun <Value> BarChartBar(
    index: Int,
    slice: Value,
    maxAmount: Float,
    split: Int,
    onSplitChange: (Int) -> Unit,
    sliceToAmount: (Value) -> Int,
    sliceToColor: @Composable (index: Int, value: Value) -> Color,
    sliceToText: @Composable (Value) -> String,
    showBarPadding: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = sliceToColor(index, slice)

    val alpha by animateFloatAsState(
        if (index >= split) 1f else 0.2f,
        label = "Bar chart color alpha"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable {
                if (split == index || index == 0) {
                    onSplitChange(0)
                } else {
                    onSplitChange(index)
                }
            }
    ) {
        val amount = sliceToAmount(slice)
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .height(240.dp)
                .fillMaxWidth()
                .widthIn(max = 120.dp)
                .run {
                    if (showBarPadding) {
                        padding(horizontal = 8.dp)
                    } else this
                }
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .alpha(alpha)
                    .height((amount / maxAmount) * 240.dp)
                    .run {
                        if (showBarPadding) {
                            clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        } else this
                    }
                    .background(color)
            )
        }

        AutoSizeText(
            text = sliceToText(slice),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 12.dp)
        ) {
            // Reserve height using a fixed text measurement
            Text(
                text = "1",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.alpha(0f)
            )
            AutoSizeText(
                text = amount.toString(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .wrapContentHeight()
            )
        }
    }
}

private fun formatWholePercentage(value: Float) = BigDecimal.fromFloat(value)
    .multiply(BigDecimal.fromInt(100))
    .roundToDigitPositionAfterDecimalPoint(0, RoundingMode.FLOOR)
    .toStringExpanded()
