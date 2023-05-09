package com.thekeeperofpie.artistalleydatabase.compose

import android.icu.text.DecimalFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun <Key, Value> PieChart(
    slices: List<Value>,
    sliceToKey: (Value) -> Key,
    sliceToAmount: (Value) -> Int,
    sliceToColor: (Value) -> Color,
    sliceToText: @Composable (Value) -> String,
    keySave: (Key) -> String,
    keyRestore: (String) -> Key,
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
                .aspectRatio(1f)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        ) {
            val format = remember { DecimalFormat("#%") }
            slices.forEach {
                val visible = sliceVisibility[sliceToKey(it)] ?: true
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .clickable { sliceVisibility[sliceToKey(it)] = !visible },
                ) {
                    val sliceColor = sliceToColor(it)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(color = sliceColor)
                            .aspectRatio(1f)
                            .padding(4.dp)
                    ) {
                        if (total != 0f) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.matchParentSize()
                            ) {
                                val amount = sliceToAmount(it).takeIf { visible } ?: 0
                                AutoHeightText(
                                    text = format.format(amount / total),
                                    color = ColorUtils.bestTextColor(sliceColor)
                                        ?: Color.Unspecified,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }

                        // Determines the size of the box regardless of the value shown
                        // TODO: Find a better way to maintain box size
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.alpha(0f),
                        )
                    }

                    Text(
                        text = sliceToText(it),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .alpha(if (visible) 1f else 0.38f)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
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
    sliceToColor: (index: Int, value: Value) -> Color,
    sliceToText: @Composable (Value) -> String,
) {
    var split by rememberSaveable { mutableStateOf(0) }
    val format = remember { DecimalFormat("#%") }
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
    ) {
        val total = slices.sumOf(sliceToAmount).toFloat()
        val firstWeight = split / slices.size.toFloat()
        val firstPortion = slices.take(split).fold(0) { acc, slice -> acc + sliceToAmount(slice) }
        if (firstWeight != 0f) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .alpha(if (split == 0) 0f else 1f)
                    .weight(firstWeight)
                    .padding(bottom = 1.5.dp) // Offset the other portion's border width
                    .bottomBorder(MaterialTheme.colorScheme.onSurface)
            ) {
                AutoWidthText(
                    text = format.format(firstPortion / total),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 2.dp)
                        .fillMaxWidth()
                )
            }
        }

        val secondWeight = (slices.size - split) / slices.size.toFloat()
        if (secondWeight != 0f) {
            val secondPortion = slices.drop(split)
                .fold(0) { acc, slice -> acc + sliceToAmount(slice) }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .alpha(if (split == 0) 0f else 1f)
                    .weight(secondWeight)
                    .bottomBorder(MaterialTheme.colorScheme.surfaceTint, width = 2.dp)
            ) {
                AutoWidthText(
                    text = format.format(secondPortion / total),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 2.dp)
                        .fillMaxWidth()
                )
            }
        }
    }

    Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
        val maxAmount = slices.maxOf { sliceToAmount(it) } * 1.1f
        slices.forEachIndexed { index, it ->
            val color = sliceToColor(index, it).copy(alpha = if (index >= split) 1f else 0.2f)

            Column(modifier = Modifier
                .weight(1f)
                .clickable { split = index }
            ) {
                val amount = sliceToAmount(it)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f - (amount / maxAmount) to Color.Transparent,
                                1f - (amount / maxAmount) to color,
                                1f to color,
                            )
                        )
                )

                AutoSizeText(
                    text = sliceToText(it),
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
    }
}
