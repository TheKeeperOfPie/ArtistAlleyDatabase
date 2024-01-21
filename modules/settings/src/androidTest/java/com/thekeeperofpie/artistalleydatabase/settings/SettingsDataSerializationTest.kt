package com.thekeeperofpie.artistalleydatabase.settings

import android.app.Application
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.CryptoUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.encodeToString
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.to
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.test_utils.withDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.reflect.full.createType

class SettingsDataSerializationTest {

    companion object {
        private val application =
            InstrumentationRegistry.getInstrumentation().context.applicationContext as Application
        private val appJson = AppJson()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun writeRead() = runTest {
        val artEntryOne = ArtEntrySamples.build("one")
        val artEntryTwo = ArtEntrySamples.build("two")
        val cropUri = Uri.parse("content://some/path/ArtistAlleyDatabaseCrop.png")
        val networkLoggingLevel = NetworkSettings.NetworkLoggingLevel.BASIC

        val data = SettingsData(
            artEntryTemplate = artEntryOne,
            cropDocumentUri = cropUri,
            networkLoggingLevel = networkLoggingLevel,
            searchQuery = artEntryTwo,
            collapseAnimeFiltersOnClose = true,
            showAdult = true,
        )

        val testParams = listOf(
            artEntryOne to ArtEntry::class to SettingsData::artEntryTemplate,
            cropUri to Uri::class to SettingsData::cropDocumentUri,
            networkLoggingLevel to NetworkSettings.NetworkLoggingLevel::class to
                    SettingsData::networkLoggingLevel,
            artEntryTwo to ArtEntry::class to SettingsData::searchQuery,
            true to Boolean::class to SettingsData::collapseAnimeFiltersOnClose,
            true to Boolean::class to SettingsData::showAdult,
        )

        val provider = SettingsProvider(
            application,
            CryptoUtils.masterKey(application),
            appJson,
            sharedPreferencesFileName = "${SettingsProvider.PREFERENCES_NAME}-test",
            object : FeatureOverrideProvider {
                override val isReleaseBuild = false
                override val enableAppMediaPlayerCache = false
            }
        )

        // In case any old test state remains, clear it
        provider.sharedPreferences.edit().clear().commit()
        testParams.forEach { (_, _, property) ->
            assertThat(provider.sharedPreferences.contains(property.name)).isFalse()
        }

        try {
            // TODO: This still doesn't work
            // StateFlow collect is captured in a Job so that it can be cancel,
            // since StateFlows are hot and will never complete, causing test failure
            val job = launch { withDispatchers { provider.initialize(this) } }
            provider.overwrite(data)
            advanceUntilIdle()
            job.cancelAndJoin()

            testParams.forEach { (value, kClass, property) ->
                assertThat(property.get(provider.settingsData)).isEqualTo(value)
                assertThat(provider.sharedPreferences.getString(property.name, ""))
                    .isEqualTo(appJson.json.encodeToString(kClass.createType(), value))
            }
        } finally {
            provider.sharedPreferences.edit().clear().commit()
        }
    }
}
