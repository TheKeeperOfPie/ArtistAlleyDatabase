package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
import android.content.Context
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings

class ArtistAlleyAppSettings(application: Application) : ArtistAlleySettings {

    private val sharedPrefs =
        application.getSharedPreferences("shared_prefs", Context.MODE_PRIVATE)

    override var lastKnownCsvSize: Long
        get() = sharedPrefs.getLong("lastKnownCsvSize", -1)
        set(value) {
            sharedPrefs.edit()
                .putLong("lastKnownCsvSize", value)
                .apply()
        }

    override var displayType: String
        get() = sharedPrefs.getString("displayType", null).orEmpty()
        set(value) {
            sharedPrefs.edit()
                .putString("displayType", value)
                .apply()
        }
}
