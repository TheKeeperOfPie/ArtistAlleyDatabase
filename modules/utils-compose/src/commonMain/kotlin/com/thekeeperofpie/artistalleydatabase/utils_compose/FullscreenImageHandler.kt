package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.full_image_content_description
import coil3.compose.AsyncImage
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource

@SingletonScope
@Inject
class FullscreenImageHandler {

    private var imageUrl by mutableStateOf<String?>(null)

    @Composable
    fun ImageDialog() {
        val imageUrl = imageUrl
        if (imageUrl != null) {
            Dialog(onDismissRequest = { this@FullscreenImageHandler.imageUrl = null }) {
                ZoomPanBox(onClick = { this@FullscreenImageHandler.imageUrl = null }) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clickable(
                                // Consume click events so that tapping image doesn't dismiss
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {}
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentScale = ContentScale.FillWidth,
                            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                            contentDescription = stringResource(Res.string.full_image_content_description),
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(min = 240.dp),
                        )
                    }
                }
            }
        }
    }

    fun openImage(url: String) {
        imageUrl = url
    }
}

val LocalFullscreenImageHandler = staticCompositionLocalOf { FullscreenImageHandler() }
