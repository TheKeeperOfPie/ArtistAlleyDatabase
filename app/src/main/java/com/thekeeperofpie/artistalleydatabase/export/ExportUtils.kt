package com.thekeeperofpie.artistalleydatabase.export

import androidx.work.CoroutineWorker
import androidx.work.Data
import com.squareup.moshi.JsonWriter
import com.thekeeperofpie.artistalleydatabase.Converters
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okio.buffer
import okio.sink
import java.io.File
import java.text.DateFormat
import java.time.Instant
import java.util.Date
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

object ExportUtils {

    const val KEY_OUTPUT_CONTENT_URI = "output_content_uri"

    fun currentDateTimeFileName(): String =
        DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)
            .format(Date.from(Instant.now()))

    fun CoroutineWorker.writeEntries(
        artEntryDao: ArtEntryDao,
        file: File,
        onEachEntry: (ArtEntry) -> Unit = {}
    ): Boolean {
        file.sink().use {
            it.buffer().use {
                JsonWriter.of(it).use { jsonWriter ->
                    jsonWriter.beginObject()
                        .name("art_entries")
                        .beginArray()
                    var stopped = false
                    var entriesSize = 0
                    artEntryDao.iterateEntries({ entriesSize = it }) { index, entry ->
                        if (isStopped) {
                            stopped = true
                            return@iterateEntries
                        }

                        onEachEntry(entry)

                        setProgressAsync(
                            Data.Builder()
                                .putFloat(
                                    "progress",
                                    index / entriesSize.coerceAtLeast(1).toFloat()
                                )
                                .build()
                        )

                        jsonWriter.writeMembersAsObject(ArtEntry::class, entry)
                    }
                    if (stopped) {
                        return false
                    }
                    jsonWriter.endArray()
                        .endObject()
                }
            }
        }

        return true
    }

    private fun <T : Any> JsonWriter.writeMembersAsObject(inputKClass: KClass<T>, input: T) {
        beginObject()
        for (property in inputKClass.memberProperties) {
            val name = property.name
            name(name)

            if (name == "locks") {
                writeMembersAsObject(ArtEntry.Locks::class, property.get(input) as ArtEntry.Locks)
                continue
            }

            val value = property.get(input)
            val kType = property.returnType
            val kClass = kType.jvmErasure

            if (value is String) {
                value(value)
            } else {
                @Suppress("UNCHECKED_CAST")
                val serializer =
                    Converters.KSERIALIZERS[kClass] as? KSerializer<Any?>
                        ?: Json.Default.serializersModule.serializer(kType)

                value(Json.Default.encodeToString(serializer, value))
            }
        }
        endObject()
    }
}