package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class StaggeredGridCellsAdaptiveWithMin(private val minSize: Dp, private val minCount: Int) :
    StaggeredGridCells {
    override fun Density.calculateCrossAxisCellSizes(
        availableSize: Int,
        spacing: Int,
    ): IntArray {
        val count = maxOf((availableSize + spacing) / (minSize.roundToPx() + spacing), minCount)
        return calculateCellsCrossAxisSizeImpl(availableSize, count, spacing)
    }
}

private fun calculateCellsCrossAxisSizeImpl(gridSize: Int, slotCount: Int, spacing: Int): IntArray {
    val gridSizeWithoutSpacing = gridSize - spacing * (slotCount - 1)
    val slotSize = gridSizeWithoutSpacing / slotCount
    val remainingPixels = gridSizeWithoutSpacing % slotCount
    return IntArray(slotCount) {
        if (slotSize < 0) {
            0
        } else {
            slotSize + if (it < remainingPixels) 1 else 0
        }
    }
}

object GridUtils {
    val standardWidthAdaptiveCells = GridCells.Adaptive(450.dp)
    val smallWidthAdaptiveCells = GridCells.Adaptive(135.dp)

    val maxSpanFunction: LazyGridItemSpanScope.() -> GridItemSpan =
        { GridItemSpan(maxLineSpan) }

    @Composable
    fun standardWidthSpans() = with(LocalDensity.current) {
        standardWidthAdaptiveLogic(
            availableSize = LocalWindowConfiguration.current.screenWidthDp.roundToPx(),
            spacing = 0,
        )
    }

    private fun Density.standardWidthAdaptiveLogic(availableSize: Int, spacing: Int): List<Int> {
        val count =
            maxOf((availableSize + spacing) / (450.dp.roundToPx() + spacing), 1)
        return calculateCellsCrossAxisSizeImpl(availableSize, count, spacing)
    }

    private fun calculateCellsCrossAxisSizeImpl(
        gridSize: Int,
        slotCount: Int,
        spacing: Int,
    ): List<Int> {
        val gridSizeWithoutSpacing = gridSize - spacing * (slotCount - 1)
        val slotSize = gridSizeWithoutSpacing / slotCount
        val remainingPixels = gridSizeWithoutSpacing % slotCount
        return List(slotCount) {
            slotSize + if (it < remainingPixels) 1 else 0
        }
    }
}
