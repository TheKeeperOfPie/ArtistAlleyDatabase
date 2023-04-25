package com.thekeeperofpie.artistalleydatabase.settings

import android.app.Application
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.entry.EntrySettings
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.serializer
import kotlin.reflect.KProperty0
import kotlin.reflect.full.createType

class SettingsProvider constructor(
    application: Application,
    masterKey: MasterKey,
    private val appJson: AppJson,
    sharedPreferencesFileName: String = PREFERENCES_NAME,
) : ArtSettings, EntrySettings, NetworkSettings, AnimeSettings {

    companion object {
        const val EXPORT_FILE_NAME = "settings.json"
        const val PREFERENCES_NAME = "settings"
    }

    @VisibleForTesting
    val sharedPreferences = EncryptedSharedPreferences.create(
        application,
        sharedPreferencesFileName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override val artEntryTemplate = MutableStateFlow<ArtEntry?>(deserialize("artEntryTemplate"))
    override val cropDocumentUri = MutableStateFlow<Uri?>(deserialize("cropDocumentUri"))
    override val networkLoggingLevel = MutableStateFlow(
        deserialize("networkLoggingLevel") ?: NetworkSettings.NetworkLoggingLevel.NONE
    )
    override var savedAnimeFilters = MutableStateFlow(deserializeAnimeFilters())
    override var collapseAnimeFiltersOnClose = MutableStateFlow(
        deserialize("collapseAnimeFiltersOnClose") ?: true
    )
    override var showAdult = MutableStateFlow(deserialize("showAdult") ?: false)
    var searchQuery = MutableStateFlow<ArtEntry?>(deserialize("searchQuery"))

    private fun deserializeAnimeFilters(): Map<String, FilterData> {
        val stringValue = sharedPreferences.getString("savedAnimeFilters", "")
        if (stringValue.isNullOrBlank()) return emptyMap()
        return appJson.json.decodeFromString(stringValue)
    }

    val settingsData: SettingsData
        get() = SettingsData(
            artEntryTemplate = artEntryTemplate.value,
            cropDocumentUri = cropDocumentUri.value,
            networkLoggingLevel = networkLoggingLevel.value,
            searchQuery = searchQuery.value,
            collapseAnimeFiltersOnClose = collapseAnimeFiltersOnClose.value,
            savedAnimeFilters = savedAnimeFilters.value,
            showAdult = showAdult.value,
        )

    // Initialization separated into its own method so that tests can cancel the StateFlow job
    fun initialize(scope: CoroutineScope) {
        subscribeProperty(scope, ::artEntryTemplate)
        subscribeProperty(scope, ::cropDocumentUri)
        subscribeProperty(scope, ::networkLoggingLevel)
        subscribeProperty(scope, ::searchQuery)
        subscribeProperty(scope, ::collapseAnimeFiltersOnClose)
        subscribeProperty(scope, ::showAdult)
        scope.launch(CustomDispatchers.IO) {
            savedAnimeFilters.drop(1).collectLatest {
                val stringValue = appJson.json.run {
                    encodeToString(it)
                }
                sharedPreferences.edit()
                    .putString("savedAnimeFilters", stringValue)
                    .apply()
            }
        }
    }

    private inline fun <reified T> subscribeProperty(
        scope: CoroutineScope,
        property: KProperty0<MutableStateFlow<T>>
    ) = scope.launch(CustomDispatchers.IO) {
        property.get().drop(1).collectLatest {
            serialize(property.name, it)
        }
    }

    suspend fun overwrite(data: SettingsData) {
        artEntryTemplate.emit(data.artEntryTemplate)
        cropDocumentUri.emit(data.cropDocumentUri)
        networkLoggingLevel.emit(data.networkLoggingLevel)
        searchQuery.emit(data.searchQuery)
        savedAnimeFilters.emit(data.savedAnimeFilters)
        collapseAnimeFiltersOnClose.emit(data.collapseAnimeFiltersOnClose)
        showAdult.emit(data.showAdult)
    }

    private inline fun <reified T> deserialize(name: String): T? {
        val stringValue = sharedPreferences.getString(name, "")
        return if (stringValue.isNullOrBlank()) {
            null
        } else {
            appJson.json.decodeFromString<T>(stringValue)
        }
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
