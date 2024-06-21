package com.thekeeperofpie.artistalleydatabase.alley.map

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel

@OptIn(ExperimentalFoundationApi::class)
object MapScreen {

    private val itemWidth = 80.dp
    private val itemHeight = 64.dp

    @Composable
    operator fun invoke(
        transformState: TransformState,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
        modifier: Modifier = Modifier,
        content: @Composable (Table) -> Unit,
    ) {
        val viewModel = hiltViewModel<MapViewModel>()
        val gridData = viewModel.gridData.result
        if (gridData != null) {
            val onArtistClickState by rememberUpdatedState(onArtistClick)
            val contentState by rememberUpdatedState(content)
            val itemProvider = remember(gridData) {
                ItemProvider(
                    viewModel = viewModel,
                    gridData = gridData,
                    onArtistClick = onArtistClickState,
                    content = contentState,
                )
            }
            val contentPaddingPixels = LocalDensity.current.run { 32.dp.toPx() }

            val itemWidthPixels =
                LocalDensity.current.run { itemWidth.toPx() } * transformState.scale
            val itemHeightPixels =
                LocalDensity.current.run { itemHeight.toPx() } * transformState.scale
            val width = transformState.size.width
            val height = transformState.size.height
            val maxX = ((gridData.maxRow + 1) * itemWidthPixels).coerceAtLeast(width.toFloat())
            val maxY = ((gridData.maxColumn + 1) * itemHeightPixels).coerceAtLeast(height.toFloat())

            transformState.xRange = 0f..maxX - width
            transformState.yRange = -maxY..-height.toFloat()
            transformState.scaleRange = (width / maxX).coerceAtMost(height / maxY)..3f

            LazyLayout(
                itemProvider = { itemProvider },
                modifier = modifier
                    .onSizeChanged { transformState.onSizeChange(it) }
                    .transformable(transformState.transformableState)
                    .fillMaxSize()
                    .clipToBounds()
            ) { constraints ->
                val boundaries = getBounds(
                    offset = transformState.translation,
                    itemHeightPixels = itemWidthPixels,
                    itemWidthPixels = itemHeightPixels,
                    constraints = constraints,
                )
                val visibleTables = itemProvider.getVisibleTables(
                    itemWidthPixels = itemWidthPixels,
                    itemHeightPixels = itemHeightPixels,
                    boundaries = boundaries,
                )

                val itemConstraints = Constraints(
                    minWidth = itemWidthPixels.toInt(),
                    minHeight = itemHeightPixels.toInt(),
                    maxWidth = itemWidthPixels.toInt(),
                    maxHeight = itemHeightPixels.toInt(),
                )
                val measured = visibleTables.map { (index, table) ->
                    table to measure(index, itemConstraints)
                }

                layout(constraints.maxWidth, constraints.maxHeight) {
                    measured.forEach { (table, placeables) ->
                        val offsetX = table.gridX * itemWidthPixels
                        val offsetY = -table.gridY * itemHeightPixels
                        val translation = transformState.translation
                        val xPosition = contentPaddingPixels + offsetX - translation.x
                        val yPosition = offsetY - translation.y - contentPaddingPixels
                        placeables.forEach {
                            it.placeRelative(xPosition.toInt(), yPosition.toInt())
                        }
                    }
                }
            }
        }
    }

    class ItemProvider(
        private val viewModel: MapViewModel,
        private val gridData: MapViewModel.GridData,
        private val onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
        private val content: @Composable (Table) -> Unit,
    ) : LazyLayoutItemProvider {
        override val itemCount = gridData.tables.size

        @Composable
        override fun Item(index: Int, key: Any) {
            val table = gridData.tables[index]
            content(table)
        }

        fun getVisibleTables(
            itemWidthPixels: Float,
            itemHeightPixels: Float,
            boundaries: LayoutBounds,
        ) = gridData.tables.asSequence()
            .withIndex()
            .filter { (index, table) ->
                val offsetX = table.gridX * itemWidthPixels
                val offsetY = -table.gridY * itemHeightPixels
                // Couldn't get X laziness to work
                /*boundaries.rangeX.contains(offsetX) && */boundaries.rangeY.contains(offsetY)
            }
            .toList()
    }

    data class LayoutBounds(
        val rangeX: ClosedFloatingPointRange<Float>,
        val rangeY: ClosedFloatingPointRange<Float>,
    )

    private fun getBounds(
        offset: Offset,
        itemHeightPixels: Float,
        itemWidthPixels: Float,
        constraints: Constraints,
    ): LayoutBounds {
        val maxWidth = constraints.maxWidth.toFloat()
        val maxHeight = constraints.maxHeight.toFloat()
        val extraWidth = itemWidthPixels * 2f
        val extraHeight = itemHeightPixels * 2f
        return LayoutBounds(
            offset.x - extraWidth..maxWidth + extraWidth,
            offset.y - extraHeight..maxHeight + extraHeight,
        )
    }

    @Composable
    fun rememberTransformState(initialScale: Float = 1f) =
        rememberSaveable(LocalDensity.current, saver = TransformState.Saver) {
            TransformState(initialScale = initialScale)
        }

    class TransformState(
        initialTranslationX: Float = 0f,
        initialTranslationY: Float = 0f,
        initialScale: Float = 1f,
        var layoutSize: IntSize = IntSize(0, 0),
        var xRange: ClosedFloatingPointRange<Float> = 0f..0f,
        var yRange: ClosedFloatingPointRange<Float> = 0f..0f,
        var scaleRange: ClosedFloatingPointRange<Float> = 0.5f..3f,
    ) {
        var size by mutableStateOf(IntSize(0, 0))
            private set
        var translation by mutableStateOf(Offset(initialTranslationX, initialTranslationY))
        var scale by mutableFloatStateOf(initialScale)

        fun onSizeChange(size: IntSize) {
            this.size = size
            if (translation.y == 0f) {
                translation = translation.copy(y = -size.height.toFloat())
            }
        }

        val transformableState = TransformableState { zoomChange, panChange, _ ->
            val translation = translation - panChange
            val scale = this.scale
            val newScale = scale * zoomChange
            val direction = if (newScale > scale) 1f else -1f
            val extraOffsetX = (scale - newScale) * layoutSize.width * direction
            val extraOffsetY = (scale - newScale) * layoutSize.height * direction
            this.translation = translation.copy(
                x = (translation.x + extraOffsetX).coerceIn(xRange),
                y = (translation.y + extraOffsetY).coerceIn(yRange),
            )
            this.scale = (scale * zoomChange).coerceIn(scaleRange)
        }

        companion object {
            val Saver: Saver<TransformState, *> = listSaver(
                save = { listOf(it.translation.x, it.translation.y, it.scale) },
                restore = {
                    TransformState(
                        initialTranslationX = it[0],
                        initialTranslationY = it[1],
                        initialScale = it[2],
                    )
                }
            )
        }
    }
}
