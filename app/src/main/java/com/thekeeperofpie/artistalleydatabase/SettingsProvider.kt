package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import android.content.Context
import android.util.Log
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.export.ExportUtils.writeMembersAsObject
import com.thekeeperofpie.artistalleydatabase.importing.ImportUtils
import okio.buffer
import okio.sink
import okio.source
import java.io.ByteArrayOutputStream

class SettingsProvider(application: Application) {

    companion object {
        private const val KEY_ART_ENTRY_TEMPLATE = "art_entry_template"
    }

    fun saveArtEntryTemplate(entry: ArtEntry) {
        ByteArrayOutputStream().use {
            it.sink().use {
                it.buffer().use {
                    JsonWriter.of(it)
                        .writeMembersAsObject(ArtEntry::class, entry)
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
                    it.buffer().use {
                        ImportUtils.readArtEntryObject(JsonReader.of(it))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SettingsProvider", "Error loading art entry template: $stringValue", e)
            null
        }
    }

    private val sharedPreferences =
        application.getSharedPreferences("settings", Context.MODE_PRIVATE)
}