package com.thekeeperofpie.artistalleydatabase.utils_compose.text

import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

actual val KeyEvent.isTabKey: Boolean
    get() {
        val awtEvent = awtEventOrNull
        return if (awtEvent != null) {
            awtEvent.keyChar == '\t'
        } else {
            key == Key.Tab
        }
    }

actual val KeyEvent.isTabKeyDownOrTyped: Boolean
    get() {
        val awtEvent = awtEventOrNull
        return if (awtEvent != null) {
            awtEvent.keyChar == '\t' && awtEvent.id == java.awt.event.KeyEvent.KEY_TYPED
        } else {
            type == KeyEventType.KeyDown && key == Key.Tab
        }
    }
