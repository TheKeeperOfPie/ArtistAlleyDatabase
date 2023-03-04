package com.thekeeperofpie.artistalleydatabase.settings

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.encodeToString
import com.thekeeperofpie.artistalleydatabase.android_utils.to
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.test.ArtEntrySamples
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.reflect.full.createType

class SettingsDataSerializationTest {

    companion object {
        private val application =
            InstrumentationRegistry.getInstrumentation().context.applicationContext as Application
        private val appJson = AppJson()
    }

    @Test
    fun writeRead() {
        val artEntryOne = ArtEntrySamples.build("one")
        val artEntryTwo = ArtEntrySamples.build("two")
        val cropUri = Uri.parse("content://some/path/ArtistAlleyDatabaseCrop.png")

        val sharedPreferences = application.getSharedPreferences(
            SettingsProvider.PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

        val data = SettingsData(appJson, sharedPreferences).apply {
            artEntryTemplate = artEntryOne
            cropDocumentUri = cropUri
            searchQuery = artEntryTwo
        }

        // Delete what might have been saved by constructing the test SettingsData
        sharedPreferences.edit().clear().commit()

        val provider = SettingsProvider(application, appJson).apply { overwrite(data) }

        listOf(
            artEntryOne to ArtEntry::class to SettingsData::artEntryTemplate,
            cropUri to Uri::class to SettingsData::cropDocumentUri,
            artEntryTwo to ArtEntry::class to SettingsData::searchQuery,
        ).forEach { (value, kClass, property) ->
            assertEquals(value, property.get(provider.settingsData))
            assertEquals(
                appJson.json.encodeToString(kClass.createType(), value),
                sharedPreferences.getString(property.name, "")
            )
        }
    }
}