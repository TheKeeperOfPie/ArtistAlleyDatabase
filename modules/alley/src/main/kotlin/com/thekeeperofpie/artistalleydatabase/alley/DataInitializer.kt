package com.thekeeperofpie.artistalleydatabase.alley

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyArtistConnection
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.launch
import org.apache.commons.csv.CSVFormat
import javax.inject.Inject

class DataInitializer @Inject constructor(
    val scopedApplication: ScopedApplication,
    val artistEntryDao: ArtistEntryDao,
    val stampRallyEntryDao: StampRallyEntryDao,
    val settings: ArtistAlleySettings,
) {
    companion object {
        private const val ARTISTS_CSV_NAME = "artists.csv"
        private const val STAMP_RALLIES_CSV_NAME = "rallies.csv"
    }

    fun init() {
        scopedApplication.scope.launch(CustomDispatchers.IO) {
            val application = scopedApplication.app
            val artistsCsvSize = parseSize(application, ARTISTS_CSV_NAME)
            if (settings.lastKnownArtistsCsvSize != artistsCsvSize
                || artistEntryDao.getEntriesSize() == 0
            ) {
                parseArtists()
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
        scopedApplication.app.assets.open(ARTISTS_CSV_NAME).use { input ->
            input.reader().use { reader ->
                CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader)
                    .asSequence()
                    .mapNotNull {
                        // Booth,Artist,Summary,Links,Store,Catalog / table,
                        // Series - Inferred,Merch - Inferred,Notes,Series - Confirmed,
                        // Merch - Confirmed,Catalog images
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

                        val commaRegex = Regex(",\\s?")
                        val seriesInferred = it["Series - Inferred"].split(commaRegex)
                            .filter(String::isNotBlank)
                        val merchInferred = it["Merch - Inferred"].split(commaRegex)
                            .filter(String::isNotBlank)

                        val seriesConfirmed = it["Series - Confirmed"].split(commaRegex)
                            .filter(String::isNotBlank)
                        val merchConfirmed = it["Merch - Confirmed"].split(commaRegex)
                            .filter(String::isNotBlank)

                        ArtistEntry(
                            id = booth,
                            booth = booth,
                            name = artist,
                            summary = summary,
                            links = links,
                            storeLinks = storeLinks,
                            catalogLinks = catalogLinks,
                            seriesInferredSerialized = seriesInferred,
                            seriesInferredSearchable = seriesInferred,
                            seriesConfirmedSerialized = seriesConfirmed,
                            seriesConfirmedSearchable = seriesConfirmed,
                            merchInferred = merchInferred,
                            merchConfirmed = merchConfirmed,
                        )
                    }
                    .chunked(20)
                    .forEach { artistEntryDao.insertUpdatedEntries(it) }
            }
        }
    }

    private suspend fun parseStampRallies() {
        stampRallyEntryDao.clearEntries()
        stampRallyEntryDao.clearConnections()
        scopedApplication.app.assets.open(STAMP_RALLIES_CSV_NAME).use { input ->
            input.reader().use { reader ->
                CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader)
                    .asSequence()
                    .mapNotNull {
                        // Theme,Link,Tables,Minimum per table,Notes,Images
                        val theme = it["Theme"]
                        val link = it["Link"]
                        val tables = it["Tables"].split("\n").filter(String::isNotBlank)
                        val hostTable = tables.firstOrNull { it.contains("-") }
                            ?.substringBefore("-")
                            ?.trim() ?: return@mapNotNull null
                        val minimumPerTable = it["Minimum per table"]

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
                            minimumPerTable = minimumPerTable,
                        ) to connections
                    }
                    .chunked(20)
                    .forEach { stampRallyEntryDao.insertUpdatedEntries(it) }
            }
        }
    }

    private fun parseSize(application: Application, csvName: String) = try {
        application.assets.open(csvName).use { it.available().toLong() }
    } catch (ignored: Throwable) {
        -1
    }
}
