package com.thekeeperofpie.artistalleydatabase.alley.artist

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

object ArtistWithUserDataProvider : PreviewParameterProvider<ArtistWithUserData> {
    override val values = sequence {
        val databaseEntry =
            ArtistDatabaseEntry.Impl(
                year = DataYear.ANIME_EXPO_2025,
                id = "artistId",
                status = ArtistStatus.FINAL,
                booth = "C39",
                name = "Hatsune Miku",
                summary = "Summary summary summary",
                socialLinks = listOf("https://x.com/example", "https://instagram.com/example"),
                storeLinks = listOf("https://etsy.com/Example"),
                catalogLinks = emptyList(),
                driveLink = null,
                notes = null,
                commissions = listOf("https://vgen.co/Example"),
                seriesInferred = listOf("Inferred Series", "Confirmed Series"),
                seriesConfirmed = listOf("Confirmed Series"),
                merchInferred = listOf("Stickers", "Prints"),
                merchConfirmed = listOf("Bags", "Shirts", "Stickers", "Prints"),
                images = emptyList(),
                counter = 1,
                editorNotes = null,
                lastEditor = null,
                lastEditTime = null,
                verifiedArtist = false,
            )
        val artist = ArtistWithUserData(
            artist = ArtistEntry(databaseEntry),
            userEntry = ArtistUserEntry(
                artistId = "artistId",
                dataYear = DataYear.ANIME_EXPO_2025,
                favorite = false,
                ignored = false,
            ),
        )
        yield(artist)
        yield(
            artist.copy(
                artist = artist.artist.copy(
                    databaseEntry = databaseEntry.copy(
                        booth = "C40",
                        name = "Megurine Luka",
                    )
                )
            )
        )
        yield(
            artist.copy(
                artist = artist.artist.copy(
                    databaseEntry = databaseEntry.copy(
                        booth = "C41",
                        name = "Kagamine Rin",
                    )
                )
            )
        )
    }
}
