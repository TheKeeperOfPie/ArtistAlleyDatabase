package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.staticCompositionLocalOf
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.blurForScreenshotMode

interface ComposeSettings {
    /** Whether to blur images via [blurForScreenshotMode] */
    val screenshotMode: Boolean
}

val LocalComposeSettings =
    staticCompositionLocalOf<ComposeSettings> {
        object : ComposeSettings {
            override val screenshotMode = false
        }
    }
