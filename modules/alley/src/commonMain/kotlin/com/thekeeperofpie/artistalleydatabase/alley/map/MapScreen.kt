package com.thekeeperofpie.artistalleydatabase.alley.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_zoom_in
import artistalleydatabase.modules.alley.generated.resources.alley_zoom_out
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
object MapScreen {

    private val itemWidth = 80.dp
    private val itemHeight = 64.dp

    @Composable
    operator fun invoke(
        viewModel: MapViewModel,
        transformState: TransformState,
        modifier: Modifier = Modifier,
        initialGridPosition: IntOffset? = null,
        showSlider: Boolean = true,
        content: @Composable (Table) -> Unit,
    ) {
        val gridData = viewModel.gridData.result
        Column(modifier = modifier.fillMaxSize()) {
            Map(
                gridData = gridData,
                transformState = transformState,
                initialGridPosition = initialGridPosition,
                modifier = Modifier.weight(1f),
                content = content,
            )

            if (showSlider) {
                ZoomSlider(
                    transformState = transformState,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }

    @Composable
    fun ZoomSlider(transformState: TransformState, modifier: Modifier = Modifier) {
        Row(modifier = modifier) {
            val scope = rememberCoroutineScope()
            IconButton(onClick = {
                scope.launch { transformState.updateScale(transformState.scale - 0.2f) }
            }) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = stringResource(Res.string.alley_zoom_out),
                )
            }
            Slider(
                value = transformState.scale,
                onValueChange = { scope.launch { transformState.updateScale(it) } },
                valueRange = transformState.scaleRange,
                modifier = Modifier
                    .weight(1f)
                    .clickable(interactionSource = null, indication = null) { /* Consume events */ }
            )
            IconButton(onClick = {
                scope.launch { transformState.updateScale(transformState.scale + 0.2f) }
            }) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = stringResource(Res.string.alley_zoom_in),
                )
            }
        }
    }

    @Composable
    private fun Map(
        gridData: MapViewModel.GridData?,
        transformState: TransformState,
        modifier: Modifier = Modifier,
        initialGridPosition: IntOffset? = null,
        content: @Composable (Table) -> Unit,
    ) {
        if (gridData != null) {
            val contentPaddingPixels = LocalDensity.current.run { 32.dp.toPx() }

            val baseItemWidth = LocalDensity.current.run { itemWidth.toPx() }
            val baseItemHeight = LocalDensity.current.run { itemHeight.toPx() }
            val width = transformState.size.width
            val height = transformState.size.height
            val baseMaxX = (gridData.maxX * baseItemWidth)
                .coerceAtLeast(width.toFloat()) + 2 * contentPaddingPixels
            val baseMaxY = ((gridData.maxY + 1) * baseItemHeight)
                .coerceAtLeast(height.toFloat()) + 2 * contentPaddingPixels
            transformState.scaleRange = (width / baseMaxX).coerceAtLeast(height / baseMaxY)..3f

            val itemWidthPixels = baseItemWidth * transformState.scale
            val itemHeightPixels = baseItemHeight * transformState.scale
            val maxX = ((gridData.maxX + 1) * itemWidthPixels)
                .coerceAtLeast(width.toFloat()) + 2 * contentPaddingPixels
            val maxY = ((gridData.maxY + 1) * itemHeightPixels)
                .coerceAtLeast(height.toFloat()) + 2 * contentPaddingPixels

            transformState.translation.updateBounds(
                Offset(0f, -maxY),
                Offset(maxX - width, -height.toFloat()),
            )
            LaunchedEffect(transformState.size) {
                transformState.translation.snapTo(transformState.translation.targetValue)
            }

            val itemProvider = remember(gridData) {
                ItemProvider(
                    gridData = gridData,
                    content = content,
                )
            }

            val coroutineScope = rememberCoroutineScope()
            val maxFlingVelocity = (LocalViewConfiguration.current.maximumFlingVelocity / 2f)
                .let { Velocity(it, it) }
            LazyLayout(
                itemProvider = { itemProvider },
                modifier = modifier
                    .onSizeChanged { size ->
                        transformState.size = size
                        if (transformState.initialized) return@onSizeChanged
                        transformState.initialized = true
                        coroutineScope.launch {
                            transformState.translation.snapTo(Offset(0f, -size.height.toFloat()))
                            if (initialGridPosition != null) {
                                val targetGridPosition = Offset(
                                    initialGridPosition.x * itemWidthPixels + contentPaddingPixels +
                                            (itemWidthPixels / 2),
                                    -initialGridPosition.y * itemHeightPixels + contentPaddingPixels +
                                            (itemHeightPixels / 2),
                                )
                                val targetInDisplaySpace = Offset(
                                    size.width / 2f,
                                    size.height / 2f,
                                )
                                val translation = transformState.translation.value
                                val targetInGridSpace = Offset(
                                    translation.x + targetInDisplaySpace.x,
                                    translation.y + targetInDisplaySpace.y,
                                )
                                val targetTranslation = targetGridPosition - targetInGridSpace
                                transformState.translation.snapTo(
                                    Offset(targetTranslation.x, targetTranslation.y - size.height)
                                )
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        val decay = splineBasedDecay<Offset>(this)
                        val velocityTracker = VelocityTracker()
                        awaitEachGesture {
                            var zoom = 1f
                            var pan = Offset.Zero
                            var pastTouchSlop = false
                            val touchSlop = viewConfiguration.touchSlop

                            val initialDown = awaitFirstDown(requireUnconsumed = false)
                            coroutineScope.launch { transformState.translation.stop() }
                            do {
                                val event = awaitPointerEvent()
                                event.changes.fastForEach {
                                    if (it.id == initialDown.id) {
                                        velocityTracker.addPointerInputChange(it)
                                    }
                                }
                                val canceled = event.changes.fastAny { it.isConsumed }
                                if (!canceled) {
                                    val zoomChange = event.calculateZoom()
                                    val panChange = event.calculatePan()

                                    if (!pastTouchSlop) {
                                        zoom *= zoomChange
                                        pan += panChange

                                        val centroidSize =
                                            event.calculateCentroidSize(useCurrent = false)
                                        val zoomMotion = abs(1 - zoom) * centroidSize
                                        val panMotion = pan.getDistance()

                                        if (zoomMotion > touchSlop || panMotion > touchSlop) {
                                            pastTouchSlop = true
                                        }
                                    }

                                    if (pastTouchSlop) {
                                        val centroid = event.calculateCentroid(useCurrent = false)
                                        if (zoomChange != 1f || panChange != Offset.Zero) {
                                            coroutineScope.launch {
                                                transformState.onTransform(
                                                    centroid = centroid,
                                                    translate = panChange,
                                                    newRawScale = transformState.scale * zoomChange,
                                                )
                                            }
                                        }
                                    }

                                    if (event.changes
                                            .fastFilter { it.id == initialDown.id }
                                            .fastAny(PointerInputChange::changedToUp)
                                    ) {
                                        coroutineScope.launch {
                                            val rawVelocity =
                                                velocityTracker.calculateVelocity(maxFlingVelocity)
                                            val translation = transformState.translation
                                            val lowerBound = translation.lowerBound
                                            val upperBound = translation.upperBound
                                            var adjustedVelocityX = rawVelocity.x
                                            var adjustedVelocityY = rawVelocity.y
                                            if (lowerBound != null && upperBound != null) {
                                                val isNearBoundsX =
                                                    (translation.value.x - lowerBound.x < contentPaddingPixels)
                                                            || (upperBound.x - translation.value.x) < contentPaddingPixels
                                                val isNearBoundsY =
                                                    (translation.value.y - lowerBound.y < contentPaddingPixels)
                                                            || (upperBound.y - translation.value.y) < contentPaddingPixels
                                                if (isNearBoundsX) {
                                                    adjustedVelocityX = 0f
                                                }
                                                if (isNearBoundsY) {
                                                    adjustedVelocityY = 0f
                                                }
                                            }
                                            val velocity = Offset(
                                                -adjustedVelocityX,
                                                -adjustedVelocityY,
                                            )
                                            transformState.translation.animateDecay(
                                                initialVelocity = velocity,
                                                animationSpec = decay,
                                            )
                                        }
                                    }

                                    event.changes.fastForEach {
                                        if (it.positionChanged()) {
                                            it.consume()
                                        }
                                    }
                                }
                            } while (!canceled && event.changes.fastAny { it.pressed })
                        }
                    }
                    .clipToBounds()
            ) { constraints ->
                val boundaries = getBounds(
                    offset = transformState.translation.value,
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
                        val translation = transformState.translation.value
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
        private val gridData: MapViewModel.GridData,
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
        var scaleRange: ClosedFloatingPointRange<Float> = 0.5f..3f,
    ) {
        var size by mutableStateOf(IntSize(0, 0))
        val translation by mutableStateOf(
            Animatable(Offset(initialTranslationX, initialTranslationY), Offset.VectorConverter)
        )

        var scale by mutableFloatStateOf(initialScale)

        var initialized = initialTranslationY != 0f

        suspend fun updateScale(newRawScale: Float) = onTransform(
            centroid = Offset(size.width / 2f, size.height / 2f),
            translate = Offset.Zero,
            newRawScale = newRawScale,
        )

        suspend fun onTransform(centroid: Offset, translate: Offset, newRawScale: Float) {
            val newScale = newRawScale.coerceIn(scaleRange)

            val translationValue = translation.value
            val centroidInGridSpace = Offset(
                translationValue.x + centroid.x,
                translationValue.y + centroid.y,
            )
            val scaleDiff = scale / newScale
            val newCentroidInGridSpace = centroidInGridSpace * scaleDiff
            val centroidDiffInGridSpace =
                centroidInGridSpace - newCentroidInGridSpace
            val newTranslation = translationValue + centroidDiffInGridSpace - translate
            scale = newScale
            translation.snapTo(newTranslation)
        }

        companion object {
            val Saver: Saver<TransformState, *> = listSaver(
                save = {
                    val translationValue = it.translation.targetValue
                    listOf(translationValue.x, translationValue.y, it.scale)
                },
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
