package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.navigation.NavController
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class DeepLinker(private val artistEntryDao: ArtistEntryDao) {

    suspend fun processRoute(navController: NavController, route: String): Boolean {
        if (route.startsWith("artist")) {
            val pieces = route.split("/")
            if (pieces.size < 3) return false
            val targetConvention = pieces[1]
            val year = DataYear.entries.find { it.serializedName == targetConvention }
                ?: return false
            val artistId = artistEntryDao.getEntriesByBooth(
                year = year,
                booth = pieces[2],
            ).firstOrNull()?.id ?: return false
            val entry = artistEntryDao.getEntry(year, artistId)?.artist ?: return false
            navController.navigate(
                Destinations.ArtistDetails(entry, null)
            )
            return true
        }
        return false
    }
}
