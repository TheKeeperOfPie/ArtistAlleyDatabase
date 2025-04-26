package com.thekeeperofpie.artistalleydatabase.alley.artist

import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

object ArtistWithUserDataProvider : PreviewParameterProvider<ArtistWithUserData> {
    override val values = sequence {
        val artist = ArtistWithUserData(
            artist = ArtistEntry(
                year = DataYear.YEAR_2025,
                id = "artistId",
                booth = "C39",
                name = "Hatsune Miku",
                summary = "Summary summary summary",
                links = listOf("https://x.com/example", "https://instagram.com/example"),
                storeLinks = listOf("https://etsy.com/Example"),
                catalogLinks = emptyList(),
                driveLink = null,
                notes = null,
                commissions = listOf("https://vgen.co/Example"),
                seriesInferred = listOf("Inferred Series", "Confirmed Series"),
                seriesConfirmed = listOf("Confirmed Series"),
                merchInferred = listOf("Stickers", "Prints"),
                merchConfirmed = listOf("Bags", "Shirts", "Stickers", "Prints"),
                counter = 1,
            ),
            userEntry = ArtistUserEntry(
                artistId = "artistId",
                dataYear = DataYear.YEAR_2025,
                favorite = false,
                ignored = false,
            ),
        )
        yield(artist)
        yield(artist.copy(artist = artist.artist.copy(booth = "C40", name = "Megurine Luka")))
        yield(artist.copy(artist = artist.artist.copy(booth = "C41", name = "Kagamine Rin")))
    }
}
