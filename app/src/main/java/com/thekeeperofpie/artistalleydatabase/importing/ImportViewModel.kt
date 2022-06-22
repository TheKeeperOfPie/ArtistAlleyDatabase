package com.thekeeperofpie.artistalleydatabase.importing

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.JsonReader
import com.thekeeperofpie.artistalleydatabase.Converters
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okio.buffer
import okio.source
import java.io.FilterInputStream
import java.io.InputStream
import java.math.BigDecimal
import java.util.Date
import java.util.zip.ZipInputStream
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val application: Application,
    private val artEntryDao: ArtEntryDao,
) : ViewModel() {

    var importUriString by mutableStateOf<String?>(null)
    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    fun onClickImport(onDone: () -> Unit) {
        val importUriString = importUriString ?: run {
            errorResource = R.string.invalid_import_source to null
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val uri = Uri.parse(importUriString)
            try {
                application.contentResolver.openInputStream(uri)
                    ?: run {
                        withContext(Dispatchers.Main) {
                            errorResource = R.string.invalid_import_source to null
                        }
                        return@launch
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorResource = R.string.invalid_import_source to e
                }
                return@launch
            }.use { fileInput ->
                ZipInputStream(fileInput).use { zipInput ->
                    var entry = zipInput.nextEntry
                    while (entry != null) {
                        val entryInputStream = object : FilterInputStream(zipInput) {
                            override fun close() {
                                // Do nothing
                            }
                        }
                        when (entry.name) {
                            "art_entries.json" -> readArtEntriesJson(entryInputStream)
                            else -> {
                                ArtEntryUtils.getImageFile(application, entry.name)
                                    .outputStream()
                                    .use { entryInputStream.copyTo(it) }
                            }
                        }
                        zipInput.closeEntry()
                        entry = zipInput.nextEntry
                    }
                }
            }

            withContext(Dispatchers.Main) { onDone() }
        }
    }

    private suspend fun readArtEntriesJson(input: InputStream) {
        input.source().use {
            it.buffer().use {
                val reader = JsonReader.of(it)
                reader.isLenient = true
                reader.beginObject()
                val name = reader.nextName()
                if (name != "art_entries") {
                    reader.skipValue()
                }

                reader.beginArray()

                artEntryDao.insertEntriesDeferred { insert ->
                    while (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                        var id: String? = null
                        val artists = mutableListOf<String>()
                        var sourceType: String? = null
                        var sourceValue: String? = null
                        val series = mutableListOf<String>()
                        val characters = mutableListOf<String>()
                        val tags = mutableListOf<String>()
                        var price: BigDecimal? = null
                        var date: Date? = null
                        var lastEditTime: Date? = null
                        var imageWidth: Int? = null
                        var imageHeight: Int? = null
                        var printWidth: Int? = null
                        var printHeight: Int? = null
                        var notes: String? = null
                        var locks: ArtEntry.Locks? = null
                        reader.beginObject()
                        while (reader.peek() != JsonReader.Token.END_OBJECT) {
                            when (reader.nextName()) {
                                "id" -> id = reader.nextString()
                                    .removePrefix("\"")
                                    .removeSuffix("\"")
                                "artists" -> reader.readStringArray(artists)
                                "sourceType" -> sourceType = reader.readNullableString()
                                "sourceValue" -> sourceValue = reader.readNullableString()
                                "series" -> reader.readStringArray(series)
                                "characters" -> reader.readStringArray(characters)
                                "tags" -> reader.readStringArray(tags)
                                "price" -> {
                                    price = Converters.BigDecimalConverter
                                        .deserializeBigDecimal(reader.readNullableString())
                                }
                                "date" -> date = reader.readDate()
                                "imageWidth" -> imageWidth = reader.readNullableInt()
                                "imageHeight" -> imageHeight = reader.readNullableInt()
                                "printWidth" -> printWidth = reader.readNullableInt()
                                "printHeight" -> printHeight = reader.readNullableInt()
                                "lastEditTime" -> lastEditTime = reader.readDate()
                                "notes" -> notes = reader.readNullableString()
                                "locks" -> {
                                    reader.beginObject()
                                    var artistsLocked = false
                                    var sourceLocked = false
                                    var seriesLocked = false
                                    var charactersLocked = false
                                    var tagsLocked = false
                                    var notesLocked = false
                                    while (reader.peek() != JsonReader.Token.END_OBJECT) {
                                        when (reader.nextName()) {
                                            "artistsLocked" -> artistsLocked = reader.nextBoolean()
                                            "sourceLocked" -> sourceLocked = reader.nextBoolean()
                                            "seriesLocked" -> seriesLocked = reader.nextBoolean()
                                            "charactersLocked" -> charactersLocked =
                                                reader.nextBoolean()
                                            "tagsLocked" -> tagsLocked = reader.nextBoolean()
                                            "notesLocked" -> notesLocked = reader.nextBoolean()
                                        }
                                    }
                                    reader.endObject()
                                    locks = ArtEntry.Locks(
                                        artistsLocked,
                                        sourceLocked,
                                        seriesLocked,
                                        charactersLocked,
                                        tagsLocked,
                                        notesLocked
                                    )
                                }
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()

                        if (id == null) {
                            continue
                        }

                        insert(
                            ArtEntry(
                                id = id,
                                artists = artists,
                                sourceType = sourceType,
                                sourceValue = sourceValue,
                                series = series,
                                characters = characters,
                                tags = tags,
                                price = price,
                                date = date,
                                lastEditTime = lastEditTime,
                                imageWidth = imageWidth,
                                imageHeight = imageHeight,
                                printWidth = printWidth,
                                printHeight = printHeight,
                                notes = notes,
                                locks = locks ?: ArtEntry.Locks.EMPTY
                            )
                        )
                    }
                }
                reader.endArray()
                reader.endObject()
            }
        }
    }

    private fun JsonReader.readStringArray(output: MutableList<String>) {
        if (peek() == JsonReader.Token.STRING) {
            output.addAll(Json.Default.decodeFromString<List<String>>(nextString()))
            return
        }
        beginArray()
        while (peek() != JsonReader.Token.END_ARRAY) {
            output += nextString()
        }
        endArray()
    }

    private fun JsonReader.readDate() = when (peek()) {
        JsonReader.Token.NULL -> nextNull<Long>()
        JsonReader.Token.STRING -> nextString().toLongOrNull()
        JsonReader.Token.NUMBER -> nextLong()
        else -> null
    }.let(Converters.DateConverter::deserializeDate)

    private fun JsonReader.readNullableString() = when (peek()) {
        JsonReader.Token.NULL -> nextNull<String>()
        JsonReader.Token.STRING -> {
            val value = nextString()
            if (value == "null") null else value
        }
        else -> null
    }

    private fun JsonReader.readNullableInt() = when (peek()) {
        JsonReader.Token.NULL -> nextNull<Int>()
        JsonReader.Token.STRING -> nextString().toIntOrNull()
        JsonReader.Token.NUMBER -> nextInt()
        else -> null
    }
}
