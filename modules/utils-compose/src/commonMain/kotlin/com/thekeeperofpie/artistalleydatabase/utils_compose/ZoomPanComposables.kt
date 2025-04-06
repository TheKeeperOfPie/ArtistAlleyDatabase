@file:OptIn(ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.lerp
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.zoom_in
import artistalleydatabase.modules.utils_compose.generated.resources.zoom_out
import org.jetbrains.compose.resources.stringResource

class MultiZoomPanState private constructor(
    private val states: List<ZoomPanState>,
) {
    constructor(size: Int) : this(states = List(size) { ZoomPanState() })

    operator fun get(index: Int) = states[index]

    companion object {
        val Saver: Saver<MultiZoomPanState, *> = listSaver(
            save = {
                it.states.flatMap {
                    listOf(it.translation.x, it.translation.y, it.scale)
                }
            },
            restore = {
                MultiZoomPanState(
                    states =
                        it.chunked(3).map {
                            ZoomPanState(
                                initialTranslationX = it[0],
                                initialTranslationY = it[1],
                                initialScale = it[2],
                            )
                        }
                )
            }
        )
    }
}

class ZoomPanState(
    initialTranslationX: Float = 0f,
    initialTranslationY: Float = 0f,
    initialScale: Float = 1f,
    var maxTranslationX: Float = 0f,
    var maxTranslationY: Float = 0f,
) {
    var transformableState = TransformableState { zoomChange, panChange, _ ->
        onZoomChange((scale * zoomChange).coerceIn(1f, 5f), translation + panChange)
    }

    companion object {
        val Saver: Saver<ZoomPanState, *> = listSaver(
            save = { listOf(it.translation.x, it.translation.y, it.scale) },
            restore = {
                ZoomPanState(
                    initialTranslationX = it[0],
                    initialTranslationY = it[1],
                    initialScale = it[2],
                )
            }
        )
    }

    var translation by mutableStateOf(Offset(initialTranslationX, initialTranslationY))
    var scale by mutableFloatStateOf(initialScale)
        private set

    fun canPanExternal(): Boolean {
        return scale < 1.1f
    }

    suspend fun toggleZoom(offset: Offset, size: IntSize) {
        val scaleTarget: Float
        val translationTarget: Offset
        if (scale < 1.1f) {
            scaleTarget = 2.5f
            translationTarget = calculateZoomOffset(offset, size, scaleTarget)
        } else {
            scaleTarget = 1f
            translationTarget = Offset.Zero
        }
        transformableState.transform(MutatePriority.UserInput) {
            val scaleStart = scale
            val translationStart = translation
            Animatable(0f).animateTo(1f) {
                scale = lerp(scaleStart, scaleTarget, value)
                translation = lerp(translationStart, translationTarget, value)
            }
        }
    }

    private fun calculateZoomOffset(tapOffset: Offset, size: IntSize, scale: Float): Offset {
        val offsetX = (-(tapOffset.x - (size.width / 2f)) * 2f)
            .coerceIn(-maxTranslationX * (scale - 1f), maxTranslationX * (scale - 1f))
        val offsetY = (-(tapOffset.y - (size.height / 2f)) * 2f)
            .coerceIn(-maxTranslationY * (scale - 1f), maxTranslationY * (scale - 1f))
        return Offset(offsetX, offsetY)
    }
    
    fun onZoomChange(newScale: Float, translation: Offset = this.translation) {
        this.scale = newScale
        this.translation = translation.copy(
            x = translation.x.coerceIn(
                -maxTranslationX * (newScale - 1f),
                maxTranslationX * (newScale - 1f),
            ),
            y = translation.y.coerceIn(
                -maxTranslationY * (newScale - 1f),
                maxTranslationY * (newScale - 1f),
            ),
        )
    }
}

@Composable
fun rememberZoomPanState() = rememberSaveable(LocalDensity.current, saver = ZoomPanState.Saver) {
    ZoomPanState()
}

@Composable
fun ZoomPanBox(
    modifier: Modifier = Modifier,
    state: ZoomPanState = rememberZoomPanState(),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                state.maxTranslationX = it.width / 2f
                state.maxTranslationY = it.height / 2f
            }
            .transformable(
                state = state.transformableState,
                canPan = { state.scale > 1.1f },
                lockRotationOnZoomPan = true,
            )
            .graphicsLayer(
                translationX = state.translation.x,
                translationY = state.translation.y,
                scaleX = state.scale,
                scaleY = state.scale,
            )
            .conditionally(onClick != null) {
                clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick!!,
                )
            },
        content = content,
    )
}

@Composable
fun ZoomSlider(
    scale: () -> Float,
    onScaleChange: (Float) -> Unit,
    scaleRange: ClosedFloatingPointRange<Float>,
    onClickZoomOut: () -> Unit,
    onClickZoomIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        IconButton(onClick = onClickZoomOut) {
            Icon(
                imageVector = Icons.Default.ZoomOut,
                contentDescription = stringResource(Res.string.zoom_out),
            )
        }
        Slider(
            value = scale(),
            onValueChange = onScaleChange,
            valueRange = scaleRange,
            modifier = Modifier
                .weight(1f)
                .clickable(interactionSource = null, indication = null) { /* Consume events */ }
        )
        IconButton(onClick = onClickZoomIn) {
            Icon(
                imageVector = Icons.Default.ZoomIn,
                contentDescription = stringResource(Res.string.zoom_in),
            )
        }
    }
}
