package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.form.EntrySettings
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import okio.buffer
import okio.sink
import java.io.ByteArrayOutputStream

class SettingsProvider(
    application: Application,
    private val appJson: AppJson,
) : ArtSettings, EntrySettings {

    companion object {
        private const val KEY_ART_ENTRY_TEMPLATE = "art_entry_template"
        private const val KEY_ART_ENTRY_CROP_DOCUMENT_URI = "art_entry_crop_document_uri"
        private const val KEY_ADVANCED_SEARCH_QUERY = "advanced_search_query"
    }

    override fun saveArtEntryTemplate(entry: ArtEntry) {
        ByteArrayOutputStream().use {
            it.sink().use {
                it.buffer().use {
                    appJson.json.encodeToString(entry)
                }
            }

            val template = String(it.toByteArray())
            sharedPreferences.edit()
                .putString(KEY_ART_ENTRY_TEMPLATE, template)
                .apply()
        }
    }

    override fun loadArtEntryTemplate(): ArtEntry? {
        var stringValue: String? = null
        return try {
            stringValue = sharedPreferences.getString(KEY_ART_ENTRY_TEMPLATE, "")
                .takeUnless(String?::isNullOrEmpty) ?: return null
            appJson.json.decodeFromString(stringValue)
        } catch (e: Exception) {
            Log.e("SettingsProvider", "Error loading art entry template: $stringValue", e)
            null
        }
    }

    override fun saveCropDocumentUri(uri: Uri) {
        sharedPreferences.edit()
            .putString(KEY_ART_ENTRY_CROP_DOCUMENT_URI, uri.toString())
            .apply()
    }

    override fun loadCropDocumentUri(): Uri? {
        var stringValue: String? = null
        return try {
            stringValue = sharedPreferences.getString(KEY_ART_ENTRY_CROP_DOCUMENT_URI, "")
            if (stringValue.isNullOrEmpty()) return null
            stringValue.toUri()
        } catch (e: Exception) {
            Log.e("SettingsProvider", "Error loading crop document URI: $stringValue", e)
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