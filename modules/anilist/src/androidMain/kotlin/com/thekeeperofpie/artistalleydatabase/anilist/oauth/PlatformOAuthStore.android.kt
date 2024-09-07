package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityOptionsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.secrets.Secrets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
actual class PlatformOAuthStore(
    application: Application,
    masterKey: MasterKey,
) {
    companion object {
        const val SHARED_PREFS_FILE_NAME = "aniList_encrypted"
        const val KEY_AUTH_TOKEN = "auth_token"

        fun setShareTargetEnabled(application: Application, enabled: Boolean) {
            @Suppress("InlinedApi")
            application.packageManager.setComponentEnabledSetting(
                ComponentName(
                    application,
                    AniListOAuthShareTargetActivity::class.java
                ),
                if (enabled) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                },
                PackageManager.DONT_KILL_APP or PackageManager.SYNCHRONOUS
            )
        }

        val ANILIST_OAUTH_URL = "https://anilist.co/api/v2/oauth/authorize" +
                "?client_id=${Secrets.aniListClientId}" +
                "&response_type=token"
    }

    private val sharedPreferences = EncryptedSharedPreferences.create(
        application,
        SHARED_PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun launchAuthRequest(activity: Activity) {
        activity.startActivity(
            Intent(activity, AniListOAuthTrampolineActivity::class.java),
            ActivityOptionsCompat.makeCustomAnimation(activity, 0, 0).toBundle(),
        )
    }

    internal actual val authTokenState = MutableStateFlow(sharedPreferences.getString(KEY_AUTH_TOKEN, null))
    private val authTokenMutex = Mutex()

    internal actual suspend fun storeAuthTokenResult(token: String) {
        @Suppress("ApplySharedPref")
        sharedPreferences.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .commit()

        authTokenMutex.withLock {
            authTokenState.emit(token)
        }
    }

    internal actual suspend fun clearAuthToken() {
        @Suppress("ApplySharedPref")
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .commit()

        authTokenMutex.withLock {
            authTokenState.emit(null)
        }
    }
}
