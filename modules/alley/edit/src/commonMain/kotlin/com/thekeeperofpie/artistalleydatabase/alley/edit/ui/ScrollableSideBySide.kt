package com.thekeeperofpie.artistalleydatabase.alley.edit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass

@Composable
fun ScrollableSideBySide(
    showSecondary: () -> Boolean,
    primary: @Composable ColumnScope.() -> Unit,
    secondary: @Composable ColumnScope.() -> Unit,
    secondaryExpanded: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollStateOne = rememberScrollState()
    val scrollStateTwo = rememberScrollState()
    val scrollAreaStateOne = rememberScrollAreaState(scrollStateOne)
    val scrollAreaStateTwo = rememberScrollAreaState(scrollStateTwo)

    val primaryContent =
        remember { movableContentOf { columnScope: ColumnScope -> columnScope.primary() } }

    val secondaryContent =
        remember { movableContentOf { columnScope: ColumnScope -> columnScope.secondary() } }

    val secondaryExpandedContent =
        remember { movableContentOf { columnScope: ColumnScope -> columnScope.secondaryExpanded() } }

    val windowSizeClass = currentWindowSizeClass()
    val canShowExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    if (!canShowExpanded) {
        ScrollArea(state = scrollAreaStateOne, modifier = modifier) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().verticalScroll(scrollStateOne)
            ) {
                Column(modifier = Modifier.widthIn(max = 960.dp)) {
                    if (showSecondary()) {
                        secondaryContent(this)
                    }
                    primaryContent(this)
                    Spacer(Modifier.height(80.dp))
                }
            }

            PrimaryVerticalScrollbar()
        }
        return
    }

    Row(modifier = modifier) {
        ScrollArea(state = scrollAreaStateOne, modifier = Modifier.weight(1f)) {
            Column(
                horizontalAlignment = if (showSecondary()) Alignment.End else Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().verticalScroll(scrollStateOne)
            ) {
                Column(modifier = Modifier.widthIn(max = 960.dp)) {
                    primaryContent(this)
                    Spacer(Modifier.height(80.dp))
                }
            }

            PrimaryVerticalScrollbar()
        }

        if (showSecondary()) {
            ScrollArea(state = scrollAreaStateTwo, modifier = Modifier.weight(1f)) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth().verticalScroll(scrollStateTwo)
                ) {
                    Column(modifier = Modifier.widthIn(max = 960.dp)) {
                        secondaryExpandedContent(this)
                        Spacer(Modifier.height(80.dp))
                    }
                }

                PrimaryVerticalScrollbar()
            }
        }
    }
}
