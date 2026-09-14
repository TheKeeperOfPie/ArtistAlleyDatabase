package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.Booth
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import kotlin.random.Random
import kotlin.uuid.Uuid

object ArtistWithUserDataProvider : PreviewParameterProvider<ArtistWithUserData> {

    private val artistIds = sequence {
        val random = Random(39)
        while (true) {
            yield(Uuid.fromLongs(random.nextLong(), random.nextLong()).toString())
        }
    }

    private val booths = sequence {
        var current = Booth.fromStringOrNull("C39")!!
        while (true) {
            yield(current)
            current = current.generateNext()
        }
    }

    private val names = sequence {
        val names = listOf("Hatsune Miku", "Megurine Luka", "Kagamine Rin")
        var currentIndex = 0
        while (true) {
            yield(names[currentIndex % names.size])
            currentIndex++
        }
    }

    override val values =
        artistIds.zip(booths).zip(names).mapIndexed { index, (idAndBooth, name) ->
            val (artistId, booth) = idAndBooth
            val databaseEntry = ArtistDatabaseEntry.Impl(
                year = DataYear.LATEST,
                id = artistId,
                status = ArtistStatus.FINAL,
                booth = booth.toString(),
                name = name,
                summary = "Summary summary summary",
                socialLinks = listOf("https://x.com/example", "https://instagram.com/example"),
                storeLinks = listOf("https://etsy.com/Example"),
                portfolioLinks = listOf("https://example.carrd.co"),
                catalogLinks = emptyList(),
                driveLink = null,
                notes = null,
                commissions = listOf("https://vgen.co/Example").takeIf { index % 2 == 0 }.orEmpty(),
                seriesInferred = listOf("Inferred Series", "Confirmed Series"),
                seriesConfirmed = listOf("Confirmed Series"),
                merchInferred = listOf("Stickers", "Prints"),
                merchConfirmed = listOf("Bags", "Shirts", "Stickers", "Prints"),
                _images = listOf(
                    DatabaseImage(
                        name = "$artistId/image$index-0.webp",
                        width = 1000,
                        height = 500,
                        color = null,
                    ),
                    DatabaseImage(
                        name = "$artistId/image$index-1.webp",
                        width = 1000,
                        height = 500,
                        color = null,
                    ),
                ),
                fallbackImageYear = null,
                tempImages = emptyList(),
                profileImage = DatabaseImage(
                    name = "$artistId/profileImage$index.webp",
                    width = null,
                    height = null,
                    color = null,
                ),
                embeds = emptyMap(),
                editorNotes = null,
                lastEditor = null,
                lastEditTime = null,
                verifiedArtist = false,
                newArtist = false,
            )

            ArtistWithUserData(
                artist = ArtistEntry(databaseEntry),
                userEntry = ArtistUserEntry(
                    artistId = databaseEntry.id,
                    dataYear = DataYear.ANIME_EXPO_2025,
                    favorite = index % 2 == 1,
                    ignored = index % 3 == 2,
                ),
            )
        }
}
