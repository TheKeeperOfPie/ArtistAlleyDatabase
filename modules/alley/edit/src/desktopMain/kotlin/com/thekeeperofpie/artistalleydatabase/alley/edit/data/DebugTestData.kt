package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

object DebugTestData {

    private var initialized = false

    suspend fun initialize(
        editDatabase: AlleyEditRemoteDatabase,
        formDatabase: AlleyFormRemoteDatabase,
    ) {
        if (initialized) return
        initialized = true
        val artist = initializeTestArtist(editDatabase)
        val artistAfter = artist.copy(
            name = artist.name + " - edited",
            summary = "New description",
            seriesInferred = artist.seriesInferred.drop(1) + "SeriesD",
            merchConfirmed = artist.merchConfirmed.drop(1) + "Photocards",
        )

        val stampRally = initializeTestStampRally(editDatabase)
        val stampRallyAfter = stampRally.copy(
            fandom = stampRally.fandom + " - edited",
            tables = listOf("C38", "C39", "C41"),
            prizeLimit = 25,
            series = stampRally.series.drop(1) + "SeriesD",
            merch = stampRally.merch.drop(1) + "Photocards",
        )

        editDatabase
            .generateFormLink(
                dataYear = artist.year,
                artistId = Uuid.parse(artist.id),
                forceRegenerate = false,
            )
            ?.substringAfter("${AlleyCryptography.ACCESS_KEY_PARAM}=")
            ?.let(ArtistFormAccessKey::setKey)
        formDatabase.saveArtist(
            artist.year,
            beforeArtist = artist,
            afterArtist = artistAfter,
            beforeStampRallies = listOf(stampRally),
            afterStampRallies = listOf(stampRallyAfter),
            formNotes = "Some test artist form notes",
        )
    }

    private suspend fun initializeTestArtist(database: AlleyEditRemoteDatabase): ArtistDatabaseEntry.Impl {
        // Seed some initial data to make it easier to test out features locally
        val artistUpdates = listOf<(ArtistDatabaseEntry.Impl) -> ArtistDatabaseEntry.Impl>(
            { it.copy(name = "First Last", lastEditor = "firstlast@example.org") },
            { it.copy(summary = "Description", lastEditor = "fakeemail@example.com") },
            {
                it.copy(
                    socialLinks = listOf(
                        "https://example.com/social",
                        "https://example.com/profile",
                    ),
                    notes = "Test notes",
                    editorNotes = "Added links",
                )
            },
            {
                it.copy(
                    storeLinks = listOf("https://example.org/store"),
                    portfolioLinks = listOf("https://example.net/portfolio"),
                    notes = "",
                )
            },
            {
                it.copy(
                    commissions = listOf("On-site", "Online"),
                    notes = "More test notes",
                    editorNotes = "Added commissions",
                )
            },
            {
                it.copy(
                    seriesInferred = listOf("SeriesA", "SeriesB"),
                    merchInferred = listOf("Prints"),
                    editorNotes = "",
                )
            },
            {
                it.copy(
                    seriesInferred = it.seriesInferred + listOf("SeriesC"),
                    merchInferred = it.merchInferred + listOf("Charms"),
                    lastEditor = "firstlast@example.com",
                )
            },
            {
                it.copy(
                    seriesInferred = it.seriesInferred - listOf("SeriesA"),
                    merchInferred = it.merchInferred - listOf("Prints"),
                    seriesConfirmed = listOf("SeriesA", "SeriesC"),
                    merchConfirmed = listOf("Prints", "Washi tape"),
                )
            },
        )
        var previous =
            ArtistDatabaseEntry.Impl(
                year = DataYear.ANIME_EXPO_2026,
                id = AlleyCryptography.FAKE_ARTIST_ID.toString(),
                status = ArtistStatus.UNKNOWN,
                booth = "C38",
                name = "",
                summary = null,
                socialLinks = emptyList(),
                storeLinks = emptyList(),
                portfolioLinks = emptyList(),
                catalogLinks = emptyList(),
                driveLink = null,
                notes = null,
                commissions = emptyList(),
                seriesInferred = emptyList(),
                seriesConfirmed = emptyList(),
                merchInferred = emptyList(),
                merchConfirmed = emptyList(),
                images = emptyList(),
                counter = 0,
                editorNotes = null,
                lastEditor = "fakeemail@example.com",
                lastEditTime = Clock.System.now() - 1.hours,
                verifiedArtist = false,
            )
        database.saveArtist(dataYear = DataYear.ANIME_EXPO_2026, initial = null, updated = previous)
        database.saveArtist(
            dataYear = DataYear.ANIME_EXPO_2026, initial = null, updated = previous.copy(
                booth = "C39",
                name = "Test artist 2",
            )
        )
        database.saveArtist(
            dataYear = DataYear.ANIME_EXPO_2026, initial = null, updated = previous.copy(
                booth = "C40",
                name = "Test artist 3",
            )
        )
        artistUpdates.forEach {
            val next = it(previous).copy(lastEditTime = previous.lastEditTime!! + 1.minutes)
            database.saveArtist(
                dataYear = DataYear.ANIME_EXPO_2026,
                initial = previous,
                updated = next
            )
            previous = next
        }

        return previous
    }

    private suspend fun initializeTestStampRally(database: AlleyEditRemoteDatabase): StampRallyDatabaseEntry {
        val updates = listOf<(StampRallyDatabaseEntry) -> StampRallyDatabaseEntry>(
            {
                it.copy(
                    tables = listOf("C38", "C39", "C40"),
                    links = listOf("https://example.com"),
                    confirmed = true,
                    lastEditor = "firstlast@example.org",
                )
            },
            {
                it.copy(
                    notes = "Sticker pack contains 5 stickers",
                    lastEditor = "fakeemail@example.org",
                )
            },
        )

        var previous = StampRallyDatabaseEntry(
            year = DataYear.ANIME_EXPO_2026,
            id = Uuid.random().toString(),
            fandom = "Test stamp rally",
            hostTable = "C39",
            tables = listOf("C38", "C39"),
            links = emptyList(),
            tableMin = TableMin.Price(5),
            totalCost = 15,
            prize = "3 charms, sticker pack, risopgraph",
            prizeLimit = 50,
            series = listOf("Vocaloid", "Original"),
            merch = listOf("Charms", "Prints", "Stickers"),
            notes = null,
            images = emptyList(),
            counter = 1,
            confirmed = false,
            editorNotes = "No images available",
            lastEditor = null,
            lastEditTime = Clock.System.now(),
        )
        database.saveStampRally(
            dataYear = DataYear.ANIME_EXPO_2026,
            initial = null,
            updated = previous
        )
        updates.forEach {
            val next = it(previous).copy(lastEditTime = previous.lastEditTime!! + 1.minutes)
            database.saveStampRally(
                dataYear = DataYear.ANIME_EXPO_2026,
                initial = previous,
                updated = next
            )
            previous = next
        }

        return previous
    }
}
