/**
 * Copy of android.compose.material3.DropdownMenu but using a
 * androidx.compose.foundation.lazy.LazyColumn.
 */

@file:Suppress("KDocUnresolvedReference")

package com.thekeeperofpie.artistalleydatabase.compose.dropdown

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.max
import kotlin.math.min

/**
 * <a href="https://m3.material.io/components/menus/overview" class="external" target="_blank">Material Design dropdown menu</a>.
 *
 * Menus display a list of choices on a temporary surface. They appear when users interact with a
 * button, action, or other control.
 *
 * ![Dropdown menu image](https://developer.android.com/images/reference/androidx/compose/material3/menu.png)
 *
 * A [DropdownMenu] behaves similarly to a [Popup], and will use the position of the parent layout
 * to position itself on screen. Commonly a [DropdownMenu] will be placed in a [Box] with a sibling
 * that will be used as the 'anchor'. Note that a [DropdownMenu] by itself will not take up any
 * space in a layout, as the menu is displayed in a separate window, on top of other content.
 *
 * The [content] of a [DropdownMenu] will typically be [DropdownMenuItem]s, as well as custom
 * content. Using [DropdownMenuItem]s will result in a menu that matches the Material
 * specification for menus. Also note that the [content] is placed inside a scrollable [Column],
 * so using a [LazyColumn] as the root layout inside [content] is unsupported.
 *
 * [onDismissRequest] will be called when the menu should close - for example when there is a
 * tap outside the menu, or when the back key is pressed.
 *
 * [DropdownMenu] changes its positioning depending on the available space, always trying to be
 * fully visible. It will try to expand horizontally, depending on layout direction, to the end of
 * its parent, then to the start of its parent, and then screen end-aligned. Vertically, it will
 * try to expand to the bottom of its parent, then from the top of its parent, and then screen
 * top-aligned. An [offset] can be provided to adjust the positioning of the menu for cases when
 * the layout bounds of its parent do not coincide with its visual bounds. Note the offset will
 * be applied in the direction in which the menu will decide to expand.
 *
 * Example usage:
 * @sample androidx.compose.material3.samples.MenuSample
 *
 * @param expanded whether the menu is expanded or not
 * @param onDismissRequest called when the user requests to dismiss the menu, such as by tapping
 * outside the menu's bounds
 * @param offset [DpOffset] to be added to the position of the menu
 */
@Suppress("ModifierParameter")
@Composable
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit
) {
    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = expanded

    if (expandedStates.currentState || expandedStates.targetState) {
        val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }
        val density = LocalDensity.current
        val popupPositionProvider = DropdownMenuPositionProvider(
            offset,
            density
        ) { parentBounds, menuBounds ->
            transformOriginState.value = calculateTransformOrigin(parentBounds, menuBounds)
        }

        Popup(
            onDismissRequest = onDismissRequest,
            popupPositionProvider = popupPositionProvider,
            properties = properties
        ) {
            DropdownMenuContent(
                expandedStates = expandedStates,
                transformOriginState = transformOriginState,
                listState = listState,
                modifier = modifier,
                content = content
            )
        }
    }
}

/**
 * <a href="https://m3.material.io/components/menus/overview" class="external" target="_blank">Material Design dropdown menu</a> item.
 *
 * Menus display a list of choices on a temporary surface. They appear when users interact with a
 * button, action, or other control.
 *
 * ![Dropdown menu image](https://developer.android.com/images/reference/androidx/compose/material3/menu.png)
 *
 * Example usage:
 * @sample androidx.compose.material3.samples.MenuSample
 *
 * @param text text of the menu item
 * @param onClick called when this menu item is clicked
 * @param modifier the [Modifier] to be applied to this menu item
 * @param leadingIcon optional leading icon to be displayed at the beginning of the item's text
 * @param trailingIcon optional trailing icon to be displayed at the end of the item's text. This
 * trailing icon slot can also accept [Text] to indicate a keyboard shortcut.
 * @param enabled controls the enabled state of this menu item. When `false`, this component will
 * not respond to user input, and it will appear visually disabled and disabled to accessibility
 * services.
 * @param colors [MenuItemColors] that will be used to resolve the colors used for this menu item in
 * different states. See [MenuDefaults.itemColors].
 * @param contentPadding the padding applied to the content of this menu item
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this menu item. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the appearance / behavior of this menu item in different states.
 */
@Composable
fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    DropdownMenuItemContent(
        text = text,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    )
}
@Suppress("ModifierParameter")
@Composable
internal fun DropdownMenuContent(
    expandedStates: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    // Menu open/close animation.
    val transition = updateTransition(expandedStates, "DropDownMenu")

    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(
                    durationMillis = InTransitionDuration,
                    easing = LinearOutSlowInEasing
                )
            } else {
                // Expanded to dismissed.
                tween(
                    durationMillis = 1,
                    delayMillis = OutTransitionDuration - 1
                )
            }
        }, label = ""
    ) {
        if (it) {
            // Menu is expanded.
            1f
        } else {
            // Menu is dismissed.
            0.8f
        }
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(durationMillis = 30)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = OutTransitionDuration)
            }
        }, label = ""
    ) {
        if (it) {
            // Menu is expanded.
            1f
        } else {
            // Menu is dismissed.
            0f
        }
    }
    Surface(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
            transformOrigin = transformOriginState.value
        },
        shape = RoundedCornerShape(4.0.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.0.dp,
        shadowElevation = 3.0.dp
    ) {
        LazyColumn(
            state = listState,
            modifier = modifier
                .padding(vertical = DropdownMenuVerticalPadding)
                .fillMaxWidth(),
            content = content
        )
    }
}

@Composable
internal fun DropdownMenuItemContent(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    enabled: Boolean,
    colors: MenuItemColors,
    contentPadding: PaddingValues,
    interactionSource: MutableInteractionSource
) {
    Row(
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = rememberRipple(true)
            )
            .fillMaxWidth()
            // Preferred min and max width used during the intrinsic measurement.
            .sizeIn(
                minWidth = DropdownMenuItemDefaultMinWidth,
                maxWidth = DropdownMenuItemDefaultMaxWidth,
                minHeight = 48.0.dp
            )
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProvideTextStyle(MaterialTheme.typography.labelLarge) {
            if (leadingIcon != null) {
                CompositionLocalProvider(
                    LocalContentColor provides colors.leadingIconColor(enabled).value,
                ) {
                    Box(Modifier.defaultMinSize(minWidth = 24.0.dp)) {
                        leadingIcon()
                    }
                }
            }
            CompositionLocalProvider(LocalContentColor provides colors.textColor(enabled).value) {
                Box(
                    Modifier
                        .weight(1f)
                        .padding(
                            start = if (leadingIcon != null) {
                                DropdownMenuItemHorizontalPadding
                            } else {
                                0.dp
                            },
                            end = if (trailingIcon != null) {
                                DropdownMenuItemHorizontalPadding
                            } else {
                                0.dp
                            }
                        )
                ) {
                    text()
                }
            }
            if (trailingIcon != null) {
                CompositionLocalProvider(
                    LocalContentColor provides colors.trailingIconColor(enabled).value
                ) {
                    Box(Modifier.defaultMinSize(minWidth = 24.0.dp)) {
                        trailingIcon()
                    }
                }
            }
        }
    }
}

internal fun calculateTransformOrigin(
    parentBounds: IntRect,
    menuBounds: IntRect
): TransformOrigin {
    val pivotX = when {
        menuBounds.left >= parentBounds.right -> 0f
        menuBounds.right <= parentBounds.left -> 1f
        menuBounds.width == 0 -> 0f
        else -> {
            val intersectionCenter =
                (
                        max(parentBounds.left, menuBounds.left) +
                                min(parentBounds.right, menuBounds.right)
                        ) / 2
            (intersectionCenter - menuBounds.left).toFloat() / menuBounds.width
        }
    }
    val pivotY = when {
        menuBounds.top >= parentBounds.bottom -> 0f
        menuBounds.bottom <= parentBounds.top -> 1f
        menuBounds.height == 0 -> 0f
        else -> {
            val intersectionCenter =
                (
                        max(parentBounds.top, menuBounds.top) +
                                min(parentBounds.bottom, menuBounds.bottom)
                        ) / 2
            (intersectionCenter - menuBounds.top).toFloat() / menuBounds.height
        }
    }
    return TransformOrigin(pivotX, pivotY)
}

// Menu positioning.

/**
 * Calculates the position of a Material [DropdownMenu].
 */
// TODO(popam): Investigate if this can/should consider the app window size rather than screen size
@Immutable
internal data class DropdownMenuPositionProvider(
    val contentOffset: DpOffset,
    val density: Density,
    val onPositionCalculated: (IntRect, IntRect) -> Unit = { _, _ -> }
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        // The min margin above and below the menu, relative to the screen.
        val verticalMargin = with(density) { MenuVerticalMargin.roundToPx() }
        // The content offset specified using the dropdown offset parameter.
        val contentOffsetX = with(density) { contentOffset.x.roundToPx() }
        val contentOffsetY = with(density) { contentOffset.y.roundToPx() }

        // Compute horizontal position.
        val toRight = anchorBounds.left + contentOffsetX
        val toLeft = anchorBounds.right - contentOffsetX - popupContentSize.width
        val toDisplayRight = windowSize.width - popupContentSize.width
        val toDisplayLeft = 0
        val x = if (layoutDirection == LayoutDirection.Ltr) {
            sequenceOf(
                toRight,
                toLeft,
                // If the anchor gets outside of the window on the left, we want to position
                // toDisplayLeft for proximity to the anchor. Otherwise, toDisplayRight.
                if (anchorBounds.left >= 0) toDisplayRight else toDisplayLeft
            )
        } else {
            sequenceOf(
                toLeft,
                toRight,
                // If the anchor gets outside of the window on the right, we want to position
                // toDisplayRight for proximity to the anchor. Otherwise, toDisplayLeft.
                if (anchorBounds.right <= windowSize.width) toDisplayLeft else toDisplayRight
            )
        }.firstOrNull {
            it >= 0 && it + popupContentSize.width <= windowSize.width
        } ?: toLeft

        // Compute vertical position.
        val toBottom = maxOf(anchorBounds.bottom + contentOffsetY, verticalMargin)
        val toTop = anchorBounds.top - contentOffsetY - popupContentSize.height
        val toCenter = anchorBounds.top - popupContentSize.height / 2
        val toDisplayBottom = windowSize.height - popupContentSize.height - verticalMargin
        val y = sequenceOf(toBottom, toTop, toCenter, toDisplayBottom).firstOrNull {
            it >= verticalMargin &&
                    it + popupContentSize.height <= windowSize.height - verticalMargin
        } ?: toTop

        onPositionCalculated(
            anchorBounds,
            IntRect(x, y, x + popupContentSize.width, y + popupContentSize.height)
        )
        return IntOffset(x, y)
    }
}


internal object MenuTokens {
    const val ListItemDisabledLabelTextOpacity = 0.38f
    const val ListItemDisabledLeadingIconOpacity = 0.38f
    const val ListItemDisabledTrailingIconOpacity = 0.38f
}

/**
 * Contains default values used for [DropdownMenuItem].
 */
object MenuDefaults {

    /**
     * Creates a [MenuItemColors] that represents the default text and icon colors used in a
     * [DropdownMenuItemContent].
     *
     * @param textColor the text color of this [DropdownMenuItemContent] when enabled
     * @param leadingIconColor the leading icon color of this [DropdownMenuItemContent] when enabled
     * @param trailingIconColor the trailing icon color of this [DropdownMenuItemContent] when
     * enabled
     * @param disabledTextColor the text color of this [DropdownMenuItemContent] when not enabled
     * @param disabledLeadingIconColor the leading icon color of this [DropdownMenuItemContent] when
     * not enabled
     * @param disabledTrailingIconColor the trailing icon color of this [DropdownMenuItemContent]
     * when not enabled
     */
    @Composable
    fun itemColors(
        textColor: Color = MaterialTheme.colorScheme.onSurface,
        leadingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        trailingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledTextColor: Color = MaterialTheme.colorScheme.onSurface
            .copy(alpha = MenuTokens.ListItemDisabledLabelTextOpacity),
        disabledLeadingIconColor: Color = MaterialTheme.colorScheme.onSurface
            .copy(alpha = MenuTokens.ListItemDisabledLeadingIconOpacity),
        disabledTrailingIconColor: Color = MaterialTheme.colorScheme.onSurface
            .copy(alpha = MenuTokens.ListItemDisabledTrailingIconOpacity),
    ): MenuItemColors = MenuItemColors(
        textColor = textColor,
        leadingIconColor = leadingIconColor,
        trailingIconColor = trailingIconColor,
        disabledTextColor = disabledTextColor,
        disabledLeadingIconColor = disabledLeadingIconColor,
        disabledTrailingIconColor = disabledTrailingIconColor,
    )

    /**
     * Default padding used for [DropdownMenuItem].
     */
    val DropdownMenuItemContentPadding = PaddingValues(
        horizontal = DropdownMenuItemHorizontalPadding,
        vertical = 0.dp
    )
}

/**
 * Represents the text and icon colors used in a menu item at different states.
 *
 * - See [MenuDefaults.itemColors] for the default colors used in a [DropdownMenuItemContent].
 */
@Immutable
class MenuItemColors internal constructor(
    private val textColor: Color,
    private val leadingIconColor: Color,
    private val trailingIconColor: Color,
    private val disabledTextColor: Color,
    private val disabledLeadingIconColor: Color,
    private val disabledTrailingIconColor: Color,
) {
    /**
     * Represents the text color for a menu item, depending on its [enabled] state.
     *
     * @param enabled whether the menu item is enabled
     */
    @Composable
    internal fun textColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) textColor else disabledTextColor)
    }

    /**
     * Represents the leading icon color for a menu item, depending on its [enabled] state.
     *
     * @param enabled whether the menu item is enabled
     */
    @Composable
    internal fun leadingIconColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) leadingIconColor else disabledLeadingIconColor)
    }

    /**
     * Represents the trailing icon color for a menu item, depending on its [enabled] state.
     *
     * @param enabled whether the menu item is enabled
     */
    @Composable
    internal fun trailingIconColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) trailingIconColor else disabledTrailingIconColor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is MenuItemColors) return false

        if (textColor != other.textColor) return false
        if (leadingIconColor != other.leadingIconColor) return false
        if (trailingIconColor != other.trailingIconColor) return false
        if (disabledTextColor != other.disabledTextColor) return false
        if (disabledLeadingIconColor != other.disabledLeadingIconColor) return false
        if (disabledTrailingIconColor != other.disabledTrailingIconColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = textColor.hashCode()
        result = 31 * result + leadingIconColor.hashCode()
        result = 31 * result + trailingIconColor.hashCode()
        result = 31 * result + disabledTextColor.hashCode()
        result = 31 * result + disabledLeadingIconColor.hashCode()
        result = 31 * result + disabledTrailingIconColor.hashCode()
        return result
    }
}

// Size defaults.
private val MenuVerticalMargin = 48.dp
private val DropdownMenuItemHorizontalPadding = 12.dp
private val DropdownMenuVerticalPadding = 8.dp
private val DropdownMenuItemDefaultMinWidth = 112.dp
private val DropdownMenuItemDefaultMaxWidth = 280.dp

// Menu open/close animation.
private const val InTransitionDuration = 120
private const val OutTransitionDuration = 75
