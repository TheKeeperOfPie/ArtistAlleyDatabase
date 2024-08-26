package com.thekeeperofpie.artistalleydatabase.android_utils

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import kotlinx.io.asInputStream
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.io.File
import java.net.URL

object ImageUtils {

    private const val TAG = "ImageUtils"

    fun getImageWidthHeight(appFileSystem: AppFileSystem, uri: Uri): Pair<Int?, Int?> {
        val options = BitmapFactory.Options().apply {
            this.inJustDecodeBounds = true
        }
        try {
            when {
                uri.scheme == "http" || uri.scheme == "https" -> URL(uri.toString()).openStream()
                uri.scheme == "file"
                        && uri.authority?.isEmpty() == true
                        && uri.path?.startsWith("/android_asset") == true ->
                    appFileSystem.application.assets.open(uri.path!!.removePrefix("/android_asset/"))
                else -> appFileSystem.openUri(uri)?.asInputStream()
            }.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error loading image size for scheme = ${uri.scheme}, " +
                    "authority = ${uri.authority}, path = ${uri.path}, uri = $uri", e)
            return null to null
        }

        return options.widthHeight
    }

    private val BitmapFactory.Options.widthHeight: Pair<Int?, Int?>
        get() {
            val imageWidth = if (outWidth == -1) null else outWidth
            val imageHeight = if (outHeight == -1) null else outHeight
            return (imageWidth to imageHeight)
        }

    fun getImageType(appFileSystem: AppFileSystem, path: Path): String? {
        val options = BitmapFactory.Options().apply {
            this.inJustDecodeBounds = true
        }
        try {
            if (!SystemFileSystem.exists(path)) return null
            SystemFileSystem.source(path).buffered().asInputStream().use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (ignored: Exception) {
            return null
        }

        return options.outMimeType
    }

    @WorkerThread
    fun writeEntryImage(
        appFileSystem: AppFileSystem,
        outputFile: File,
        imageUri: Uri?
    ): Pair<Int, Exception?>? {
        imageUri ?: return null
        val imageStream = try {
            when (imageUri.scheme) {
                "http", "https" -> URL(imageUri.toString()).openStream()
                ContentResolver.SCHEME_FILE -> {
                    if (imageUri.path == outputFile.toUri().path) {
                        return null
                    } else appFileSystem.openUri(imageUri)?.asInputStream()
                }
                else -> appFileSystem.openUri(imageUri)?.asInputStream()
            }
        } catch (e: Exception) {
            return UtilsStringR.error_fail_to_load_image to e
        } ?: run {
            return UtilsStringR.error_fail_to_load_image to null
        }

        outputFile.parentFile?.mkdirs()

        val output = try {
            outputFile.outputStream()
        } catch (e: Exception) {
            return UtilsStringR.error_fail_to_open_file_output to e
        }

        output.use {
            imageStream.use {
                imageStream.copyTo(output)
            }
        }
        return null
    }
}
