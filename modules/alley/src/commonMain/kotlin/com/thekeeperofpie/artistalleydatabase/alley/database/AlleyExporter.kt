package com.thekeeperofpie.artistalleydatabase.alley.database

import artistalleydatabase.modules.alley.data.generated.resources.Res
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleyExporter.Companion.CHARACTERS
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleyExporter.Companion.SEPARATOR
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistNotes
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyNotes
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.decodeSequenceIgnoreEndOfFile
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.indexOf
import kotlinx.io.readCodePointValue
import kotlinx.io.readLine
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.encodeToSink
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Export data format:
 * - Database version followed by [SEPARATOR]
 * - Schema version by [SEPARATOR]
 * - Artist data chunks (repeated for each DataYear)
 *     - Favorite
 *     - Ignored
 * - Stamp rally data chunks (repeated for each year)
 *     - Favorite
 *     - Ignored
 *
 * Chunks:
 * Take all artists in table, sorted by ID, and split them into chunks of [CHARACTERS] size. Then
 * encode each index in each chunk with the character at the corresponding index in [CHARACTERS].
 * Each chunk is separated by [SEPARATOR].
 */
@OptIn(
    ExperimentalResourceApi::class,
    ExperimentalUnsignedTypes::class,
    ExperimentalSerializationApi::class,
)
@Inject
class AlleyExporter(
    private val importExportDao: ImportExportDao,
    private val json: Json = Json { encodeDefaults = false },
) {
    companion object {
        private const val SCHEMA_VERSION = "1"
        private const val FULL_CHUNK_SIZE = 10L

        private val CHARACTERS = listOf(
            '0'.rangeTo('9'),
            'a'.rangeTo('z'),
            'A'.rangeTo('Z'),
            listOf(
                '!', '#', '$', '%', '&', '(', ')', '*', '+', '-', ';', '<', '>', '?', '@', '^', '_',
                '`', '{', '|', '}', '~', '[', ']'
            )
        ).flatten()
            .sorted()
            .withIndex()
            .associate { it.index to it.value.toString() }
        private val CHARACTERS_REVERSED = CHARACTERS.entries.associate { it.value to it.key }

        const val SEPARATOR = "="
    }

    suspend fun exportPartial(sink: Sink) {
        val databaseHash = Res.readBytes("files/databaseHash.txt")
            .decodeToString()
        sink.writeString(databaseHash)
        sink.writeString(SEPARATOR)
        sink.writeString(SCHEMA_VERSION)
        sink.writeString(SEPARATOR)

        sink.writeData(
            source = importExportDao.getExportDataArtists2023(),
            id = { it.id },
            favorite = { it.favorite },
            ignored = { it.ignored },
        )
        sink.writeData(
            source = importExportDao.getExportDataArtists2024(),
            id = { it.id },
            favorite = { it.favorite },
            ignored = { it.ignored },
        )
        sink.writeData(
            source = importExportDao.getExportDataArtists2025(),
            id = { it.id },
            favorite = { it.favorite },
            ignored = { it.ignored },
        )

        sink.writeData(
            source = importExportDao.getExportDataStampRallies2023(),
            id = { it.id },
            favorite = { it.favorite },
            ignored = { it.ignored },
        )
        sink.writeData(
            source = importExportDao.getExportDataStampRallies2024(),
            id = { it.id },
            favorite = { it.favorite },
            ignored = { it.ignored },
        )
        sink.writeData(
            source = importExportDao.getExportDataStampRallies2025(),
            id = { it.id },
            favorite = { it.favorite },
            ignored = { it.ignored },
        )
    }

    suspend fun exportFull(sink: Sink) {
        val partial = Buffer().use {
            exportPartial(it)
            it.readString()
        }
        sink.writeString("{\"partial\":\"$partial\",\n")

        importExportDao.database().transaction {
            val limit = FULL_CHUNK_SIZE
            DataYear.entries.sortedBy { it.year }
                .forEach {
                    var writtenFirst = false
                    sink.writeString("\"${it.serializedName}\":[")
                    var offset = 0L
                    var done = false
                    while (!done) {
                        val list = importExportDao.getExportArtistNotes(it, limit, offset)
                        list.forEach {
                            if (writtenFirst) {
                                sink.writeString(",\n")
                            } else {
                                writtenFirst = true
                            }
                            json.encodeToSink(ArtistNotesWrapper(it), sink)
                        }
                        offset += limit
                        done = list.size < limit
                    }
                    sink.writeString("],\n")
                }

            var writtenFirst = false
            sink.writeString("\"stampRallies\":[")
            var offset = 0L
            var done = false
            while (!done) {
                val list = importExportDao.getExportStampRallyNotes(limit, offset)
                list.forEach {
                    if (writtenFirst) {
                        sink.writeString(",\n")
                    } else {
                        writtenFirst = true
                    }
                    json.encodeToSink(StampRallyNotesWrapper(it), sink)
                }
                offset += limit
                done = list.size < limit
            }
            sink.writeString("]")
        }

        sink.writeString("}")
    }

    // TODO: Remove usages of indexOf(ByteString) as it doesn't have a maximum read length
    suspend fun import(source: Source): LoadingResult<*> {
        return if (source.peek().readCodePointValue() == '{'.code.toInt()) {
            val partialKeyTarget = "partial\":".encodeToByteString()
            val partialKeyIndex = source.indexOf(partialKeyTarget)
            if (partialKeyIndex < 0) {
                return LoadingResult.error<Unit>("Failed to read user data")
            }
            source.skip(partialKeyIndex + partialKeyTarget.size)
            val partialStartIndex = source.indexOf('"'.code.toByte())
            if (partialStartIndex < 0) {
                return LoadingResult.error<Unit>("Failed to read user data")
            }
            source.skip(1)
            val partialEndIndex = source.indexOf('"'.code.toByte())
            if (partialEndIndex < 0) {
                return LoadingResult.error<Unit>("Failed to read user data")
            }
            Buffer().use {
                source.readAtMostTo(it, partialEndIndex)
                importPartial(it)
            }

            DataYear.entries
                .sortedBy { it.year }
                .forEach { year ->
                    val result = importTarget(source, year.serializedName) {
                        json.decodeSequenceIgnoreEndOfFile<ArtistNotesWrapper>(it)
                            .chunked(10)
                            .forEach {
                                it.forEach {
                                    if (it.notes != null) {
                                        importExportDao.importArtistNotes(
                                            artistId = it.artistId,
                                            dataYear = year,
                                            notes = it.notes,
                                        )
                                    }
                                }
                            }
                    }
                    if (!result.success) return result
                }

            val result = importTarget(source, "stampRallies") {
                json.decodeSequenceIgnoreEndOfFile<StampRallyNotesWrapper>(it)
                    .chunked(10)
                    .forEach {
                        it.forEach {
                            if (it.notes != null) {
                                importExportDao.importStampRallyNotes(it.stampRallyId, it.notes)
                            }
                        }
                    }
            }
            // TODO: Exhaust the rest of the JSON
            return result
        } else {
            importPartial(source)
        }
    }

    private suspend fun importTarget(
        originalSource: Source,
        targetName: String,
        decode: suspend (Source) -> Unit,
    ): LoadingResult<*> {
        val truncatedSource = object : RawSource {
            private var exhausted = false
            override fun readAtMostTo(
                sink: Buffer,
                byteCount: Long,
            ): Long {
                if (exhausted) return -1
                val index = originalSource.indexOf(byte = ']'.code.toByte(), endIndex = byteCount)
                return if (index >= 0) {
                    exhausted = true
                    originalSource.readAtMostTo(sink, index + 1)
                } else {
                    originalSource.readAtMostTo(sink, byteCount)
                }
            }

            override fun close() {
                originalSource.close()
            }
        }.buffered()
        val target = "\"$targetName\":"
        val targetIndex = truncatedSource.indexOf(target.encodeToByteString())
        if (targetIndex < 0) {
            return LoadingResult.error<Unit>("Failed to read $targetName, ${truncatedSource.peek().readLine()}")
        }
        truncatedSource.skip(targetIndex)

        val arrayIndex = truncatedSource.indexOf("[".encodeToByteString())
        if (arrayIndex < 0) {
            return LoadingResult.error<Unit>("Failed to read array for $targetName, ${truncatedSource.peek().readLine()}")
        }
        truncatedSource.skip(arrayIndex)

        decode(truncatedSource)
        return LoadingResult.success(Unit)
    }

    private suspend fun importPartial(source: Source): LoadingResult<*> {
        val databaseHashSize = source.indexOf('='.code.toByte())
        if (databaseHashSize < 1) return LoadingResult.error<Unit>("Failed to read database hash")
        val expectedDatabaseHash = Res.readBytes("files/databaseHash.txt").decodeToString()
        val actualDatabaseHash = source.readString(databaseHashSize)
        if (expectedDatabaseHash != actualDatabaseHash) {
            return LoadingResult.error<Unit>(
                "Database hash did not match, " +
                        "expected $expectedDatabaseHash but got $actualDatabaseHash"
            )
        }
        source.skip(1)

        val schemaVersionSize = source.indexOf('='.code.toByte())
        if (schemaVersionSize < 1) return LoadingResult.error<Unit>("Failed to read schema version")
        val actualSchemaVersion = source.readString(schemaVersionSize)
        if (SCHEMA_VERSION != actualSchemaVersion) {
            return LoadingResult.error<Unit>(
                "Schema version did not match, " +
                        "expected $SCHEMA_VERSION but got $actualSchemaVersion"
            )
        }
        source.skip(1)

        importExportDao.database().transaction {
            readData(
                source = source,
                databaseValues = importExportDao.getExportDataArtists2023(),
                id = { it.id },
                insert = { artistId, favorite, ignored ->
                    importExportDao.importArtist(
                        artistId = artistId,
                        dataYear = DataYear.YEAR_2023,
                        favorite = favorite,
                        ignored = ignored,
                    )
                },
            )

            readData(
                source = source,
                databaseValues = importExportDao.getExportDataArtists2024(),
                id = { it.id },
                insert = { artistId, favorite, ignored ->
                    importExportDao.importArtist(
                        artistId = artistId,
                        dataYear = DataYear.YEAR_2024,
                        favorite = favorite,
                        ignored = ignored,
                    )
                },
            )

            readData(
                source = source,
                databaseValues = importExportDao.getExportDataArtists2025(),
                id = { it.id },
                insert = { artistId, favorite, ignored ->
                    importExportDao.importArtist(
                        artistId = artistId,
                        dataYear = DataYear.YEAR_2025,
                        favorite = favorite,
                        ignored = ignored,
                    )
                },
            )

            readData(
                source = source,
                databaseValues = importExportDao.getExportDataStampRallies2023(),
                id = { it.id },
                insert = importExportDao::importStampRally,
            )

            readData(
                source = source,
                databaseValues = importExportDao.getExportDataStampRallies2024(),
                id = { it.id },
                insert = importExportDao::importStampRally,
            )

            readData(
                source = source,
                databaseValues = importExportDao.getExportDataStampRallies2025(),
                id = { it.id },
                insert = importExportDao::importStampRally,
            )
        }

        return LoadingResult.success(Unit)
    }

    private suspend fun <T> readData(
        source: Source,
        databaseValues: List<T>,
        id: (T) -> String,
        insert: suspend (id: String, favorite: Boolean, ignored: Boolean) -> Unit,
    ): LoadingResult<*> {
        val values = databaseValues.sortedBy(id)

        val favoriteChunks = readChunks(source, values)
        if (!favoriteChunks.success) return favoriteChunks
        val favorites = mutableSetOf<String>()
        values.chunked(CHARACTERS.size).zip(favoriteChunks.result.orEmpty())
            .forEach { (artistChunk, favoriteChunk) ->
                favoriteChunk.forEach {
                    val index = CHARACTERS_REVERSED[it.toString()]!!
                    favorites += id(artistChunk[index])
                }
            }

        val ignoredChunks = readChunks(source, values)
        if (!ignoredChunks.success) return ignoredChunks
        val ignored = mutableSetOf<String>()
        values.chunked(CHARACTERS.size).zip(ignoredChunks.result.orEmpty())
            .forEach { (artistChunk, ignoredChunk) ->
                ignoredChunk.forEach {
                    val index = CHARACTERS_REVERSED[it.toString()]!!
                    ignored += id(artistChunk[index])
                }
            }

        values.forEach {
            val artistId = id(it)
            insert(artistId, favorites.contains(artistId), ignored.contains(artistId))
        }

        return LoadingResult.success(Unit)
    }

    private fun readChunks(source: Source, values: List<*>): LoadingResult<List<String>> {
        val expectedChunks = (values.size + CHARACTERS.size - 1) / CHARACTERS.size
        val actualChunks = (0 until expectedChunks).map {
            val index = source.indexOf('='.code.toByte())
            if (index == 0L) {
                source.skip(1)
                return@map ""
            } else if (index < 0) {
                return LoadingResult.error("Failed to parse chunk")
            }

            val result = source.readString(index)
            source.skip(1)
            result
        }

        if (expectedChunks != actualChunks.size) {
            return LoadingResult.error(
                "Chunk size did not match, expected $expectedChunks " +
                        "but got ${actualChunks.size}"
            )
        }

        return LoadingResult.success(actualChunks)
    }

    private fun <T> Sink.writeData(
        source: List<T>,
        id: (T) -> String,
        favorite: (T) -> Boolean?,
        ignored: (T) -> Boolean?,
    ) {
        val data = source.sortedBy(id)
        data.chunked(CHARACTERS.size)
            .forEach {
                it.forEachIndexed { index, artist ->
                    if (favorite(artist) == true) {
                        writeString(CHARACTERS[index]!!)
                    }
                }
                writeString(SEPARATOR)
            }
        data.chunked(CHARACTERS.size)
            .forEach {
                it.forEachIndexed { index, artist ->
                    if (ignored(artist) == true) {
                        writeString(CHARACTERS[index]!!)
                    }
                }
                writeString(SEPARATOR)
            }
    }

    /** Wraps for Serialization support. */
    @Serializable
    private class ArtistNotesWrapper(
        val artistId: String,
        val notes: String? = null,
    ) {
        constructor(entry: ArtistNotes) : this(
            artistId = entry.artistId,
            notes = entry.notes,
        )

        fun toDatabaseEntry(dataYear: DataYear) = ArtistNotes(
            artistId = artistId,
            dataYear = dataYear,
            notes = notes,
        )
    }

    /** Wraps for Serialization support. */
    @Serializable
    private class StampRallyNotesWrapper(
        val stampRallyId: String,
        val notes: String? = null,
    ) {
        constructor(entry: StampRallyNotes) : this(
            stampRallyId = entry.stampRallyId,
            notes = entry.notes,
        )

        fun toDatabaseEntry() = StampRallyNotes(
            stampRallyId = stampRallyId,
            notes = notes,
        )
    }
}
