package com.thekeeperofpie.artistalleydatabase.settings

import android.content.SharedPreferences
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.art.test.ArtEntrySamples
import com.thekeeperofpie.artistalleydatabase.test_utils.doNothing
import com.thekeeperofpie.artistalleydatabase.test_utils.mock
import com.thekeeperofpie.artistalleydatabase.test_utils.mockWhen
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString

class SettingsDataSerializationTest {

    private val appJson = AppJson()
    private val sharedPreferences = mock<SharedPreferences> {
        mockWhen(edit()) {
            mockWhen(putString(anyString(), anyString())) {
                doNothing { apply() }
            }
        }
    }

    @Test
    fun writeRead() {
        SettingsData(appJson, sharedPreferences).apply {
            searchQuery = ArtEntrySamples.ONE
        }
    }
}