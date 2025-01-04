package com.thekeeperofpie.artistalleydatabase.alley

import artistalleydatabase.modules.alley.generated.resources.Res
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyArtistConnection
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.tags.ArtistMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.tags.ArtistSeriesConnection
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.launch
import kotlinx.io.InternalIoApi
import kotlinx.io.buffered
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Inject
class DataInitializer(
    private val appFileSystem: AppFileSystem,
    private val appScope: ApplicationScope,
    private val artistEntryDao: ArtistEntryDao,
    private val stampRallyEntryDao: StampRallyEntryDao,
    private val tagEntryDao: TagEntryDao,
    private val settings: ArtistAlleySettings,
) {
    companion object {
        private const val ARTISTS_CSV_NAME = "artists.csv"
        private const val STAMP_RALLIES_CSV_NAME = "rallies.csv"
        private const val SERIES_CSV_NAME = "series.csv"
        private const val MERCH_CSV_NAME = "merch.csv"
    }

    fun init() {
        appScope.launch(CustomDispatchers.IO) {
            val artistsCsvSize = parseSize(ARTISTS_CSV_NAME)
            if (settings.lastKnownArtistsCsvSize.value != artistsCsvSize
                || artistEntryDao.getEntriesSize() == 0
            ) {
                parseArtists()
                parseTags()
                settings.lastKnownArtistsCsvSize.value = artistsCsvSize
            }
            val ralliesCsvSize = parseSize(STAMP_RALLIES_CSV_NAME)
            if (settings.lastKnownStampRalliesCsvSize.value != ralliesCsvSize
                || artistEntryDao.getEntriesSize() == 0
            ) {
                parseStampRallies()
                settings.lastKnownStampRalliesCsvSize.value = ralliesCsvSize
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun parseArtists() {
        artistEntryDao.clearSeriesConnections()
        artistEntryDao.clearMerchConnections()
        appFileSystem.openUriSource(Uri.parse(Res.getUri("files/$ARTISTS_CSV_NAME")))!!
            .use {
                it.buffered().use {
                    var counter = 1
                    CsvReader.read(it)
                        .map {
                            // Booth,Artist,Summary,Links,Store,Catalog / table,
                            // Series - Inferred,Merch - Inferred,Notes,Series - Confirmed,
                            // Merch - Confirmed,Drive,Catalog images
                            val booth = it["Booth"]!!
                            val artist = it["Artist"]!!
                            val summary = it["Summary"]

                            val newLineRegex = Regex("\n\\s?")
                            val links = it["Links"]!!.split(newLineRegex)
                                .filter(String::isNotBlank)
                            val storeLinks = it["Store"]!!.split(newLineRegex)
                                .filter(String::isNotBlank)
                            val catalogLinks = it["Catalog / table"]!!.split(newLineRegex)
                                .filter(String::isNotBlank)
                            val driveLink = it["Drive"]

                            val commaRegex = Regex(",\\s?")
                            val seriesInferred = it["Series - Inferred"]!!.split(commaRegex)
                                .filter(String::isNotBlank)
                            val merchInferred = it["Merch - Inferred"]!!.split(commaRegex)
                                .filter(String::isNotBlank)

                            val seriesConfirmed = it["Series - Confirmed"]!!.split(commaRegex)
                                .filter(String::isNotBlank)
                            val merchConfirmed = it["Merch - Confirmed"]!!.split(commaRegex)
                                .filter(String::isNotBlank)

                            val notes = it["Notes"]

                            val artistEntry = ArtistEntry(
                                id = booth,
                                booth = booth,
                                name = artist,
                                summary = summary,
                                links = links,
                                storeLinks = storeLinks,
                                catalogLinks = catalogLinks,
                                driveLink = driveLink,
                                seriesInferred = seriesInferred,
                                seriesConfirmed = seriesConfirmed,
                                merchInferred = merchInferred,
                                merchConfirmed = merchConfirmed,
                                notes = notes,
                                counter = counter++
                            )

                            val seriesConnectionsInferred =
                                (seriesInferred - seriesConfirmed.toSet())
                                    .map {
                                        ArtistSeriesConnection(
                                            artistId = booth,
                                            seriesId = it,
                                            confirmed = false
                                        )
                                    }
                            val seriesConnectionsConfirmed = seriesConfirmed
                                .map {
                                    ArtistSeriesConnection(
                                        artistId = booth,
                                        seriesId = it,
                                        confirmed = true
                                    )
                                }
                            val seriesConnections =
                                seriesConnectionsInferred + seriesConnectionsConfirmed

                            val merchConnectionsInferred = (merchInferred - merchConfirmed.toSet())
                                .map {
                                    ArtistMerchConnection(
                                        artistId = booth,
                                        merchId = it,
                                        confirmed = false
                                    )
                                }
                            val merchConnectionsConfirmed = merchConfirmed
                                .map {
                                    ArtistMerchConnection(
                                        artistId = booth,
                                        merchId = it,
                                        confirmed = true
                                    )
                                }
                            val merchConnections =
                                merchConnectionsInferred + merchConnectionsConfirmed

                            Triple(artistEntry, seriesConnections, merchConnections)
                        }
                        .chunked(20)
                        .forEach { artistEntryDao.insertUpdatedEntries(it) }
                }
            }
    }

    private suspend fun parseTags() {
        tagEntryDao.clearSeries()
        appFileSystem.openUriSource(Uri.parse(Res.getUri("files/$SERIES_CSV_NAME")))!!
            .use { input ->
                input.buffered().use {
                    CsvReader.read(it)
                        .map {
                            // Series, Notes
                            val name = it["Series"]!!
                            val notes = it["Notes"]
                            SeriesEntry(name = name, notes = notes)
                        }
                        .chunked(20)
                        .forEach { tagEntryDao.insertSeries(it) }
                }
            }
        tagEntryDao.clearMerch()
        appFileSystem.openUriSource(Uri.parse(Res.getUri("files/$MERCH_CSV_NAME")))!!.use { input ->
            input.buffered().use {
                CsvReader.read(it)
                    .map {
                        // Merch, Notes
                        val name = it["Merch"]!!
                        val notes = it["Notes"]
                        MerchEntry(name = name, notes = notes)
                    }
                    .chunked(20)
                    .forEach { tagEntryDao.insertMerch(it) }
            }
        }
    }

    private suspend fun parseStampRallies() {
        stampRallyEntryDao.clearConnections()
        val allIds = mutableListOf<String>()
        appFileSystem.openUriSource(Uri.parse(Res.getUri("files/$STAMP_RALLIES_CSV_NAME")))!!.use {
            it.buffered().use {
                var counter = 1
                CsvReader.read(it)
                    .mapNotNull {
                        // Theme,Link,Tables,Table Min, Total, Notes,Images
                        val theme = it["Theme"]!!
                        val links = it["Link"]!!.split("\n")
                            .filter(String::isNotBlank)
                        val tables = it["Tables"]!!.split("\n")
                            .filter(String::isNotBlank)
                        val hostTable = tables.firstOrNull { it.contains("-") }
                            ?.substringBefore("-")
                            ?.trim() ?: return@mapNotNull null
                        val tableMin = it["Table Min"]!!.let {
                            when {
                                it.equals("Free", ignoreCase = true) -> 0
                                it.equals("Any", ignoreCase = true) -> 1
                                else -> it.removePrefix("$").toIntOrNull()
                            }
                        }
                        val totalCost = it["Total"]?.removePrefix("$")?.toIntOrNull()
                        val prizeLimit = it["Prize Limit"]!!.toIntOrNull()
                        val notes = it["Notes"]

                        val stampRallyId = "$hostTable-$theme"
                        val connections = tables
                            .filter { it.contains("-") }
                            .map { it.substringBefore("-") }
                            .map(String::trim)
                            .map { StampRallyArtistConnection(stampRallyId, it) }

                        StampRallyEntry(
                            id = stampRallyId,
                            fandom = theme,
                            tables = tables,
                            hostTable = hostTable,
                            links = links,
                            tableMin = tableMin,
                            totalCost = if (tableMin == 0) 0 else totalCost,
                            prizeLimit = prizeLimit,
                            notes = notes,
                            counter = counter++
                        ) to connections
                    }
                    .chunked(20)
                    .forEach {
                        allIds += it.map { it.first.id }
                        stampRallyEntryDao.insertUpdatedEntries(it)
                    }
            }
        }
        stampRallyEntryDao.retainIds(allIds)
    }

    @OptIn(InternalIoApi::class)
    private fun parseSize(csvName: String) = try {
        appFileSystem.openUriSource(Uri.parse(Res.getUri("files/$csvName")))!!
            .use { it.buffer.size }
    } catch (_: Throwable) {
        -1L
    }
}
