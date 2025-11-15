package com.thekeeperofpie.artistalleydatabase.utils_compose.text

import androidx.compose.ui.input.key.KeyEvent

expect val KeyEvent.isTabKey: Boolean
expect val KeyEvent.isTabKeyDownOrTyped: Boolean
