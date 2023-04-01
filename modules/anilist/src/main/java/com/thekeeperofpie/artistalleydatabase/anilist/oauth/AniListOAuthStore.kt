package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.browser.customtabs.CustomTabsIntent
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.BuildConfig
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response

class AniListOAuthStore(private val application: Application) : Interceptor {

    companion object {
        const val MASTER_KEY_ALIAS = "ArtistAlleyDatabaseKey"
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
                "?client_id=${BuildConfig.ANILIST_CLIENT_ID}" +
                "&response_type=token"
    }

    private val masterKey = MasterKey.Builder(application, MASTER_KEY_ALIAS)
        .setKeyGenParameterSpec(
            KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        .setRequestStrongBoxBacked(true)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        application,
        SHARED_PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val authToken = MutableStateFlow<String?>(null)

    val hasAuth = authToken.map { !it.isNullOrBlank() }

    init {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(CustomDispatchers.IO) {
            authToken.tryEmit(sharedPreferences.getString(KEY_AUTH_TOKEN, null))
        }
    }

    fun launchAuthRequest(activity: Activity) {
        setShareTargetEnabled(application, true)
        CustomTabsIntent.Builder()
            .build()
            .launchUrl(activity, Uri.parse(ANILIST_OAUTH_URL))
    }

    suspend fun storeAuthTokenResult(token: String) {
        @Suppress("ApplySharedPref")
        sharedPreferences.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .commit()

        authToken.emit(token)
    }

    suspend fun clearAuthToken() {
        @Suppress("ApplySharedPref")
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .commit()

        authToken.emit(null)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = authToken.value
        return if (token == null) {
            chain.proceed(chain.request())
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
                .let(chain::proceed)
        }
    }
}