package com.thekeeperofpie.artistalleydatabase.alley

import android.app.Application
import android.net.Uri
import androidx.annotation.WorkerThread
import com.thekeeperofpie.artistalleydatabase.android_utils.ImageUtils

object ArtistAlleyUtils {

    @WorkerThread
    fun getImages(application: Application, folder: String, file: String): List<CatalogImage> {
        val assetManager = application.assets
        val targetFolder = assetManager.list(folder)?.find { it.startsWith(file) }
        return assetManager.list("$folder/$targetFolder")
            ?.flatMap {
                if (it.endsWith(".pdf")) {
                    emptyList()
                } else if (it.startsWith(file)) {
                    try {
                        val subFolder = it
                        application.assets.list("$folder/$targetFolder/$subFolder")?.map {
                            "$subFolder/$it"
                        }.orEmpty()
                    } catch (ignored: Throwable) {
                        emptyList()
                    }
                } else {
                    listOf(it)
                }
            }
            .orEmpty()
            .sortedBy { it.substringAfter("/") }
            .map {
                Uri.parse("file:///android_asset/$folder/$targetFolder/$it")
            }
            .map {
                val (width, height) = ImageUtils.getImageWidthHeight(application, it)
                CatalogImage(
                    uri = it,
                    width = width,
                    height = height,
                )
            }
    }
}
