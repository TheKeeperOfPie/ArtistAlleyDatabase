package com.thekeeperofpie.artistalleydatabase.compose.placeholder

import androidx.annotation.FloatRange
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.thekeeperofpie.artistalleydatabase.android_utils.AnimationUtils.lerp
import kotlin.math.max

interface PlaceholderHighlight {
    val animationSpec: InfiniteRepeatableSpec<Float>?

    fun brush(
        @FloatRange(from = 0.0, to = 1.0) progress: Float,
        size: Size
    ): Brush

    @FloatRange(from = 0.0, to = 1.0)
    fun alpha(progress: Float): Float

    companion object {

        fun fade(
            highlightColor: Color,
            animationSpec: InfiniteRepeatableSpec<Float> = PlaceholderDefaults.fadeAnimationSpec,
        ): PlaceholderHighlight = Fade(
            highlightColor = highlightColor,
            animationSpec = animationSpec,
        )

        fun shimmer(
            highlightColor: Color,
            animationSpec: InfiniteRepeatableSpec<Float> = PlaceholderDefaults.shimmerAnimationSpec,
            @FloatRange(from = 0.0, to = 1.0) progressForMaxAlpha: Float = 0.6f,
        ): PlaceholderHighlight = Shimmer(
            highlightColor = highlightColor,
            animationSpec = animationSpec,
            progressForMaxAlpha = progressForMaxAlpha,
        )

        @Composable
        fun fade(
            animationSpec: InfiniteRepeatableSpec<Float> = PlaceholderDefaults.fadeAnimationSpec,
        ): PlaceholderHighlight = PlaceholderHighlight.fade(
            highlightColor = PlaceholderDefaults.fadeHighlightColor(),
            animationSpec = animationSpec,
        )

        @Composable
        fun shimmer(
            animationSpec: InfiniteRepeatableSpec<Float> = PlaceholderDefaults.shimmerAnimationSpec,
            @FloatRange(from = 0.0, to = 1.0) progressForMaxAlpha: Float = 0.6f,
        ): PlaceholderHighlight = PlaceholderHighlight.shimmer(
            highlightColor = PlaceholderDefaults.shimmerHighlightColor(),
            animationSpec = animationSpec,
            progressForMaxAlpha = progressForMaxAlpha,
        )
    }
}

private data class Fade(
    private val highlightColor: Color,
    override val animationSpec: InfiniteRepeatableSpec<Float>,
) : PlaceholderHighlight {
    private val brush = SolidColor(highlightColor)

    override fun brush(progress: Float, size: Size): Brush = brush
    override fun alpha(progress: Float): Float = progress
}

private data class Shimmer(
    private val highlightColor: Color,
    override val animationSpec: InfiniteRepeatableSpec<Float>,
    private val progressForMaxAlpha: Float = 0.6f,
) : PlaceholderHighlight {
    override fun brush(
        progress: Float,
        size: Size,
    ): Brush = Brush.radialGradient(
        colors = listOf(
            highlightColor.copy(alpha = 0f),
            highlightColor,
            highlightColor.copy(alpha = 0f),
        ),
        center = Offset(x = 0f, y = 0f),
        radius = (max(size.width, size.height) * progress * 2).coerceAtLeast(0.01f),
    )

    override fun alpha(progress: Float): Float = when {
        // From 0f...ProgressForOpaqueAlpha we animate from 0..1
        progress <= progressForMaxAlpha -> {
            lerp(
                start = 0f,
                end = 1f,
                progress = progress / progressForMaxAlpha
            )
        }
        // From ProgressForOpaqueAlpha..1f we animate from 1..0
        else -> {
            lerp(
                start = 1f,
                end = 0f,
                progress = (progress - progressForMaxAlpha) / (1f - progressForMaxAlpha)
            )
        }
    }
}
