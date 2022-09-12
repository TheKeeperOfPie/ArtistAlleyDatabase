package com.thekeeperofpie.artistalleydatabase.android_utils

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.WorkerThread
import java.io.File
import java.net.URL

object ImageUtils {

    fun getImageSize(file: File): Pair<Int?, Int?> {
        val options = BitmapFactory.Options().apply {
            this.inJustDecodeBounds = true
        }
        try {
            if (!file.exists()) return null to null
            file.inputStream().use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (ignored: Exception) {
            return null to null
        }

        val imageWidth = if (options.outWidth == -1) null else options.outWidth
        val imageHeight = if (options.outHeight == -1) null else options.outHeight
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
            if (imageUri.scheme?.startsWith("http") == true) {
                URL(imageUri.toString()).openStream()
            } else {
                application.contentResolver.openInputStream(imageUri)
            }
        } catch (e: Exception) {
            return UtilsStringR.error_fail_to_load_image to e
        } ?: run {
            return UtilsStringR.error_fail_to_load_image to null
        }

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