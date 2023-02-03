package com.thekeeperofpie.artistalleydatabase.android_utils

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import java.io.File
import java.net.URL

object ImageUtils {

    fun getImageWidthHeight(context: Context, uri: Uri): Pair<Int?, Int?> {
        val options = BitmapFactory.Options().apply {
            this.inJustDecodeBounds = true
        }
        try {
            context.contentResolver.openInputStream(uri).use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (ignored: Exception) {
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

    fun getImageType(file: File): String? {
        val options = BitmapFactory.Options().apply {
            this.inJustDecodeBounds = true
        }
        try {
            if (!file.exists()) return null
            file.inputStream().use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (ignored: Exception) {
            return null
        }

        return options.outMimeType
    }

    @WorkerThread
    fun writeEntryImage(
        application: Application,
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
                    } else application.contentResolver.openInputStream(imageUri)
                }
                else -> application.contentResolver.openInputStream(imageUri)
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