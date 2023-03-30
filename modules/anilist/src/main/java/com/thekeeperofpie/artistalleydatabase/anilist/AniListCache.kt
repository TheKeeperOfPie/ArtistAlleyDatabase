package com.thekeeperofpie.artistalleydatabase.anilist

import android.app.Application
import okhttp3.Cache
import java.io.File

class AniListCache(application: Application) {

    val cache by lazy {
        Cache(
            directory = File(application.cacheDir, "aniList"),
            maxSize = 500L * 1024L * 1024L // 500 MiB
        )
    }
}