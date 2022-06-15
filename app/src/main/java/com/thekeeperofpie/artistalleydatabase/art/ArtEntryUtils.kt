package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import android.net.Uri
import com.thekeeperofpie.artistalleydatabase.R

object ArtEntryUtils {

    fun writeEntryImage(
        application: Application,
        id: String,
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
                application.filesDir.resolve("entry_images/${id}").outputStream()
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