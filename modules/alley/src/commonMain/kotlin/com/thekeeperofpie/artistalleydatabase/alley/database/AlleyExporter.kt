package com.thekeeperofpie.artistalleydatabase.alley.database

import artistalleydatabase.modules.alley.data.generated.resources.Res
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleyExporter.Companion.CHARACTERS
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleyExporter.Companion.SEPARATOR
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.indexOf
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.ExperimentalSerializationApi
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
) {
    companion object {
        private const val SCHEMA_VERSION = "1"

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

    suspend fun import(source: Source): LoadingResult<*> {
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
}
