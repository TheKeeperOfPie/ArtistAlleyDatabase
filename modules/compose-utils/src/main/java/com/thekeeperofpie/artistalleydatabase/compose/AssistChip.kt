@file:OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Copy of [androidx.compose.material3.AssistChip] to support long click.
 */
@Composable
fun AssistChip(
    onClick: () -> Unit,
    onLongClickLabel: String?,
    onLongClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = AssistChipDefaults.shape,
    colors: ChipColors = assistChipColors(),
    elevation: ChipElevation? = assistChipElevation(),
    border: ChipBorder? = assistChipBorder(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) = Chip(
    modifier = modifier,
    onClick = onClick,
    onLongClickLabel = onLongClickLabel,
    onLongClick = onLongClick,
    enabled = enabled,
    label = label,
    labelTextStyle = MaterialTheme.typography.labelLarge,
    labelColor = colors.labelColor(enabled).value,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    shape = shape,
    colors = colors,
    elevation = elevation,
    border = border?.borderStroke(enabled)?.value,
    minHeight = AssistChipDefaults.Height,
    paddingValues = AssistChipPadding,
    interactionSource = interactionSource
)

/**
 * The padding between the elements in the chip.
 */
private val HorizontalElementsPadding = 8.dp

/**
 * Returns the [PaddingValues] for the assist chip.
 */
private val AssistChipPadding = PaddingValues(horizontal = HorizontalElementsPadding)

/**
 * Creates a [ChipColors] that represents the default container , label, and icon colors used in
 * a flat [AssistChip].
 *
 * @param containerColor the container color of this chip when enabled
 * @param labelColor the label color of this chip when enabled
 * @param leadingIconContentColor the color of this chip's start icon when enabled
 * @param trailingIconContentColor the color of this chip's end icon when enabled
 * @param disabledContainerColor the container color of this chip when not enabled
 * @param disabledLabelColor the label color of this chip when not enabled
 * @param disabledLeadingIconContentColor the color of this chip's start icon when not enabled
 * @param disabledTrailingIconContentColor the color of this chip's end icon when not enabled
 */
@Composable
fun assistChipColors(
    containerColor: Color = Color.Transparent,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    leadingIconContentColor: Color = MaterialTheme.colorScheme.primary,
    trailingIconContentColor: Color = leadingIconContentColor,
    disabledContainerColor: Color = Color.Transparent,
    disabledLabelColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    disabledLeadingIconContentColor: Color = MaterialTheme.colorScheme.onSurface
        .copy(alpha = 0.38f),
    disabledTrailingIconContentColor: Color = disabledLeadingIconContentColor,
): ChipColors = ChipColors(
    containerColor = containerColor,
    labelColor = labelColor,
    leadingIconContentColor = leadingIconContentColor,
    trailingIconContentColor = trailingIconContentColor,
    disabledContainerColor = disabledContainerColor,
    disabledLabelColor = disabledLabelColor,
    disabledLeadingIconContentColor = disabledLeadingIconContentColor,
    disabledTrailingIconContentColor = disabledTrailingIconContentColor
)

@Immutable
class ChipColors constructor(
    private val containerColor: Color,
    private val labelColor: Color,
    private val leadingIconContentColor: Color,
    private val trailingIconContentColor: Color,
    private val disabledContainerColor: Color,
    private val disabledLabelColor: Color,
    private val disabledLeadingIconContentColor: Color,
    private val disabledTrailingIconContentColor: Color
    // TODO(b/113855296): Support other states: hover, focus, drag
) {
    /**
     * Represents the container color for this chip, depending on [enabled].
     *
     * @param enabled whether the chip is enabled
     */
    @Composable
    internal fun containerColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) containerColor else disabledContainerColor)
    }

    /**
     * Represents the label color for this chip, depending on [enabled].
     *
     * @param enabled whether the chip is enabled
     */
    @Composable
    internal fun labelColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) labelColor else disabledLabelColor)
    }

    /**
     * Represents the leading icon's content color for this chip, depending on [enabled].
     *
     * @param enabled whether the chip is enabled
     */
    @Composable
    internal fun leadingIconContentColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(
            if (enabled) leadingIconContentColor else disabledLeadingIconContentColor
        )
    }

    /**
     * Represents the trailing icon's content color for this chip, depending on [enabled].
     *
     * @param enabled whether the chip is enabled
     */
    @Composable
    internal fun trailingIconContentColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(
            if (enabled) trailingIconContentColor else disabledTrailingIconContentColor
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ChipColors) return false

        if (containerColor != other.containerColor) return false
        if (labelColor != other.labelColor) return false
        if (leadingIconContentColor != other.leadingIconContentColor) return false
        if (trailingIconContentColor != other.trailingIconContentColor) return false
        if (disabledContainerColor != other.disabledContainerColor) return false
        if (disabledLabelColor != other.disabledLabelColor) return false
        if (disabledLeadingIconContentColor != other.disabledLeadingIconContentColor) return false
        if (disabledTrailingIconContentColor != other.disabledTrailingIconContentColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + labelColor.hashCode()
        result = 31 * result + leadingIconContentColor.hashCode()
        result = 31 * result + trailingIconContentColor.hashCode()
        result = 31 * result + disabledContainerColor.hashCode()
        result = 31 * result + disabledLabelColor.hashCode()
        result = 31 * result + disabledLeadingIconContentColor.hashCode()
        result = 31 * result + disabledTrailingIconContentColor.hashCode()

        return result
    }
}

@Immutable
class ChipBorder internal constructor(
    private val borderColor: Color,
    private val disabledBorderColor: Color,
    private val borderWidth: Dp,
) {
    /**
     * Represents the [BorderStroke] for this chip, depending on [enabled].
     *
     * @param enabled whether the chip is enabled
     */
    @Composable
    internal fun borderStroke(enabled: Boolean): State<BorderStroke?> {
        return rememberUpdatedState(
            BorderStroke(borderWidth, if (enabled) borderColor else disabledBorderColor)
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ChipBorder) return false

        if (borderColor != other.borderColor) return false
        if (disabledBorderColor != other.disabledBorderColor) return false
        if (borderWidth != other.borderWidth) return false

        return true
    }

    override fun hashCode(): Int {
        var result = borderColor.hashCode()
        result = 31 * result + disabledBorderColor.hashCode()
        result = 31 * result + borderWidth.hashCode()

        return result
    }
}

@Composable
private fun Chip(
    modifier: Modifier,
    onClick: () -> Unit,
    onLongClickLabel: String?,
    onLongClick: () -> Unit,
    enabled: Boolean,
    label: @Composable () -> Unit,
    labelTextStyle: TextStyle,
    labelColor: Color,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    shape: Shape,
    colors: ChipColors,
    elevation: ChipElevation?,
    border: BorderStroke?,
    minHeight: Dp,
    paddingValues: PaddingValues,
    interactionSource: MutableInteractionSource,
) {
    Surface(
        onClick = onClick,
        onLongClickLabel = onLongClickLabel,
        onLongClick = onLongClick,
        modifier = modifier.semantics { role = Role.Button },
        enabled = enabled,
        shape = shape,
        color = colors.containerColor(enabled).value,
        tonalElevation = elevation?.tonalElevation(enabled, interactionSource)?.value ?: 0.dp,
        shadowElevation = elevation?.shadowElevation(enabled, interactionSource)?.value ?: 0.dp,
        border = border,
        interactionSource = interactionSource,
    ) {
        ChipContent(
            label = label,
            labelTextStyle = labelTextStyle,
            labelColor = labelColor,
            leadingIcon = leadingIcon,
            avatar = null,
            trailingIcon = trailingIcon,
            leadingIconColor = colors.leadingIconContentColor(enabled).value,
            trailingIconColor = colors.trailingIconContentColor(enabled).value,
            minHeight = minHeight,
            paddingValues = paddingValues
        )
    }
}

@Composable
@NonRestartableComposable
private fun Surface(
    onClick: () -> Unit,
    onLongClickLabel: String?,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val absoluteElevation = LocalAbsoluteTonalElevation.current + tonalElevation
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalAbsoluteTonalElevation provides absoluteElevation
    ) {
        @Suppress("DEPRECATION_ERROR")
        Box(
            modifier = modifier
                .minimumInteractiveComponentSize()
                .surface(
                    shape = shape,
                    backgroundColor = surfaceColorAtElevation(
                        color = color,
                        elevation = absoluteElevation
                    ),
                    border = border,
                    shadowElevation = shadowElevation
                )
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple(),
                    enabled = enabled,
                    onClick = onClick,
                    onLongClickLabel = onLongClickLabel,
                    onLongClick = onLongClick,
                ),
            propagateMinConstraints = true
        ) {
            content()
        }
    }
}

private fun Modifier.surface(
    shape: Shape,
    backgroundColor: Color,
    border: BorderStroke?,
    shadowElevation: Dp
) = this.shadow(shadowElevation, shape, clip = false)
    .then(if (border != null) Modifier.border(border, shape) else Modifier)
    .background(color = backgroundColor, shape = shape)
    .clip(shape)

@Composable
private fun surfaceColorAtElevation(color: Color, elevation: Dp): Color {
    return if (color == MaterialTheme.colorScheme.surface) {
        MaterialTheme.colorScheme.surfaceColorAtElevation(elevation)
    } else {
        color
    }
}


/**
 * Represents the elevation for a chip in different states.
 */
@Immutable
class ChipElevation internal constructor(
    private val elevation: Dp,
    private val pressedElevation: Dp,
    private val focusedElevation: Dp,
    private val hoveredElevation: Dp,
    private val draggedElevation: Dp,
    private val disabledElevation: Dp
) {
    /**
     * Represents the tonal elevation used in a chip, depending on its [enabled] state and
     * [interactionSource]. This should typically be the same value as the [shadowElevation].
     *
     * Tonal elevation is used to apply a color shift to the surface to give the it higher emphasis.
     * When surface's color is [ColorScheme.surface], a higher elevation will result in a darker
     * color in light theme and lighter color in dark theme.
     *
     * See [shadowElevation] which controls the elevation of the shadow drawn around the chip.
     *
     * @param enabled whether the chip is enabled
     * @param interactionSource the [InteractionSource] for this chip
     */
    @Composable
    internal fun tonalElevation(
        enabled: Boolean,
        interactionSource: InteractionSource
    ): State<Dp> {
        return animateElevation(enabled = enabled, interactionSource = interactionSource)
    }

    /**
     * Represents the shadow elevation used in a chip, depending on its [enabled] state and
     * [interactionSource]. This should typically be the same value as the [tonalElevation].
     *
     * Shadow elevation is used to apply a shadow around the chip to give it higher emphasis.
     *
     * See [tonalElevation] which controls the elevation with a color shift to the surface.
     *
     * @param enabled whether the chip is enabled
     * @param interactionSource the [InteractionSource] for this chip
     */
    @Composable
    internal fun shadowElevation(
        enabled: Boolean,
        interactionSource: InteractionSource
    ): State<Dp> {
        return animateElevation(enabled = enabled, interactionSource = interactionSource)
    }

    @Composable
    private fun animateElevation(
        enabled: Boolean,
        interactionSource: InteractionSource
    ): State<Dp> {
        val interactions = remember { mutableStateListOf<Interaction>() }
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is HoverInteraction.Enter -> {
                        interactions.add(interaction)
                    }
                    is HoverInteraction.Exit -> {
                        interactions.remove(interaction.enter)
                    }
                    is FocusInteraction.Focus -> {
                        interactions.add(interaction)
                    }
                    is FocusInteraction.Unfocus -> {
                        interactions.remove(interaction.focus)
                    }
                    is PressInteraction.Press -> {
                        interactions.add(interaction)
                    }
                    is PressInteraction.Release -> {
                        interactions.remove(interaction.press)
                    }
                    is PressInteraction.Cancel -> {
                        interactions.remove(interaction.press)
                    }
                    is DragInteraction.Start -> {
                        interactions.add(interaction)
                    }
                    is DragInteraction.Stop -> {
                        interactions.remove(interaction.start)
                    }
                    is DragInteraction.Cancel -> {
                        interactions.remove(interaction.start)
                    }
                }
            }
        }

        val interaction = interactions.lastOrNull()

        val target = if (!enabled) {
            disabledElevation
        } else {
            when (interaction) {
                is PressInteraction.Press -> pressedElevation
                is HoverInteraction.Enter -> hoveredElevation
                is FocusInteraction.Focus -> focusedElevation
                is DragInteraction.Start -> draggedElevation
                else -> elevation
            }
        }

        val animatable = remember { Animatable(target, Dp.VectorConverter) }

        if (!enabled) {
            // No transition when moving to a disabled state
            LaunchedEffect(target) { animatable.snapTo(target) }
        } else {
            LaunchedEffect(target) {
                val lastInteraction = when (animatable.targetValue) {
                    pressedElevation -> PressInteraction.Press(Offset.Zero)
                    hoveredElevation -> HoverInteraction.Enter()
                    focusedElevation -> FocusInteraction.Focus()
                    draggedElevation -> DragInteraction.Start()
                    else -> null
                }
                animatable.animateElevation(
                    from = lastInteraction, to = interaction, target = target
                )
            }
        }

        return animatable.asState()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ChipElevation) return false

        if (elevation != other.elevation) return false
        if (pressedElevation != other.pressedElevation) return false
        if (focusedElevation != other.focusedElevation) return false
        if (hoveredElevation != other.hoveredElevation) return false
        if (disabledElevation != other.disabledElevation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = elevation.hashCode()
        result = 31 * result + pressedElevation.hashCode()
        result = 31 * result + focusedElevation.hashCode()
        result = 31 * result + hoveredElevation.hashCode()
        result = 31 * result + disabledElevation.hashCode()
        return result
    }
}

/**
 * Creates a [ChipElevation] that will animate between the provided values according to the
 * Material specification for a flat [AssistChip].
 *
 * @param elevation the elevation used when the [AssistChip] is has no other
 * [Interaction]s
 * @param pressedElevation the elevation used when the chip is pressed.
 * @param focusedElevation the elevation used when the chip is focused
 * @param hoveredElevation the elevation used when the chip is hovered
 * @param draggedElevation the elevation used when the chip is dragged
 * @param disabledElevation the elevation used when the chip is not enabled
 */
@Composable
fun assistChipElevation(
    elevation: Dp = 0.dp,
    pressedElevation: Dp = elevation,
    focusedElevation: Dp = elevation,
    hoveredElevation: Dp = elevation,
    draggedElevation: Dp = 8.dp,
    disabledElevation: Dp = elevation
): ChipElevation = ChipElevation(
    elevation = elevation,
    pressedElevation = pressedElevation,
    focusedElevation = focusedElevation,
    hoveredElevation = hoveredElevation,
    draggedElevation = draggedElevation,
    disabledElevation = disabledElevation
)
/**
 * Creates a [ChipBorder] that represents the default border used in a flat [AssistChip].
 *
 * @param borderColor the border color of this chip when enabled
 * @param disabledBorderColor the border color of this chip when not enabled
 * @param borderWidth the border stroke width of this chip
 */
@Composable
fun assistChipBorder(
    borderColor: Color = MaterialTheme.colorScheme.outline,
    disabledBorderColor: Color = MaterialTheme.colorScheme.onSurface
        .copy(alpha = 0.12f),
    borderWidth: Dp = 1.0.dp,
): ChipBorder = ChipBorder(
    borderColor = borderColor,
    disabledBorderColor = disabledBorderColor,
    borderWidth = borderWidth
)
/**
 * Animates the [Dp] value of [this] between [from] and [to] [Interaction]s, to [target]. The
 * [AnimationSpec] used depends on the values for [from] and [to], see
 * [ElevationDefaults.incomingAnimationSpecForInteraction] and
 * [ElevationDefaults.outgoingAnimationSpecForInteraction] for more details.
 *
 * @param target the [Dp] target elevation for this component, corresponding to the elevation
 * desired for the [to] state.
 * @param from the previous [Interaction] that was used to calculate elevation. `null` if there
 * was no previous [Interaction], such as when the component is in its default state.
 * @param to the [Interaction] that this component is moving to, such as [PressInteraction.Press]
 * when this component is being pressed. `null` if this component is moving back to its default
 * state.
 */
internal suspend fun Animatable<Dp, *>.animateElevation(
    target: Dp,
    from: Interaction? = null,
    to: Interaction? = null
) {
    val spec = when {
        // Moving to a new state
        to != null -> com.thekeeperofpie.artistalleydatabase.compose.ElevationDefaults.incomingAnimationSpecForInteraction(to)
        // Moving to default, from a previous state
        from != null -> com.thekeeperofpie.artistalleydatabase.compose.ElevationDefaults.outgoingAnimationSpecForInteraction(from)
        // Loading the initial state, or moving back to the baseline state from a disabled /
        // unknown state, so just snap to the final value.
        else -> null
    }
    if (spec != null) animateTo(target, spec) else snapTo(target)
}

/**
 * Contains default [AnimationSpec]s used for animating elevation between different [Interaction]s.
 *
 * Typically you should use [animateElevation] instead, which uses these [AnimationSpec]s
 * internally. [animateElevation] in turn is used by the defaults for cards and buttons.
 *
 * @see animateElevation
 */
private object ElevationDefaults {
    /**
     * Returns the [AnimationSpec]s used when animating elevation to [interaction], either from a
     * previous [Interaction], or from the default state. If [interaction] is unknown, then
     * returns `null`.
     *
     * @param interaction the [Interaction] that is being animated to
     */
    fun incomingAnimationSpecForInteraction(interaction: Interaction): AnimationSpec<Dp>? {
        return when (interaction) {
            is PressInteraction.Press -> DefaultIncomingSpec
            is DragInteraction.Start -> DefaultIncomingSpec
            is HoverInteraction.Enter -> DefaultIncomingSpec
            is FocusInteraction.Focus -> DefaultIncomingSpec
            else -> null
        }
    }

    /**
     * Returns the [AnimationSpec]s used when animating elevation away from [interaction], to the
     * default state. If [interaction] is unknown, then returns `null`.
     *
     * @param interaction the [Interaction] that is being animated away from
     */
    fun outgoingAnimationSpecForInteraction(interaction: Interaction): AnimationSpec<Dp>? {
        return when (interaction) {
            is PressInteraction.Press -> DefaultOutgoingSpec
            is DragInteraction.Start -> DefaultOutgoingSpec
            is HoverInteraction.Enter -> HoveredOutgoingSpec
            is FocusInteraction.Focus -> DefaultOutgoingSpec
            else -> null
        }
    }
}

private val OutgoingSpecEasing: Easing = CubicBezierEasing(0.40f, 0.00f, 0.60f, 1.00f)

private val DefaultIncomingSpec = TweenSpec<Dp>(
    durationMillis = 120,
    easing = FastOutSlowInEasing
)

private val DefaultOutgoingSpec = TweenSpec<Dp>(
    durationMillis = 150,
    easing = OutgoingSpecEasing
)

private val HoveredOutgoingSpec = TweenSpec<Dp>(
    durationMillis = 120,
    easing = OutgoingSpecEasing
)

@Composable
internal fun ChipContent(
    label: @Composable () -> Unit,
    labelTextStyle: TextStyle,
    labelColor: Color,
    leadingIcon: @Composable (() -> Unit)?,
    avatar: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    leadingIconColor: Color,
    trailingIconColor: Color,
    minHeight: Dp,
    paddingValues: PaddingValues
) {
    CompositionLocalProvider(
        LocalContentColor provides labelColor,
        LocalTextStyle provides labelTextStyle
    ) {
        Row(
            Modifier
                .defaultMinSize(minHeight = minHeight)
                .padding(paddingValues),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (avatar != null) {
                avatar()
            } else if (leadingIcon != null) {
                CompositionLocalProvider(
                    LocalContentColor provides leadingIconColor, content = leadingIcon
                )
            }
            Spacer(Modifier.width(HorizontalElementsPadding))
            label()
            Spacer(Modifier.width(HorizontalElementsPadding))
            if (trailingIcon != null) {
                CompositionLocalProvider(
                    LocalContentColor provides trailingIconColor, content = trailingIcon
                )
            }
        }
    }
}
