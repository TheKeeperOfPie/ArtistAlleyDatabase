package com.thekeeperofpie.artistalleydatabase.image

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import artistalleydatabase.modules.image.generated.resources.Res
import artistalleydatabase.modules.image.generated.resources.open_full_image_content_description
import com.eygraber.uri.Uri
import com.eygraber.uri.toAndroidUri
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import java.io.File

@Composable
actual fun rememberImageHandler(): ImageHandler {
    val context = LocalContext.current
    return remember(context) { ImageHandler(context) }
}

actual class ImageHandler(private val context: Context) {

    actual fun openImage(uri: Uri) {
        val path = uri.path
        if (uri.scheme == ContentResolver.SCHEME_FILE && path != null) {
            // TODO: Make this more reliable?
            openInternalImage(File(path))
        } else {
            openImage(uri.toAndroidUri())
        }
    }

    private fun openInternalImage(file: File) {
        val imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(imageUri, "image/*")
        }
        startChooser(intent)
    }

    private fun openImage(uri: android.net.Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, "image/*")
        }
        startChooser(intent)
    }

    private fun startChooser(intent: Intent) {
        val chooserIntent = Intent.createChooser(
            intent,
            runBlocking { getString(Res.string.open_full_image_content_description) },
        )
        context.startActivity(chooserIntent)
    }
}
