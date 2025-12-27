package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.BasicTooltipDefaults
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TooltipIconButton(
    icon: ImageVector,
    tooltipText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    positioning: TooltipAnchorPosition = TooltipAnchorPosition.Below,
    enabled: Boolean = true,
    useButtonOnClickForTooltipOnClick: Boolean = false,
    contentDescription: String? = tooltipText,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = positioning,
            spacingBetweenTooltipAndAnchor = 0.dp,
        ),
        tooltip = {
            val clipboardManager = LocalClipboardManager.current
            PlainTooltip(
                modifier = Modifier
                    .hoverable(interactionSource)
                    .run {
                        if (useButtonOnClickForTooltipOnClick) {
                            clickable(onClick = onClick)
                        } else {
                            clickable { clipboardManager.setText(AnnotatedString(tooltipText)) }
                        }
                    }
            ) {
                Text(tooltipText)
            }
        },
        state = rememberCustomTooltipState(isHovered = { isHovered }),
        focusable = true,
        enableUserInput = true,
        hasAction = true,
        modifier = modifier,
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
            )
        }
    }
}

private val GlobalMutatorMutex: MutatorMutex = MutatorMutex()

@Composable
private fun rememberCustomTooltipState(
    isHovered: () -> Boolean,
): TooltipState =
    remember(isHovered) {
        TooltipStateImpl(isHovered)
    }

/**
 * Custom implementation of [TooltipState] which adds a delay when dismissing and keeps the tooltip
 * visible when hovered.
 */
@Stable
private class TooltipStateImpl(
    private val isHovered: () -> Boolean,
) : TooltipState {
    override val isPersistent: Boolean = false

    override val transition: MutableTransitionState<Boolean> =
        MutableTransitionState(false)

    override val isVisible: Boolean
        get() = transition.currentState || transition.targetState

    private var job: (CancellableContinuation<Unit>)? = null

    override suspend fun show(mutatePriority: MutatePriority) {
        val cancellableShow: suspend () -> Unit = {
            suspendCancellableCoroutine { continuation ->
                transition.targetState = true
                job = continuation
            }
        }

        GlobalMutatorMutex.mutate(mutatePriority) {
            try {
                if (isPersistent || mutatePriority == MutatePriority.UserInput) {
                    cancellableShow()
                } else {
                    withTimeout(BasicTooltipDefaults.TooltipDuration) { cancellableShow() }
                }
            } finally {
                if (mutatePriority != MutatePriority.PreventUserInput) {
                    try {
                        delay(500.milliseconds)
                        snapshotFlow { isHovered() }.filter { !it }.first()
                    } finally {
                        transition.targetState = false
                    }
                }
            }
        }
    }

    override fun dismiss() {
        job?.cancel()
    }

    override fun onDispose() {
        job?.cancel()
    }
}
