package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ThemeAwareElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick == null && onLongClick == null) {
        ThemeAwareElevatedCardImpl(onClick = null, content = content, modifier = modifier)
    } else if (onClick != null && onLongClick != null) {
        ThemeAwareElevatedCardImpl(onClick = null, modifier = modifier) {
            Column(
                content = content,
                modifier = Modifier.combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
            )
        }
    } else if (onClick != null && onLongClick == null) {
        ThemeAwareElevatedCardImpl(onClick = onClick, content = content, modifier = modifier)
    } else if (onClick == null && onLongClick != null) {
        ThemeAwareElevatedCardImpl(onClick = null, modifier = modifier) {
            Column(
                content = content,
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick,
                )
            )
        }
    }
}

@Composable
private fun ThemeAwareElevatedCardImpl(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick == null) {
        when (LocalAppTheme.current) {
            AppThemeSetting.AUTO,
            AppThemeSetting.LIGHT,
            AppThemeSetting.DARK,
                -> ElevatedCard(modifier = modifier, content = content)
            AppThemeSetting.BLACK -> OutlinedCard(modifier = modifier, content = content)
        }
    } else {
        when (LocalAppTheme.current) {
            AppThemeSetting.AUTO,
            AppThemeSetting.LIGHT,
            AppThemeSetting.DARK,
                -> ElevatedCard(onClick = onClick, modifier = modifier, content = content)
            AppThemeSetting.BLACK -> OutlinedCard(
                onClick = onClick,
                modifier = modifier,
                content = content,
            )
        }
    }
}
