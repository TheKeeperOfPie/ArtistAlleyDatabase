package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.isClearFocusOnMouseDownEnabled

@OptIn(ExperimentalComposeUiApi::class)
object ComposeInit {

    fun init() {
        ComposeUiFlags.isClearFocusOnMouseDownEnabled = true
    }
}
