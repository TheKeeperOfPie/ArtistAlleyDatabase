package com.thekeeperofpie.artistalleydatabase.utils_roborazzi

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.DefaultDesktopComposePreviewTester
import com.github.takahirom.roborazzi.DesktopComposePreviewTester
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.LosslessWebPImageIoFormat
import com.github.takahirom.roborazzi.advanceMainClockFor
import io.github.takahirom.roborazzi.captureRoboImage

@Suppress("unused")
@OptIn(ExperimentalRoborazziApi::class, ExperimentalTestApi::class)
class WebpDesktopPreviewTester : DesktopComposePreviewTester by DefaultDesktopComposePreviewTester(
    capturer = DefaultDesktopComposePreviewTester.Capturer { parameter ->
        setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                parameter.preview()
            }
        }
        advanceMainClockFor(parameter)
        onRoot().captureRoboImage(
            filePath = parameter.filePath,
            roborazziOptions = parameter.roborazziOptions.copy(
                recordOptions = parameter.roborazziOptions.recordOptions.copy(
                    imageIoFormat = LosslessWebPImageIoFormat(),
                ),
            )
        )
    }
)
