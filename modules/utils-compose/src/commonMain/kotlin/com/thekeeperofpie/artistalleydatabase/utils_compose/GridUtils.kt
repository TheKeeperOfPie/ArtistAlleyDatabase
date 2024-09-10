package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
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
    val standardMediaWidthAdaptiveCells = GridCells.Adaptive(450.dp)
    val smallMediaWidthAdaptiveCells = GridCells.Adaptive(135.dp)
}
