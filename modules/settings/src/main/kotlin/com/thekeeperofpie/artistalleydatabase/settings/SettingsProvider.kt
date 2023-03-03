package com.thekeeperofpie.artistalleydatabase.settings

import android.app.Application
import android.content.Context
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters

class SettingsProvider constructor(
    application: Application,
    private val appJson: AppJson,
) {

    companion object {
        const val EXPORT_FILE_NAME = "settings.json"
    }

    val serializer = Converters.PropertiesSerializer(SettingsData::class, appJson)

    private val sharedPreferences =
        application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var settingsData = SettingsData(appJson = appJson, sharedPreferences = sharedPreferences)
        private set

    fun overwrite(data: SettingsData) {
        data.initialize(appJson = appJson, sharedPreferences = sharedPreferences)
        settingsData = data
        serializer.nonTransientProperties()
            .forEach {
                (it.getDelegate(data) as SettingsData.SharedPreferenceDelegate<*>)
                    .onRestore(it, settingsData)
            }
    }
}
