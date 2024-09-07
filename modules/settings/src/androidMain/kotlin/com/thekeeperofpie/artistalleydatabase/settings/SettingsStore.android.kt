package com.thekeeperofpie.artistalleydatabase.settings

import android.annotation.SuppressLint
import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
actual class SettingsStore(
    application: Application,
    masterKey: MasterKey,
) {
    companion object {
        const val EXPORT_FILE_NAME = "settings.json"
        const val PREFERENCES_NAME = "settings"
    }

    @VisibleForTesting
    val sharedPreferences = EncryptedSharedPreferences.create(
        application,
        PREFERENCES_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    @SuppressLint("ApplySharedPref")
    actual fun writeString(
        key: String,
        value: String,
        commitImmediately: Boolean,
    ) {
        sharedPreferences.edit()
            .putString(key, value)
            .run {
                if (commitImmediately) {
                    commit()
                } else {
                    apply()
                }
            }
    }

    actual fun readString(key: String) = sharedPreferences.getString(key, null)
}
