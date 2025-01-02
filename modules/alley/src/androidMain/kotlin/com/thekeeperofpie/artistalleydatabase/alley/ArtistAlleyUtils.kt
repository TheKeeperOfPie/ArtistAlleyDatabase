package com.thekeeperofpie.artistalleydatabase.alley

import android.app.Application
import androidx.annotation.WorkerThread
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem

object ArtistAlleyUtils {

    @WorkerThread
    fun getImages(
        application: Application,
        appFileSystem: AppFileSystem,
        folder: String,
        file: String,
    ): List<CatalogImage> {
        val assetManager = application.assets
        val targetName = file.replace("'", "_")
        val targetFolder = assetManager.list(folder)?.find { it.startsWith(targetName) }
        return assetManager.list("$folder/$targetFolder")
            ?.flatMap {
                if (it.endsWith(".pdf")) {
                    emptyList()
                } else if (it.startsWith(targetName)) {
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
                val (width, height) = appFileSystem.getImageWidthHeight(it)
                CatalogImage(
                    uri = it,
                    width = width,
                    height = height,
                )
            }
    }
}
