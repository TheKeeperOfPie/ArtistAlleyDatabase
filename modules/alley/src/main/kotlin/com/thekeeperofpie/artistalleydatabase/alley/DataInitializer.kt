package com.thekeeperofpie.artistalleydatabase.alley

import android.app.Application
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
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.launch
import org.apache.commons.csv.CSVFormat
import java.io.Reader
import javax.inject.Inject

class DataInitializer @Inject constructor(
    private val scopedApplication: ScopedApplication,
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
        scopedApplication.scope.launch(CustomDispatchers.IO) {
            val application = scopedApplication.app
            val artistsCsvSize = parseSize(application, ARTISTS_CSV_NAME)
            if (settings.lastKnownArtistsCsvSize != artistsCsvSize
                || artistEntryDao.getEntriesSize() == 0
            ) {
                parseArtists()
                parseTags()
                settings.lastKnownArtistsCsvSize = artistsCsvSize
            }
            val ralliesCsvSize = parseSize(application, STAMP_RALLIES_CSV_NAME)
            if (settings.lastKnownStampRalliesCsvSize != ralliesCsvSize
                || artistEntryDao.getEntriesSize() == 0
            ) {
                parseStampRallies()
                settings.lastKnownStampRalliesCsvSize = ralliesCsvSize
            }
        }
    }

    private suspend fun parseArtists() {
        artistEntryDao.clearSeriesConnections()
        artistEntryDao.clearMerchConnections()
        scopedApplication.app.assets.open(ARTISTS_CSV_NAME).use { input ->
            input.reader().use { reader ->
                var counter = 1
                csvReader(reader)
                    .mapNotNull {
                        // Booth,Artist,Summary,Links,Store,Catalog / table,
                        // Series - Inferred,Merch - Inferred,Notes,Series - Confirmed,
                        // Merch - Confirmed,Drive,Catalog images
                        val booth = it["Booth"]
                        val artist = it["Artist"]
                        val summary = it["Summary"]

                        val newLineRegex = Regex("\n\\s?")
                        val links = it["Links"].split(newLineRegex)
                            .filter(String::isNotBlank)
                        val storeLinks = it["Store"].split(newLineRegex)
                            .filter(String::isNotBlank)
                        val catalogLinks = it["Catalog / table"].split(newLineRegex)
                            .filter(String::isNotBlank)
                        val driveLink = it["Drive"]

                        val commaRegex = Regex(",\\s?")
                        val seriesInferred = it["Series - Inferred"].split(commaRegex)
                            .filter(String::isNotBlank)
                        val merchInferred = it["Merch - Inferred"].split(commaRegex)
                            .filter(String::isNotBlank)

                        val seriesConfirmed = it["Series - Confirmed"].split(commaRegex)
                            .filter(String::isNotBlank)
                        val merchConfirmed = it["Merch - Confirmed"].split(commaRegex)
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
                            seriesInferredSerialized = seriesInferred,
                            seriesInferredSearchable = seriesInferred,
                            seriesConfirmedSerialized = seriesConfirmed,
                            seriesConfirmedSearchable = seriesConfirmed,
                            merchInferred = merchInferred,
                            merchConfirmed = merchConfirmed,
                            notes = notes,
                            counter = counter++
                        )

                        val seriesConnections = (seriesInferred + seriesConfirmed)
                            .map { ArtistSeriesConnection(artistId = booth, seriesId = it) }
                            .distinct()
                        val merchConnections = (merchInferred + merchConfirmed)
                            .map { ArtistMerchConnection(artistId = booth, merchId = it) }
                            .distinct()

                        Triple(artistEntry, seriesConnections, merchConnections)
                    }
                    .chunked(20)
                    .forEach { artistEntryDao.insertUpdatedEntries(it) }
            }
        }
    }

    private suspend fun parseTags() {
        tagEntryDao.clearSeries()
        scopedApplication.app.assets.open(SERIES_CSV_NAME).use { input ->
            input.reader().use { reader ->
                csvReader(reader)
                    .mapNotNull {
                        // Series, Notes
                        val name = it["Series"]
                        val notes = it["Notes"]
                        SeriesEntry(name = name, notes = notes)
                    }
                    .chunked(20)
                    .forEach { tagEntryDao.insertSeries(it) }
            }
        }
        tagEntryDao.clearMerch()
        scopedApplication.app.assets.open(MERCH_CSV_NAME).use { input ->
            input.reader().use { reader ->
                csvReader(reader)
                    .mapNotNull {
                        // Merch, Notes
                        val name = it["Merch"]
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
        scopedApplication.app.assets.open(STAMP_RALLIES_CSV_NAME).use { input ->
            input.reader().use { reader ->
                var counter = 1
                csvReader(reader)
                    .mapNotNull {
                        // Theme,Link,Tables,Minimum per table,Notes,Images
                        val theme = it["Theme"]
                        val links = it["Link"].split("\n")
                            .filter(String::isNotBlank)
                        val tables = it["Tables"].split("\n")
                            .filter(String::isNotBlank)
                        val hostTable = tables.firstOrNull { it.contains("-") }
                            ?.substringBefore("-")
                            ?.trim() ?: return@mapNotNull null
                        val minimumPerTable = it["Minimum per table"]
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
                            minimumPerTable = minimumPerTable,
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

    private fun csvReader(reader: Reader) =
        CSVFormat.RFC4180.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .build()
            .parse(reader)
            .asSequence()

    private fun parseSize(application: Application, csvName: String) = try {
        application.assets.open(csvName).use { it.available().toLong() }
    } catch (ignored: Throwable) {
        -1
    }
}
