package com.thekeeperofpie.artistalleydatabase.importing

import android.util.Log
import com.squareup.moshi.JsonReader
import com.thekeeperofpie.artistalleydatabase.Converters
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.util.Date

object ImportUtils {
    const val UNIQUE_WORK_NAME = "import_entries"
    const val KEY_INPUT_CONTENT_URI = "input_content_uri"
    const val KEY_DRY_RUN = "dry_run"
    const val KEY_PROGRESS = "progress"

    fun readArtEntryObject(reader: JsonReader): ArtEntry? {
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
            when (val name = reader.nextName()) {
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
                    var printSizeLocked = false
                    while (reader.peek() != JsonReader.Token.END_OBJECT) {
                        when (val locksName = reader.nextName()) {
                            "artistsLocked" -> artistsLocked = reader.readBoolean()
                            "sourceLocked" -> sourceLocked = reader.readBoolean()
                            "seriesLocked" -> seriesLocked = reader.readBoolean()
                            "charactersLocked" -> charactersLocked =
                                reader.readBoolean()
                            "tagsLocked" -> tagsLocked = reader.readBoolean()
                            "notesLocked" -> notesLocked = reader.readBoolean()
                            "printSizeLocked" -> printSizeLocked =
                                reader.readBoolean()
                            else -> {
                                Log.e("Import", "Unexpected name = $locksName")
                                reader.skipValue()
                            }
                        }
                    }
                    reader.endObject()
                    locks = ArtEntry.Locks(
                        artistsLocked = artistsLocked,
                        sourceLocked = sourceLocked,
                        seriesLocked = seriesLocked,
                        charactersLocked = charactersLocked,
                        tagsLocked = tagsLocked,
                        notesLocked = notesLocked,
                        printSizeLocked = printSizeLocked,
                    )
                }
                else -> {
                    Log.e("Import", "Unexpected name = $name")
                    reader.skipValue()
                }
            }
        }
        reader.endObject()

        if (id == null) {
            return null
        }

        return ArtEntry(
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
        else -> {
            skipValue()
            null
        }
    }.let(Converters.DateConverter::deserializeDate)

    private fun JsonReader.readNullableString() = when (peek()) {
        JsonReader.Token.NULL -> nextNull<String>()
        JsonReader.Token.STRING -> {
            val value = nextString()
            if (value == "null") null else value
        }
        else -> {
            skipValue()
            null
        }
    }

    private fun JsonReader.readNullableInt() = when (peek()) {
        JsonReader.Token.NULL -> nextNull<Int>()
        JsonReader.Token.STRING -> nextString().toIntOrNull()
        JsonReader.Token.NUMBER -> nextInt()
        else -> {
            skipValue()
            null
        }
    }

    private fun JsonReader.readBoolean() = when (peek()) {
        JsonReader.Token.NULL -> {
            nextNull<Boolean>()
            false
        }
        JsonReader.Token.STRING -> nextString().toBoolean()
        JsonReader.Token.BOOLEAN -> nextBoolean()
        else -> {
            skipValue()
            false
        }
    }
}