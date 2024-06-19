package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
import android.content.Context
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings

class ArtistAlleyAppSettings(application: Application) : ArtistAlleySettings {

    private val sharedPrefs =
        application.getSharedPreferences("shared_prefs", Context.MODE_PRIVATE)

    override var lastKnownArtistsCsvSize: Long
        get() = sharedPrefs.getLong("lastKnownArtistsCsvSize", -1)
        set(value) {
            sharedPrefs.edit()
                .putLong("lastKnownArtistsCsvSize", value)
                .apply()
        }

    override var lastKnownStampRalliesCsvSize: Long
        get() = sharedPrefs.getLong("lastKnownStampRalliesCsvSize", -1)
        set(value) {
            sharedPrefs.edit()
                .putLong("lastKnownStampRalliesCsvSize", value)
                .apply()
        }

    override var displayType: String
        get() = sharedPrefs.getString("displayType", null).orEmpty()
        set(value) {
            sharedPrefs.edit()
                .putString("displayType", value)
                .apply()
        }

    override var artistsSortOption: String
        get() = sharedPrefs.getString("artistsSortOption", null).orEmpty()
        set(value) {
            sharedPrefs.edit()
                .putString("artistsSortOption", value)
                .apply()
        }

    override var artistsSortAscending: Boolean
        get() = sharedPrefs.getBoolean("artistsSortAscending", true)
        set(value) {
            sharedPrefs.edit()
                .putBoolean("artistsSortAscending", value)
                .apply()
        }

    override var stampRalliesSortOption: String
        get() = sharedPrefs.getString("stampRalliesSortOption", null).orEmpty()
        set(value) {
            sharedPrefs.edit()
                .putString("stampRalliesSortOption", value)
                .apply()
        }

    override var stampRalliesSortAscending: Boolean
        get() = sharedPrefs.getBoolean("stampRalliesSortAscending", true)
        set(value) {
            sharedPrefs.edit()
                .putBoolean("stampRalliesSortAscending", value)
                .apply()
        }

    override var showGridByDefault: Boolean
        get() = sharedPrefs.getBoolean("showGridByDefault", false)
        set(value) {
            sharedPrefs.edit()
                .putBoolean("showGridByDefault", value)
                .apply()
        }
}
