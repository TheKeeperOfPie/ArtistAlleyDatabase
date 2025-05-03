package com.thekeeperofpie.artistalleydatabase.alley.database

import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_import_chunk_size_mismatch
import artistalleydatabase.modules.alley.generated.resources.alley_import_database_hash_mismatch
import artistalleydatabase.modules.alley.generated.resources.alley_import_failed
import artistalleydatabase.modules.alley.generated.resources.alley_import_failed_to_parse_chunk
import artistalleydatabase.modules.alley.generated.resources.alley_import_failed_to_read_database_hash
import artistalleydatabase.modules.alley.generated.resources.alley_import_failed_to_read_schema_version
import artistalleydatabase.modules.alley.generated.resources.alley_import_schema_version_mismatch
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleyExporter.Companion.CHARACTERS
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleyExporter.Companion.SEPARATOR
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.indexOf
import kotlinx.io.readCodePointValue
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.ExperimentalResourceApi
import artistalleydatabase.modules.alley.data.generated.resources.Res as AlleyDataRes

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
        private const val SCHEMA_VERSION = "2"

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
        val databaseHash = AlleyDataRes.readBytes("files/databaseHash.txt")
            .decodeToString()
        sink.writeString(databaseHash)
        sink.writeString(SEPARATOR)
        sink.writeString(SCHEMA_VERSION)
        sink.writeString(SEPARATOR)

        sink.writeData(
            source = importExportDao.getExportPartialArtists2023(),
            id = { it.id },
            favorite = { it.favorite },
            ignored = { it.ignored },
        )
        sink.writeData(
            source = importExportDao.getExportPartialArtists2024(),
            id = { it.id },
            favorite = { it.favorite },
            ignored = { it.ignored },
        )
        sink.writeData(
            source = importExportDao.getExportPartialArtists2025(),
            id = { it.id },
            favorite = { it.favorite },
            ignored = { it.ignored },
        )

        sink.writeData(
            source = importExportDao.getExportPartialStampRallies2023(),
            id = { it.id },
            favorite = { it.favorite },
            ignored = { it.ignored },
        )
        sink.writeData(
            source = importExportDao.getExportPartialStampRallies2024(),
            id = { it.id },
            favorite = { it.favorite },
            ignored = { it.ignored },
        )
        sink.writeData(
            source = importExportDao.getExportPartialStampRallies2025(),
            id = { it.id },
            favorite = { it.favorite },
            ignored = { it.ignored },
        )
    }

    // TODO: Remove usages of indexOf(ByteString) as it doesn't have a maximum read length
    suspend fun import(source: Source): LoadingResult<*> {
        return try {
            if (source.peek().readCodePointValue() == '{'.code) {
                val data = json.decodeFromSource<FullExport>(source)
                importFull(data)
                return LoadingResult.success(data)
            } else {
                importPartial(source)
            }
        } catch (t: Throwable) {
            LoadingResult.error<Unit>(Res.string.alley_import_failed, throwable = t)
        }
    }

    private suspend fun importPartial(source: Source): LoadingResult<*> {
        val databaseHashSize = source.indexOf('='.code.toByte())
        if (databaseHashSize < 1) {
            return LoadingResult.error<Unit>(Res.string.alley_import_failed_to_read_database_hash)
        }
        val expectedDatabaseHash = AlleyDataRes.readBytes("files/databaseHash.txt").decodeToString()
        val actualDatabaseHash = source.readString(databaseHashSize)
        if (expectedDatabaseHash != actualDatabaseHash) {
            return LoadingResult.error<Unit>(
                Res.string.alley_import_database_hash_mismatch,
                expectedDatabaseHash,
                actualDatabaseHash,
            )
        }
        source.skip(1)

        val schemaVersionSize = source.indexOf('='.code.toByte())
        if (schemaVersionSize < 1) {
            return LoadingResult.error<Unit>(Res.string.alley_import_failed_to_read_schema_version)
        }
        val actualSchemaVersion = source.readString(schemaVersionSize)
        if (SCHEMA_VERSION != actualSchemaVersion) {
            return LoadingResult.error<Unit>(
                Res.string.alley_import_schema_version_mismatch,
                SCHEMA_VERSION,
                actualSchemaVersion,
            )
        }
        source.skip(1)

        importExportDao.database().transaction {
            readData(
                source = source,
                databaseValues = importExportDao.getExportPartialArtists2023(),
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
                databaseValues = importExportDao.getExportPartialArtists2024(),
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
                databaseValues = importExportDao.getExportPartialArtists2025(),
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
                databaseValues = importExportDao.getExportPartialStampRallies2023(),
                id = { it.id },
                insert = importExportDao::importStampRally,
            )

            readData(
                source = source,
                databaseValues = importExportDao.getExportPartialStampRallies2024(),
                id = { it.id },
                insert = importExportDao::importStampRally,
            )

            readData(
                source = source,
                databaseValues = importExportDao.getExportPartialStampRallies2025(),
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
                return LoadingResult.error(Res.string.alley_import_failed_to_parse_chunk)
            }

            val result = source.readString(index)
            source.skip(1)
            result
        }

        if (expectedChunks != actualChunks.size) {
            return LoadingResult.error(
                Res.string.alley_import_chunk_size_mismatch,
                expectedChunks,
                actualChunks.size
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

    suspend fun exportFull(sink: Sink) {
        val artists: Map<DataYear?, Map<String, FullExport.ArtistData>> = mapOf(
            DataYear.YEAR_2023 to importExportDao.getExportFullArtists2023()
                .associate {
                    it.id to FullExport.ArtistData(
                        it.favorite == true,
                        it.ignored == true,
                        it.notes
                    )
                },
            DataYear.YEAR_2024 to importExportDao.getExportFullArtists2024()
                .associate {
                    it.id to FullExport.ArtistData(
                        it.favorite == true,
                        it.ignored == true,
                        it.notes
                    )
                },
            DataYear.YEAR_2025 to importExportDao.getExportFullArtists2025()
                .associate {
                    it.id to FullExport.ArtistData(
                        it.favorite == true,
                        it.ignored == true,
                        it.notes
                    )
                },
        )

        val rallies = mutableMapOf<String, FullExport.RallyData>()
        rallies += importExportDao.getExportFullStampRallies2023()
            .associate {
                it.id to FullExport.RallyData(it.favorite == true, it.ignored == true, it.notes)
            }
        rallies += importExportDao.getExportFullStampRallies2024()
            .map {
                it.id to FullExport.RallyData(it.favorite == true, it.ignored == true, it.notes)
            }
        rallies += importExportDao.getExportFullStampRallies2025()
            .map {
                it.id to FullExport.RallyData(it.favorite == true, it.ignored == true, it.notes)
            }

        json.encodeToSink(FullExport(artists, rallies), sink)
    }

    private suspend fun importFull(data: FullExport) {
        data.artists.forEach { (year, artists) ->
            if (year != null) {
                artists.forEach { (id, data) ->
                    importExportDao.importArtist(id, year, data.favorite, data.ignored, data.notes)
                }
            }
        }
        data.rallies.forEach { (id, data) ->
            importExportDao.importStampRally(id, data.favorite, data.ignored, data.notes)
        }
    }

    @Serializable
    private class FullExport(
        val artists: Map<DataYear?, Map<String, ArtistData>>,
        val rallies: Map<String, RallyData>,
    ) {
        @Serializable
        data class ArtistData(
            @SerialName("f")
            val favorite: Boolean = false,
            @SerialName("i")
            val ignored: Boolean = false,
            @SerialName("n")
            val notes: String? = null,
        )

        @Serializable
        data class RallyData(
            @SerialName("f")
            val favorite: Boolean = false,
            @SerialName("i")
            val ignored: Boolean = false,
            @SerialName("n")
            val notes: String? = null,
        )
    }
}
