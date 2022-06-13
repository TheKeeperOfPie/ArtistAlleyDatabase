package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        filesDir.toPath().resolve("entry_images").toFile().mkdirs()
    }
}