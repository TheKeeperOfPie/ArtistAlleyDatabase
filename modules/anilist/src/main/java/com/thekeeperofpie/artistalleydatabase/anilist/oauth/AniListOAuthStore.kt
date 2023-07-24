package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityOptionsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.BuildConfig
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkAuthProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class AniListOAuthStore(
    application: Application,
    masterKey: MasterKey,
) : NetworkAuthProvider {

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

        const val ANILIST_OAUTH_URL = "https://anilist.co/api/v2/oauth/authorize" +
                "?client_id=${BuildConfig.aniListclientId}" +
                "&response_type=token"
    }

    private val sharedPreferences = EncryptedSharedPreferences.create(
        application,
        SHARED_PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val authToken = MutableStateFlow<String?>(null)
    private val authTokenMutex = Mutex()

    override val authHeader get() = authToken.value?.let { "Bearer $it" }

    val hasAuth = authToken.map { !it.isNullOrBlank() }

    init {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(CustomDispatchers.IO) {
            authTokenMutex.withLock {
                authToken.tryEmit(sharedPreferences.getString(KEY_AUTH_TOKEN, null))
            }
        }
    }

    fun launchAuthRequest(activity: Activity) {
        activity.startActivity(
            Intent(activity, AniListOAuthTrampolineActivity::class.java),
            ActivityOptionsCompat.makeCustomAnimation(activity, 0, 0).toBundle(),
        )
    }

    suspend fun storeAuthTokenResult(token: String) {
        @Suppress("ApplySharedPref")
        sharedPreferences.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .commit()

        authTokenMutex.withLock {
            authToken.emit(token)
        }
    }

    suspend fun clearAuthToken() {
        @Suppress("ApplySharedPref")
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .commit()

        authTokenMutex.withLock {
            authToken.emit(null)
        }
    }
}
