package com.thekeeperofpie.artistalleydatabase.alley.map

import androidx.annotation.FloatRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen.TransformState.Companion.SHOW_TEXT_MIN_SCALE
import com.thekeeperofpie.artistalleydatabase.utils_compose.ZoomSlider
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
object MapScreen {

    const val MAX_ZOOM: Float = 5f

    val ITEM_WIDTH = 80.dp
    val ITEM_HEIGHT = 64.dp

    @Composable
    operator fun invoke(
        viewModel: MapViewModel,
        transformState: TransformState,
        modifier: Modifier = Modifier,
        initialGridPosition: IntOffset? = null,
        showSlider: Boolean = true,
        bottomContentPadding: Dp = 0.dp,
        content: @Composable (Table) -> Unit,
    ) {
        val gridData = viewModel.gridData.result
        Surface(color = MaterialTheme.colorScheme.background, modifier = modifier) {
            Box {
                Map(
                    gridData = gridData,
                    transformState = transformState,
                    initialGridPosition = initialGridPosition,
                    bottomContentPadding = if (showSlider) {
                        bottomContentPadding + 100.dp
                    } else {
                        bottomContentPadding
                    },
                    modifier = Modifier.fillMaxSize(),
                    content = content,
                )

                if (showSlider) {
                    ZoomSlider(
                        transformState = transformState,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .widthIn(max = 480.dp)
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }

    @Composable
    fun ZoomSlider(transformState: TransformState, modifier: Modifier = Modifier) {
        val scope = rememberCoroutineScope()
        ZoomSlider(
            scale = { transformState.userScale },
            scaleRange = 0f..1f,
            onScaleChange = {
                scope.launch { transformState.updateScaleFromUser(it) }
            },
            onScaleChangeFinished = transformState::onGestureEnd,
            onClickZoomOut = {
                scope.launch { transformState.updateScale(transformState.scale * 0.9f) }
            },
            onClickZoomIn = {
                scope.launch { transformState.updateScale(transformState.scale * 1.1f) }
            },
            modifier = modifier,
        )
    }

    @Composable
    private fun Map(
        gridData: MapViewModel.GridData?,
        transformState: TransformState,
        modifier: Modifier = Modifier,
        initialGridPosition: IntOffset? = null,
        bottomContentPadding: Dp = 0.dp,
        content: @Composable (Table) -> Unit,
    ) {
        if (gridData != null) {
            val density = LocalDensity.current
            val contentPaddingPixels = density.run { 72.dp.toPx() }
            val bottomContentPaddingPixels = density.run { bottomContentPadding.toPx() }

            val baseItemWidth = density.run { ITEM_WIDTH.toPx() }
            val baseItemHeight = density.run { ITEM_HEIGHT.toPx() }
            val (width, height) = transformState.size

            val gridAreaWidth = (gridData.maxX + 1) * baseItemWidth
            val gridAreaHeight = (gridData.maxY + 1) * baseItemHeight
            val availableWidth = (width - 2 * contentPaddingPixels).coerceAtLeast(1f)
            val availableHeight = (height - 2 * contentPaddingPixels - bottomContentPaddingPixels)
                .coerceAtLeast(1f)

            val newScale = Snapshot.withMutableSnapshot {
                transformState.scaleRange =
                    minOf(availableWidth / gridAreaWidth, availableHeight / gridAreaHeight)
                        .coerceAtMost(transformState.scaleRange.endInclusive)..transformState.scaleRange.endInclusive
                transformState.scale.coerceIn(transformState.scaleRange)
                    .also { transformState.scale = it }
            }

            val itemWidthPixels = baseItemWidth * newScale
            val itemHeightPixels = baseItemHeight * newScale

            val stableItemWidthPixels = baseItemWidth * transformState.layoutScale
            val stableItemHeightPixels = baseItemHeight * transformState.layoutScale

            transformState.layoutContext = GridLayoutInput(
                gridX = gridData.maxX,
                gridY = gridData.maxY,
                baseItemWidth = baseItemWidth,
                baseItemHeight = baseItemHeight,
                paddingX = contentPaddingPixels,
                bottomContentPaddingPixels = bottomContentPaddingPixels,
            )
            transformState.updateBounds()
            LaunchedEffect(transformState.size) {
                transformState.translation.snapTo(transformState.translation.targetValue)
            }

            val itemProvider = remember(gridData, content) {
                ItemProvider(
                    gridData = gridData,
                    content = content,
                )
            }

            val coroutineScope = rememberCoroutineScope()
            val maxFlingVelocity = (LocalViewConfiguration.current.maximumFlingVelocity / 2f)
                .let { Velocity(it, it) }

            val quantizedTranslation by remember {
                derivedStateOf {
                    val translation = transformState.translation.value
                    Offset(
                        x = (translation.x / 32f).roundToInt() * 32f,
                        y = (translation.y / 32f).roundToInt() * 32f
                    )
                }
            }
            val quantizedScale by remember {
                derivedStateOf { (transformState.scale * 10f).roundToInt() / 10f }
            }
            LazyLayout(
                itemProvider = { itemProvider },
                modifier = modifier
                    .onSizeChanged { size ->
                        transformState.size = size
                        if (transformState.initialized) return@onSizeChanged
                        transformState.initialized = true
                        transformState.layoutScale = transformState.scale
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
                        awaitEachGesture {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Scroll) {
                                val change = event.changes.first()
                                change.consume()
                                val deltaY = change.scrollDelta.y
                                val zoom = if (deltaY > 0f) {
                                    0.9f
                                } else {
                                    1.1f
                                }

                                // This doesn't align perfectly, but it's good enough
                                val position = change.position
                                coroutineScope.launch {
                                    transformState.translation.stop()
                                    transformState.updateScale(
                                        newRawScale = transformState.scale * zoom,
                                        centroid = position,
                                    )
                                    transformState.onGestureEnd()
                                }
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
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
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
                                            // Only allow gesture zoom if it wouldn't hide text,
                                            // to avoid user having to target a very specific zoom
                                            // to still read the map
                                            val newRawScale = transformState.scale * zoomChange
                                            if (transformState.scale < SHOW_TEXT_MIN_SCALE || newRawScale >= SHOW_TEXT_MIN_SCALE) {
                                                coroutineScope.launch {
                                                    transformState.onTransform(
                                                        centroid = centroid,
                                                        translate = panChange,
                                                        newRawScale = newRawScale,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (event.changes
                                            .fastFilter { it.id == initialDown.id }
                                            .fastAny(PointerInputChange::changedToUp)
                                    ) {
                                        transformState.onGestureEnd()
                                        coroutineScope.launch {
                                            val rawVelocity =
                                                velocityTracker.calculateVelocity(maxFlingVelocity)
                                            val translation = transformState.translation
                                            val lowerBound = translation.lowerBound
                                            val upperBound = translation.upperBound
                                            var adjustedVelocityX = rawVelocity.x
                                            var adjustedVelocityY = rawVelocity.y
                                            if (lowerBound != null && upperBound != null) {
                                                val paddingX = transformState.layoutContext.paddingX
                                                val isNearBoundsX =
                                                    (translation.value.x - lowerBound.x < paddingX)
                                                            || (upperBound.x - translation.value.x) < paddingX
                                                val isNearBoundsY =
                                                    (translation.value.y - lowerBound.y < paddingX)
                                                            || (upperBound.y - translation.value.y) < paddingX
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

                                    if (event.changes.size > 1 || pastTouchSlop) {
                                        event.changes.fastForEach {
                                            if (it.positionChanged()) {
                                                it.consume()
                                            }
                                        }
                                    }
                                }
                            } while (!canceled && event.changes.fastAny { it.pressed })
                        }
                    }
                    .clipToBounds()
            ) { constraints ->
                val boundaries = getBounds(
                    offset = quantizedTranslation,
                    scale = quantizedScale,
                    constraints = constraints,
                    paddingX = contentPaddingPixels,
                    paddingY = contentPaddingPixels + bottomContentPaddingPixels,
                    baseItemWidth = baseItemWidth,
                    baseItemHeight = baseItemHeight,
                )
                val visibleTables = itemProvider.getVisibleTables(boundaries = boundaries)

                val itemConstraints = Constraints(
                    minWidth = stableItemWidthPixels.toInt(),
                    minHeight = stableItemHeightPixels.toInt(),
                    maxWidth = stableItemWidthPixels.toInt(),
                    maxHeight = stableItemHeightPixels.toInt(),
                )
                val measured = visibleTables.map { (index, table) ->
                    table to compose(index).map { it.measure(itemConstraints) }
                }

                layout(constraints.maxWidth, constraints.maxHeight) {
                    val visualScale = transformState.scale / transformState.layoutScale
                    measured.forEach { (table, placeables) ->
                        val offsetX = table.gridX * itemWidthPixels
                        val offsetY = -table.gridY * itemHeightPixels
                        val translation = transformState.translation.value
                        val xPosition = contentPaddingPixels + offsetX - translation.x
                        val yPosition = offsetY - translation.y - contentPaddingPixels -
                                bottomContentPaddingPixels
                        placeables.forEach {
                            it.placeRelativeWithLayer(
                                x = xPosition.toInt(),
                                y = yPosition.toInt(),
                            ) {
                                scaleX = visualScale
                                scaleY = visualScale
                                transformOrigin = TransformOrigin(0f, 0f)
                            }
                        }
                    }
                }
            }
        }
    }

    data class GridLayoutInput(
        val gridX: Int = 0,
        val gridY: Int = 0,
        val baseItemWidth: Float = 0f,
        val baseItemHeight: Float = 0f,
        val paddingX: Float = 0f,
        val bottomContentPaddingPixels: Float = 0f,
    )

    class ItemProvider(
        private val gridData: MapViewModel.GridData,
        private val content: @Composable (Table) -> Unit,
    ) : LazyLayoutItemProvider {
        override val itemCount = gridData.tables.size

        private val tablesByRow = gridData.tables.asSequence()
            .withIndex()
            .groupBy { it.value.gridY }

        @Composable
        override fun Item(index: Int, key: Any) {
            val table = gridData.tables[index]
            content(table)
        }

        override fun getKey(index: Int) = gridData.tables[index].booth

        override fun getContentType(index: Int) = "table"

        override fun getIndex(key: Any) = gridData.tables.indexOfFirst { it.booth == key }

        fun getVisibleTables(boundaries: LayoutBounds) =
            boundaries.gridRangeY
                .flatMap {
                    tablesByRow[it]
                        ?.filter { it.value.gridX in boundaries.gridRangeX }
                        .orEmpty()
                }
    }

    data class LayoutBounds(val gridRangeX: IntRange, val gridRangeY: IntRange)

    private fun getBounds(
        offset: Offset,
        scale: Float,
        constraints: Constraints,
        paddingX: Float,
        paddingY: Float,
        baseItemWidth: Float,
        baseItemHeight: Float,
    ): LayoutBounds {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        val extraWidth = width * 0.5f
        val extraHeight = height * 0.5f

        val itemWidth = baseItemWidth * scale
        val itemHeight = baseItemHeight * scale

        val startX = floor((offset.x - paddingX - extraWidth) / itemWidth).toInt()
        val endX = ceil((offset.x - paddingX + width + extraWidth) / itemWidth).toInt()

        val startY = floor(-(offset.y + paddingY + height + extraHeight) / itemHeight).toInt()
        val endY = ceil(-(offset.y + paddingY - extraHeight) / itemHeight).toInt()

        // Snap to multiple of 4 to avoid large recomposition differences
        val snappedStartX = (startX / 4) * 4
        val snappedEndX = ceil(endX / 4f).toInt() * 4
        val snappedStartY = (startY / 4) * 4
        val snappedEndY = ceil(endY / 4f).toInt() * 4

        return LayoutBounds(
            gridRangeX = snappedStartX..snappedEndX,
            gridRangeY = snappedStartY..snappedEndY,
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
        initialScaleRange: ClosedFloatingPointRange<Float> = 0.5f..MAX_ZOOM,
    ) {
        var layoutContext by mutableStateOf(GridLayoutInput())
        var scaleRange by mutableStateOf(initialScaleRange)
        var size by mutableStateOf(IntSize(0, 0))
        val translation by mutableStateOf(
            Animatable(Offset(initialTranslationX, initialTranslationY), Offset.VectorConverter)
        )

        var scale by mutableFloatStateOf(initialScale)

        var layoutScale by mutableFloatStateOf(initialScale)

        var initialized = initialTranslationY != 0f

        val showImages get() = layoutScale > 3f

        val showText get() = layoutScale > SHOW_TEXT_MIN_SCALE

        val userScale
            get() = if (scaleRange.start == scaleRange.endInclusive) {
                1f
            } else {
                ln(scale / scaleRange.start) / ln(scaleRange.endInclusive / scaleRange.start)
            }

        suspend fun updateScaleFromUser(@FloatRange(0.0, 1.0) scale: Float) {
            updateScale(scaleRange.start * (scaleRange.endInclusive / scaleRange.start).pow(scale))
        }

        suspend fun updateScale(
            newRawScale: Float,
            centroid: Offset = Offset(size.width / 2f, size.height / 2f),
        ) = onTransform(
            centroid = centroid,
            translate = Offset.Zero,
            newRawScale = newRawScale,
        )

        suspend fun onTransform(
            centroid: Offset,
            translate: Offset,
            newRawScale: Float,
        ) {
            val newScale = newRawScale.coerceIn(scaleRange)

            val translationValue = translation.value
            val scaleRatio = newScale / scale

            val paddingX = layoutContext.paddingX
            val paddingY = paddingX + layoutContext.bottomContentPaddingPixels

            val newTranslationX =
                (translationValue.x - paddingX + centroid.x) * scaleRatio + paddingX - centroid.x - translate.x
            val newTranslationY =
                (translationValue.y + paddingY + centroid.y) * scaleRatio - paddingY - centroid.y - translate.y

            scale = newScale
            updateBounds()
            translation.snapTo(Offset(newTranslationX, newTranslationY))

            if (abs(scale - layoutScale) >= 1f) {
                layoutScale = scale
            }
        }

        fun onGestureEnd() {
            layoutScale = scale
        }

        fun updateBounds() {
            val itemWidthPixels = layoutContext.baseItemWidth * scale
            val itemHeightPixels = layoutContext.baseItemHeight * scale
            val maxX = ((layoutContext.gridX + 1) * itemWidthPixels) + 2 * layoutContext.paddingX
            val maxY =
                ((layoutContext.gridY + 1) * itemHeightPixels + layoutContext.bottomContentPaddingPixels) + 2 * layoutContext.paddingX

            val scrollRangeX = maxX - size.width
            val scrollRangeY = maxY - size.height
            val lowerBoundX = if (scrollRangeX < 0) scrollRangeX / 2f else 0f
            val upperBoundX = if (scrollRangeX < 0) scrollRangeX / 2f else scrollRangeX
            val lowerBoundY = if (scrollRangeY < 0) -(maxY + size.height) / 2f else -maxY
            val upperBoundY =
                if (scrollRangeY < 0) -(maxY + size.height) / 2f else -size.height.toFloat()

            translation.updateBounds(
                Offset(lowerBoundX, lowerBoundY),
                Offset(upperBoundX, upperBoundY),
            )
        }

        companion object {
            val Saver: Saver<TransformState, *> = listSaver(
                save = {
                    val translationValue = it.translation.targetValue
                    listOf(
                        translationValue.x,
                        translationValue.y,
                        it.scale,
                        it.scaleRange.start,
                        it.scaleRange.endInclusive,
                    )
                },
                restore = {
                    TransformState(
                        initialTranslationX = it[0],
                        initialTranslationY = it[1],
                        initialScale = it[2],
                        initialScaleRange = it[3]..it[4],
                    )
                }
            )

            const val SHOW_TEXT_MIN_SCALE = 0.4f
        }
    }
}
