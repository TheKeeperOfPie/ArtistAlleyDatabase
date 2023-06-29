package com.thekeeperofpie.artistalleydatabase.alley

import android.app.Application
import android.net.Uri

object ArtistAlleyUtils {

    fun getImages(application: Application, booth: String): List<Uri> {
        val assetManager = application.assets
        val boothFolder = assetManager.list("catalogs")?.find { it.startsWith(booth) }
        return assetManager.list("catalogs/$boothFolder")
            ?.flatMap {
                if (it.endsWith(".pdf")) {
                    emptyList()
                } else if (it.startsWith(booth)) {
                    try {
                        val subFolder = it
                        application.assets.list("catalogs/$boothFolder/$subFolder")?.map {
                            Uri.parse("file:///android_asset/catalogs/$boothFolder/$subFolder/$it")
                        }.orEmpty()
                    } catch (ignored: Throwable) {
                        emptyList()
                    }
                } else {
                    listOf(Uri.parse("file:///android_asset/catalogs/$boothFolder/$it"))
                }
            }
            .orEmpty()
    }
}
