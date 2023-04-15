@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.platform.inspectable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Copy of [androidx.compose.material3.FilterChip] to support long click.
 */
@ExperimentalMaterial3Api
@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    onLongClickLabel: String?,
    onLongClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = FilterChipDefaults.shape,
    colors: SelectableChipColors = filterChipColors(),
    elevation: SelectableChipElevation? = filterChipElevation(),
    border: SelectableChipBorder? = filterChipBorder(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) = SelectableChip(
    selected = selected,
    modifier = modifier,
    onClick = onClick,
    onLongClickLabel = onLongClickLabel,
    onLongClick = onLongClick,
    enabled = enabled,
    label = label,
    labelTextStyle = MaterialTheme.typography.labelLarge,
    leadingIcon = leadingIcon,
    avatar = null,
    trailingIcon = trailingIcon,
    elevation = elevation,
    colors = colors,
    minHeight = FilterChipDefaults.Height,
    paddingValues = FilterChipPadding,
    shape = shape,
    border = border?.borderStroke(enabled, selected)?.value,
    interactionSource = interactionSource
)

/**
 * The padding between the elements in the chip.
 */
private val HorizontalElementsPadding = 8.dp

/**
 * [PaddingValues] for the filter chip.
 */
private val FilterChipPadding =
    PaddingValues(horizontal = HorizontalElementsPadding)

/**
 * Represents the border stroke used used in a selectable chip in different states.
 */
@ExperimentalMaterial3Api
@Immutable
class SelectableChipBorder internal constructor(
    private val borderColor: Color,
    private val selectedBorderColor: Color,
    private val disabledBorderColor: Color,
    private val disabledSelectedBorderColor: Color,
    private val borderWidth: Dp,
    private val selectedBorderWidth: Dp
) {
    /**
     * Represents the [BorderStroke] stroke used for this chip, depending on [enabled] and
     * [selected].
     *
     * @param enabled whether the chip is enabled
     * @param selected whether the chip is selected
     */
    @Composable
    internal fun borderStroke(enabled: Boolean, selected: Boolean): State<BorderStroke?> {
        val color = if (enabled) {
            if (selected) selectedBorderColor else borderColor
        } else {
            if (selected) disabledSelectedBorderColor else disabledBorderColor
        }
        return rememberUpdatedState(
            BorderStroke(if (selected) selectedBorderWidth else borderWidth, color)
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is SelectableChipBorder) return false

        if (borderColor != other.borderColor) return false
        if (selectedBorderColor != other.selectedBorderColor) return false
        if (disabledBorderColor != other.disabledBorderColor) return false
        if (disabledSelectedBorderColor != other.disabledSelectedBorderColor) return false
        if (borderWidth != other.borderWidth) return false
        if (selectedBorderWidth != other.selectedBorderWidth) return false

        return true
    }

    override fun hashCode(): Int {
        var result = borderColor.hashCode()
        result = 31 * result + selectedBorderColor.hashCode()
        result = 31 * result + disabledBorderColor.hashCode()
        result = 31 * result + disabledSelectedBorderColor.hashCode()
        result = 31 * result + borderWidth.hashCode()
        result = 31 * result + selectedBorderWidth.hashCode()

        return result
    }
}

@ExperimentalMaterial3Api
@Composable
private fun SelectableChip(
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    onLongClickLabel: String?,
    onLongClick: () -> Unit,
    enabled: Boolean,
    label: @Composable () -> Unit,
    labelTextStyle: TextStyle,
    leadingIcon: @Composable (() -> Unit)?,
    avatar: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    shape: Shape,
    colors: SelectableChipColors,
    elevation: SelectableChipElevation?,
    border: BorderStroke?,
    minHeight: Dp,
    paddingValues: PaddingValues,
    interactionSource: MutableInteractionSource
) {
    Surface(
        selected = selected,
        onClick = onClick,
        onLongClickLabel = onLongClickLabel,
        onLongClick = onLongClick,
        modifier = modifier.semantics { role = Role.Checkbox },
        enabled = enabled,
        shape = shape,
        color = colors.containerColor(enabled, selected).value,
        tonalElevation = elevation?.tonalElevation(enabled, interactionSource)?.value
            ?: 0.dp,
        shadowElevation = elevation?.shadowElevation(enabled, interactionSource)?.value
            ?: 0.dp,
        border = border,
        interactionSource = interactionSource,
    ) {
        ChipContent(
            label = label,
            labelTextStyle = labelTextStyle,
            leadingIcon = leadingIcon,
            avatar = avatar,
            labelColor = colors.labelColor(enabled, selected).value,
            trailingIcon = trailingIcon,
            leadingIconColor = colors.leadingIconContentColor(enabled, selected).value,
            trailingIconColor = colors.trailingIconContentColor(enabled, selected).value,
            minHeight = minHeight,
            paddingValues = paddingValues
        )
    }
}

@Composable
@NonRestartableComposable
fun Surface(
    selected: Boolean,
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
                .selectable(
                    selected = selected,
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

fun Modifier.selectable(
    selected: Boolean,
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean = true,
    role: Role? = null,
    onClick: () -> Unit,
    onLongClickLabel: String?,
    onLongClick: () -> Unit,
) = inspectable(
    inspectorInfo = debugInspectorInfo {
        name = "selectable"
        properties["selected"] = selected
        properties["interactionSource"] = interactionSource
        properties["indication"] = indication
        properties["enabled"] = enabled
        properties["role"] = role
        properties["onClick"] = onClick
    },
    factory = {
        Modifier.combinedClickable(
            enabled = enabled,
            role = role,
            interactionSource = interactionSource,
            indication = indication,
            onClick = onClick,
            onLongClickLabel = onLongClickLabel,
            onLongClick = onLongClick,
        ).semantics {
            this.selected = selected
        }
    }
)

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
 * Represents the container and content colors used in a selectable chip in different states.
 *
 * See [FilterChipDefaults.filterChipColors] and [FilterChipDefaults.elevatedFilterChipColors] for
 * the default colors used in [FilterChip].
 */
@ExperimentalMaterial3Api
@Immutable
class SelectableChipColors internal constructor(
    private val containerColor: Color,
    private val labelColor: Color,
    private val leadingIconColor: Color,
    private val trailingIconColor: Color,
    private val disabledContainerColor: Color,
    private val disabledLabelColor: Color,
    private val disabledLeadingIconColor: Color,
    private val disabledTrailingIconColor: Color,
    private val selectedContainerColor: Color,
    private val disabledSelectedContainerColor: Color,
    private val selectedLabelColor: Color,
    private val selectedLeadingIconColor: Color,
    private val selectedTrailingIconColor: Color
    // TODO(b/113855296): Support other states: hover, focus, drag
) {
    /**
     * Represents the container color for this chip, depending on [enabled] and [selected].
     *
     * @param enabled whether the chip is enabled
     * @param selected whether the chip is selected
     */
    @Composable
    internal fun containerColor(enabled: Boolean, selected: Boolean): State<Color> {
        val target = when {
            !enabled -> if (selected) disabledSelectedContainerColor else disabledContainerColor
            !selected -> containerColor
            else -> selectedContainerColor
        }
        return rememberUpdatedState(target)
    }

    /**
     * Represents the label color for this chip, depending on [enabled] and [selected].
     *
     * @param enabled whether the chip is enabled
     * @param selected whether the chip is selected
     */
    @Composable
    internal fun labelColor(enabled: Boolean, selected: Boolean): State<Color> {
        val target = when {
            !enabled -> disabledLabelColor
            !selected -> labelColor
            else -> selectedLabelColor
        }
        return rememberUpdatedState(target)
    }

    /**
     * Represents the leading icon color for this chip, depending on [enabled] and [selected].
     *
     * @param enabled whether the chip is enabled
     * @param selected whether the chip is selected
     */
    @Composable
    internal fun leadingIconContentColor(enabled: Boolean, selected: Boolean): State<Color> {
        val target = when {
            !enabled -> disabledLeadingIconColor
            !selected -> leadingIconColor
            else -> selectedLeadingIconColor
        }
        return rememberUpdatedState(target)
    }

    /**
     * Represents the trailing icon color for this chip, depending on [enabled] and [selected].
     *
     * @param enabled whether the chip is enabled
     * @param selected whether the chip is selected
     */
    @Composable
    internal fun trailingIconContentColor(enabled: Boolean, selected: Boolean): State<Color> {
        val target = when {
            !enabled -> disabledTrailingIconColor
            !selected -> trailingIconColor
            else -> selectedTrailingIconColor
        }
        return rememberUpdatedState(target)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is SelectableChipColors) return false

        if (containerColor != other.containerColor) return false
        if (labelColor != other.labelColor) return false
        if (leadingIconColor != other.leadingIconColor) return false
        if (trailingIconColor != other.trailingIconColor) return false
        if (disabledContainerColor != other.disabledContainerColor) return false
        if (disabledLabelColor != other.disabledLabelColor) return false
        if (disabledLeadingIconColor != other.disabledLeadingIconColor) return false
        if (disabledTrailingIconColor != other.disabledTrailingIconColor) return false
        if (selectedContainerColor != other.selectedContainerColor) return false
        if (disabledSelectedContainerColor != other.disabledSelectedContainerColor) return false
        if (selectedLabelColor != other.selectedLabelColor) return false
        if (selectedLeadingIconColor != other.selectedLeadingIconColor) return false
        if (selectedTrailingIconColor != other.selectedTrailingIconColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + labelColor.hashCode()
        result = 31 * result + leadingIconColor.hashCode()
        result = 31 * result + trailingIconColor.hashCode()
        result = 31 * result + disabledContainerColor.hashCode()
        result = 31 * result + disabledLabelColor.hashCode()
        result = 31 * result + disabledLeadingIconColor.hashCode()
        result = 31 * result + disabledTrailingIconColor.hashCode()
        result = 31 * result + selectedContainerColor.hashCode()
        result = 31 * result + disabledSelectedContainerColor.hashCode()
        result = 31 * result + selectedLabelColor.hashCode()
        result = 31 * result + selectedLeadingIconColor.hashCode()
        result = 31 * result + selectedTrailingIconColor.hashCode()

        return result
    }
}


/**
 * Creates a [SelectableChipColors] that represents the default container and content colors
 * used in a flat [FilterChip].
 *
 * @param containerColor the container color of this chip when enabled
 * @param labelColor the label color of this chip when enabled
 * @param iconColor the color of this chip's start and end icons when enabled
 * @param disabledContainerColor the container color of this chip when not enabled
 * @param disabledLabelColor the label color of this chip when not enabled
 * @param disabledLeadingIconColor the color of this chip's start icon when not enabled
 * @param disabledTrailingIconColor the color of this chip's end icon when not enabled
 * @param selectedContainerColor the container color of this chip when selected
 * @param disabledSelectedContainerColor the container color of this chip when not enabled and
 * selected
 * @param selectedLabelColor the label color of this chip when selected
 * @param selectedLeadingIconColor the color of this chip's start icon when selected
 * @param selectedTrailingIconColor the color of this chip's end icon when selected
 */
@Composable
fun filterChipColors(
    containerColor: Color = Color.Transparent,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    disabledContainerColor: Color = Color.Transparent,
    disabledLabelColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    disabledLeadingIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    disabledTrailingIconColor: Color = disabledLeadingIconColor,
    selectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    disabledSelectedContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    selectedLabelColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    selectedLeadingIconColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    selectedTrailingIconColor: Color = selectedLeadingIconColor
): SelectableChipColors = SelectableChipColors(
    containerColor = containerColor,
    labelColor = labelColor,
    leadingIconColor = iconColor,
    trailingIconColor = iconColor,
    disabledContainerColor = disabledContainerColor,
    disabledLabelColor = disabledLabelColor,
    disabledLeadingIconColor = disabledLeadingIconColor,
    disabledTrailingIconColor = disabledTrailingIconColor,
    selectedContainerColor = selectedContainerColor,
    disabledSelectedContainerColor = disabledSelectedContainerColor,
    selectedLabelColor = selectedLabelColor,
    selectedLeadingIconColor = selectedLeadingIconColor,
    selectedTrailingIconColor = selectedTrailingIconColor
)

/**
 * Creates a [SelectableChipBorder] that represents the default border used in a flat
 * [FilterChip].
 *
 * @param borderColor the border color of this chip when enabled and not selected
 * @param selectedBorderColor the border color of this chip when enabled and selected
 * @param disabledBorderColor the border color of this chip when not enabled and not
 * selected
 * @param disabledSelectedBorderColor the border color of this chip when not enabled
 * but selected
 * @param borderWidth the border stroke width of this chip when not selected
 * @param selectedBorderWidth the border stroke width of this chip when selected
 */
@Composable
fun filterChipBorder(
    borderColor: Color = MaterialTheme.colorScheme.outline,
    selectedBorderColor: Color = Color.Transparent,
    disabledBorderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    disabledSelectedBorderColor: Color = Color.Transparent,
    borderWidth: Dp = 1.0.dp,
    selectedBorderWidth: Dp = 0.0.dp,
): SelectableChipBorder = SelectableChipBorder(
    borderColor = borderColor,
    selectedBorderColor = selectedBorderColor,
    disabledBorderColor = disabledBorderColor,
    disabledSelectedBorderColor = disabledSelectedBorderColor,
    borderWidth = borderWidth,
    selectedBorderWidth = selectedBorderWidth
)

/**
 * Represents the elevation used in a selectable chip in different states.
 *
 * Note that this default implementation does not take into consideration the `selectable` state
 * passed into its [tonalElevation] and [shadowElevation]. If you wish to apply that state, use a
 * different [SelectableChipElevation].
 */
@ExperimentalMaterial3Api
@Immutable
class SelectableChipElevation internal constructor(
    private val elevation: Dp,
    private val pressedElevation: Dp,
    private val focusedElevation: Dp,
    private val hoveredElevation: Dp,
    private val draggedElevation: Dp,
    private val disabledElevation: Dp
) {
    /**
     * Represents the tonal elevation used in a chip, depending on [enabled] and
     * [interactionSource]. This should typically be the same value as the [shadowElevation].
     *
     * Tonal elevation is used to apply a color shift to the surface to give the it higher emphasis.
     * When surface's color is [ColorScheme.surface], a higher elevation will result in a darker
     * color in light theme and lighter color in dark theme.
     *
     * See [shadowElevation] which controls the elevation of the shadow drawn around the Chip.
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
     * Represents the shadow elevation used in a chip, depending on [enabled] and
     * [interactionSource]. This should typically be the same value as the [tonalElevation].
     *
     * Shadow elevation is used to apply a shadow around the surface to give it higher emphasis.
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
        if (other == null || other !is SelectableChipElevation) return false

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
 * Creates a [SelectableChipElevation] that will animate between the provided values according
 * to the Material specification for a flat [FilterChip].
 *
 * @param elevation the elevation used when the [FilterChip] is has no other
 * [Interaction]s
 * @param pressedElevation the elevation used when the chip is pressed
 * @param focusedElevation the elevation used when the chip is focused
 * @param hoveredElevation the elevation used when the chip is hovered
 * @param draggedElevation the elevation used when the chip is dragged
 * @param disabledElevation the elevation used when the chip is not enabled
 */
@Composable
fun filterChipElevation(
    elevation: Dp = 0.0.dp,
    pressedElevation: Dp = 0.0.dp,
    focusedElevation: Dp = 0.0.dp,
    hoveredElevation: Dp = 1.0.dp,
    draggedElevation: Dp = 8.0.dp,
    disabledElevation: Dp = elevation
): SelectableChipElevation = SelectableChipElevation(
    elevation = elevation,
    pressedElevation = pressedElevation,
    focusedElevation = focusedElevation,
    hoveredElevation = hoveredElevation,
    draggedElevation = draggedElevation,
    disabledElevation = disabledElevation
)