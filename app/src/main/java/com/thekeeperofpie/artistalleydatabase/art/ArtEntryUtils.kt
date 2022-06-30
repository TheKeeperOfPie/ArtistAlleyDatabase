package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.thekeeperofpie.artistalleydatabase.R
import java.io.File

object ArtEntryUtils {

    fun getImageFile(context: Context, id: String) =
        context.filesDir.resolve("entry_images/${id}")

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

    fun writeEntryImage(
        application: Application,
        outputFile: File,
        imageUri: Uri?
    ): Pair<Int, Exception?>? {
        imageUri?.let {
            val imageStream = try {
                application.contentResolver.openInputStream(it)
            } catch (e: Exception) {
                return R.string.error_fail_to_load_image to e
            } ?: run {
                return R.string.error_fail_to_load_image to null
            }

            val output = try {
                outputFile.outputStream()
            } catch (e: Exception) {
                return R.string.error_fail_to_open_file_output to e
            }

            output.use {
                imageStream.use {
                    imageStream.copyTo(output)
                }
            }
        }

        return null
    }
}