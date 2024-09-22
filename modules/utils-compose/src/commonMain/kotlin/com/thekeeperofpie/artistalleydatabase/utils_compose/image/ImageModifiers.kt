package com.thekeeperofpie.artistalleydatabase.utils_compose.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalComposeSettings
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally


private val ScreenshotBlur by lazy { Modifier.blur(6.dp) }

@Composable
fun Modifier.blurForScreenshotMode() =
    conditionally(LocalComposeSettings.current.screenshotMode) { ScreenshotBlur }
