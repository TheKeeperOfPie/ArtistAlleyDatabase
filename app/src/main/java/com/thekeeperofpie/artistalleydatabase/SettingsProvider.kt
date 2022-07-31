package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import android.content.Context
import android.util.Log
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.json.AppJson
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import okio.buffer
import okio.sink
import okio.source
import java.io.ByteArrayOutputStream

class SettingsProvider(
    application: Application,
    private val appMoshi: AppMoshi,
    private val appJson: AppJson,
) {

    companion object {
        private const val KEY_ART_ENTRY_TEMPLATE = "art_entry_template"
        private const val KEY_ADVANCED_SEARCH_QUERY = "advanced_search_query"
    }

    fun saveArtEntryTemplate(entry: ArtEntry) {
        ByteArrayOutputStream().use {
            it.sink().use {
                it.buffer().use {
                    appMoshi.artEntryAdapter.toJson(entry)
                }
            }

            val template = String(it.toByteArray())
            sharedPreferences.edit()
                .putString(KEY_ART_ENTRY_TEMPLATE, template)
                .apply()
        }
    }

    fun loadArtEntryTemplate(): ArtEntry? {
        var stringValue: String? = null
        return try {
            stringValue = sharedPreferences.getString(KEY_ART_ENTRY_TEMPLATE, "")
                .takeUnless(String?::isNullOrEmpty) ?: return null
            stringValue.byteInputStream().use {
                it.source().use {
                    it.buffer().use(appMoshi.artEntryAdapter::fromJson)
                }
            }
        } catch (e: Exception) {
            Log.e("SettingsProvider", "Error loading art entry template: $stringValue", e)
            null
        }
    }

    fun saveSearchQuery(entry: ArtEntry) {
        val stringValue = appJson.json.encodeToString(entry)
        sharedPreferences.edit()
            .putString(KEY_ADVANCED_SEARCH_QUERY, stringValue)
            .apply()
    }

    fun loadSearchQuery(): ArtEntry? {
        var stringValue: String? = null
        return try {
            stringValue = sharedPreferences.getString(KEY_ADVANCED_SEARCH_QUERY, "")
                .takeUnless(String?::isNullOrEmpty) ?: return null
            appJson.json.decodeFromString(stringValue)
        } catch (e: Exception) {
            Log.e("SettingsProvider", "Error loading search query: $stringValue", e)
            null
        }
    }

    private val sharedPreferences =
        application.getSharedPreferences("settings", Context.MODE_PRIVATE)
}