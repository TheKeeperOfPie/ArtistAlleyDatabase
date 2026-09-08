package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import kotlin.random.Random
import kotlin.uuid.Uuid

object ArtistWithUserDataProvider : PreviewParameterProvider<ArtistWithUserData> {
    override val values = sequence {
        val random = Random(1234)
        fun generateArtistId() = Uuid.fromLongs(random.nextLong(), random.nextLong()).toString()
        val artistId = generateArtistId()
        val databaseEntry =
            ArtistDatabaseEntry.Impl(
                year = DataYear.LATEST,
                id = artistId,
                status = ArtistStatus.FINAL,
                booth = "C39",
                name = "Hatsune Miku",
                summary = "Summary summary summary",
                socialLinks = listOf("https://x.com/example", "https://instagram.com/example"),
                storeLinks = listOf("https://etsy.com/Example"),
                portfolioLinks = listOf("https://example.carrd.co"),
                catalogLinks = emptyList(),
                driveLink = null,
                notes = null,
                commissions = listOf("https://vgen.co/Example"),
                seriesInferred = listOf("Inferred Series", "Confirmed Series"),
                seriesConfirmed = listOf("Confirmed Series"),
                merchInferred = listOf("Stickers", "Prints"),
                merchConfirmed = listOf("Bags", "Shirts", "Stickers", "Prints"),
                _images = emptyList(),
                fallbackImageYear = null,
                tempImages = emptyList(),
                profileImage = DatabaseImage(
                    name = "$artistId/profileImage.webp",
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
        val artist = ArtistWithUserData(
            artist = ArtistEntry(databaseEntry),
            userEntry = ArtistUserEntry(
                artistId = databaseEntry.id,
                dataYear = DataYear.ANIME_EXPO_2025,
                favorite = false,
                ignored = false,
            ),
        )
        yield(artist)

        val artistTwo = databaseEntry.copy(
            id = generateArtistId(),
            booth = "C40",
            name = "Megurine Luka",
        )
        yield(
            artist.copy(
                artist = artist.artist.copy(databaseEntry = artistTwo),
                userEntry = artist.userEntry.copy(artistId = artistTwo.id)
            )
        )

        val artistThree = databaseEntry.copy(
            id = generateArtistId(),
            booth = "U41",
            name = "Kagamine Rin",
        )
        yield(
            artist.copy(
                artist = artist.artist.copy(databaseEntry = artistThree),
                userEntry = artist.userEntry.copy(artistId = artistTwo.id)
            )
        )
    }
}
