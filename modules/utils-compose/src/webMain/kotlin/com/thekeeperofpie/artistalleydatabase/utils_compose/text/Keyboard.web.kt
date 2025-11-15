package com.thekeeperofpie.artistalleydatabase.utils_compose.text

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

actual val KeyEvent.isTabKey: Boolean
    get() = key == Key.Tab

actual val KeyEvent.isTabKeyDownOrTyped: Boolean
    get() = type == KeyEventType.KeyDown && key == Key.Tab
