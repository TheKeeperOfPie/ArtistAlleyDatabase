package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import javax.inject.Inject

@HiltAndroidApp
class CustomApplication : Application(), Configuration.Provider {

    companion object {
        const val TAG = "ArtistAlleyDatabase"
    }

    val scope = MainScope()

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        filesDir.toPath().resolve("entry_images").toFile().mkdirs()
    }
}