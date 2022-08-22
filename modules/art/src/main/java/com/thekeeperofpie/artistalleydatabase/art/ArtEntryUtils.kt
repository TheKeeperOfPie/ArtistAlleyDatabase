package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.thekeeperofpie.artistalleydatabase.utils.UtilsStringR
import kotlinx.serialization.json.Json
import java.io.File

object ArtEntryUtils {

    fun getImageFile(context: Context, id: String) = context.filesDir
        .resolve("entry_images/${id}")

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

    fun writeEntryImage(
        application: Application,
        outputFile: File,
        imageUri: Uri?
    ): Pair<Int, Exception?>? {
        imageUri?.let {
            val imageStream = try {
                application.contentResolver.openInputStream(it)
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
        }

        return null
    }

    fun buildPlaceholderText(json: Json, entry: ArtEntry) = entry.run {
        val source = when (val source = SourceType.fromEntry(json, this)) {
            is SourceType.Convention -> (source.name + (source.year?.let { " $it" }
                ?: "") + "\n" + source.hall + " " + source.booth).trim()
            is SourceType.Custom -> source.value
            is SourceType.Online -> source.name
            SourceType.Unknown,
            SourceType.Different -> ""
        }

        val info = if (artists.isNotEmpty()) {
            artists.joinToString("\n")
        } else if (series.isNotEmpty()) {
            series.joinToString("\n")
        } else if (characters.isNotEmpty()) {
            characters.joinToString("\n")
        } else if (tags.isNotEmpty()) {
            tags.take(10).joinToString("\n")
        } else ""

        val pieces = listOf(source, info, notes)
        if (pieces.any { !it.isNullOrBlank() }) {
            pieces.joinToString("\n")
        } else id
    }
}