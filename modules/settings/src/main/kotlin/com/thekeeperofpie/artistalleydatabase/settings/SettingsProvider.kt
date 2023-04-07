package com.thekeeperofpie.artistalleydatabase.settings

import android.app.Application
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters
import com.thekeeperofpie.artistalleydatabase.android_utils.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.entry.EntrySettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.serializer
import kotlin.reflect.KProperty0
import kotlin.reflect.full.createType

class SettingsProvider constructor(
    application: Application,
    masterKey: MasterKey,
    private val appJson: AppJson,
    sharedPreferencesFileName: String = PREFERENCES_NAME,
) : ArtSettings, EntrySettings, NetworkSettings {

    companion object {
        const val EXPORT_FILE_NAME = "settings.json"
        const val PREFERENCES_NAME = "settings"
    }

    override val artEntryTemplate = MutableStateFlow<ArtEntry?>(null)
    override val cropDocumentUri = MutableStateFlow<Uri?>(null)
    override val networkLoggingLevel = MutableStateFlow(NetworkSettings.NetworkLoggingLevel.NONE)
    var searchQuery = MutableStateFlow<ArtEntry?>(null)

    val serializer = Converters.PropertiesSerializer(SettingsData::class, appJson)

    @VisibleForTesting
    val sharedPreferences = EncryptedSharedPreferences.create(
        application,
        sharedPreferencesFileName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val settingsData: SettingsData
        get() = SettingsData(
            artEntryTemplate = artEntryTemplate.value,
            cropDocumentUri = cropDocumentUri.value,
            networkLoggingLevel = networkLoggingLevel.value,
            searchQuery = searchQuery.value
        )

    // Initialization separated into its own method so that tests can cancel the StateFlow job
    fun initialize(scope: CoroutineScope) {
        subscribeProperty(scope, ::artEntryTemplate)
        subscribeProperty(scope, ::cropDocumentUri)
        subscribeProperty(scope, ::networkLoggingLevel)
        subscribeProperty(scope, ::searchQuery)
    }

    private inline fun <reified T> subscribeProperty(
        scope: CoroutineScope,
        property: KProperty0<MutableStateFlow<T>>
    ) = scope.launch(CustomDispatchers.IO) {
        property.get().collectLatest {
            serialize(property.name, it)
        }
    }

    suspend fun overwrite(data: SettingsData) {
        artEntryTemplate.emit(data.artEntryTemplate)
        cropDocumentUri.emit(data.cropDocumentUri)
        networkLoggingLevel.emit(data.networkLoggingLevel)
        searchQuery.emit(data.searchQuery)
    }

    private inline fun <reified T> serialize(name: String, value: T) {
        val stringValue = appJson.json.run {
            encodeToString(
                // Create type as nullable to encapsulate both null and non-null
                serializersModule.serializer(T::class.createType(nullable = true)),
                value
            )
        }
        sharedPreferences.edit()
            .putString(name, stringValue)
            .apply()
    }
}
