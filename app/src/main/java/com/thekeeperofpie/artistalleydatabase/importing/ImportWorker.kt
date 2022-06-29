package com.thekeeperofpie.artistalleydatabase.importing

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.squareup.moshi.JsonReader
import com.thekeeperofpie.artistalleydatabase.Converters
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okio.buffer
import okio.source
import java.io.FilterInputStream
import java.io.InputStream
import java.math.BigDecimal
import java.nio.file.Paths
import java.util.Date
import java.util.zip.ZipInputStream
import kotlin.io.path.nameWithoutExtension

@HiltWorker
class ImportWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val artEntryDao: ArtEntryDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val uriString = params.inputData.getString(ImportUtils.KEY_INPUT_CONTENT_URI)
            ?: return Result.failure()

        // Default to true to avoid accidentally overwrites
        val dryRun = params.inputData.getBoolean(ImportUtils.KEY_DRY_RUN, true)
        val uri = Uri.parse(uriString)

        // First open only counts and inserts entries
        val entriesSize =
            (appContext.contentResolver.openInputStream(uri) ?: return Result.failure())
                .use { fileInput ->
                    ZipInputStream(fileInput).use { zipInput ->
                        var count = 0
                        var entry = zipInput.nextEntry
                        while (entry != null) {
                            val entryInputStream = object : FilterInputStream(zipInput) {
                                override fun close() {
                                    // Do nothing
                                }
                            }

                            when (entry.name) {
                                "art_entries.json" -> count = readArtEntriesJson(
                                    entryInputStream,
                                    dryRun,
                                )
                            }
                            zipInput.closeEntry()
                            entry = zipInput.nextEntry
                        }
                        count
                    }
                }

        // Second pass uses previous count to determine progress and does image copying
        (appContext.contentResolver.openInputStream(uri) ?: return Result.failure())
            .use { fileInput ->
                ZipInputStream(fileInput).use { zipInput ->
                    var count = 0
                    var entry = zipInput.nextEntry
                    while (entry != null) {
                        val entryInputStream = object : FilterInputStream(zipInput) {
                            override fun close() {
                                // Do nothing
                            }
                        }

                        if (entry.name != "art_entries.json") {
                            if (!dryRun) {
                                ArtEntryUtils.getImageFile(
                                    appContext,
                                    Paths.get(entry.name).nameWithoutExtension
                                )
                                    .outputStream()
                                    .use { entryInputStream.copyTo(it) }
                            }
                            count++
                            setProgressAsync(
                                Data.Builder().putFloat(
                                    ImportUtils.KEY_PROGRESS,
                                    (count / entriesSize.toFloat()).coerceIn(0f, 1f)
                                ).build()
                            )
                        }
                        zipInput.closeEntry()
                        entry = zipInput.nextEntry
                    }
                }
            }

        return Result.success()
    }

    /**
     * @return number of valid entries found
     */
    private suspend fun readArtEntriesJson(input: InputStream, dryRun: Boolean): Int {
        var count = 0
        input.source().use {
            it.buffer().use {
                val reader = JsonReader.of(it)
                reader.isLenient = true
                reader.beginObject()
                val rootName = reader.nextName()
                if (rootName != "art_entries") {
                    reader.skipValue()
                }

                reader.beginArray()

                val block: suspend (insert: suspend (ArtEntry) -> Unit) -> Unit = { insert ->
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
                            continue
                        }

                        count++
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

                if (dryRun) {
                    block {}
                } else {
                    artEntryDao.insertEntriesDeferred(block)
                }
                reader.endArray()
                reader.endObject()
            }
        }

        return count
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