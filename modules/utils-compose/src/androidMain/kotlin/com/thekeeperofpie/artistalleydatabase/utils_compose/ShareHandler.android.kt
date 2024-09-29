package com.thekeeperofpie.artistalleydatabase.utils_compose

import android.app.Application
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import com.eygraber.uri.Uri
import com.eygraber.uri.toAndroidUriOrNull
import com.thekeeperofpie.artistalleydatabase.inject.ActivityScope
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import kotlinx.io.buffered
import kotlinx.io.files.Path
import me.tatarka.inject.annotations.Inject
import java.io.File

@ActivityScope
@Inject
actual class ShareHandler(
    private val application: Application,
    private val appFileSystem: AppFileSystem,
    private val activity: ComponentActivity,
) {
    companion object {
        fun getShareUriAndMimeTypeForPath(
            application: Application,
            appFileSystem: AppFileSystem,
            path: Path?,
            uri: Uri?,
        ): Pair<android.net.Uri?, String?> = if (path != null && appFileSystem.exists(path)) {
            // Some consumers require a file extension to parse the image correctly.
            val mimeType = appFileSystem.getImageType(path)
            val extension = when (mimeType) {
                "image/jpeg", "image/jpg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> ""
            }

            // TODO: Find a better solution for the file extension problem
            // TODO: Offer an option to compress before export in case the caller has a size limitation
            appFileSystem.createDirectories(appFileSystem.filePath("external"))
            val externalFile = appFileSystem.filePath("external/external.$extension")
            appFileSystem.source(path).buffered().use { input ->
                appFileSystem.sink(externalFile).buffered().use { output ->
                    input.transferTo(output)
                }
            }
            FileProvider.getUriForFile(
                application,
                "${application.packageName}.fileprovider",
                File(externalFile.toString()),
            ) to mimeType
        } else {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            val androidUri = uri?.toAndroidUriOrNull()
            if (androidUri != null) {
                try {
                    application.contentResolver.openInputStream(androidUri).use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                } catch (ignored: Exception) {
                }
            }

            androidUri to options.outMimeType
        }
    }

    actual fun shareUrl(title: String?, url: String) {
        val shareIntent = Intent()
            .apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
                if (title != null) {
                    putExtra(Intent.EXTRA_TITLE, title)
                }
            }
            .let { Intent.createChooser(it, null) }
        activity.startActivity(shareIntent)
    }

    actual fun shareImage(path: Path?, uri: Uri?) {
        val (finalUri, mimeType) =
            getShareUriAndMimeTypeForPath(application, appFileSystem, path, uri)
        finalUri ?: return

        val shareIntent = Intent()
            .apply {
                action = Intent.ACTION_SEND
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, finalUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            .let { Intent.createChooser(it, null) }
        activity.startActivity(shareIntent)
    }
}
