package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@SingleIn(AppScope::class)
@Inject
class ArtistCache(
    applicationScope: ApplicationScope,
    private val database: AlleyEditDatabase,
) {
    private val refreshFlow = RefreshFlow()
    val artistsAnimeExpo2026 = refreshFlow.updates
        .mapLatest { database.loadArtists(DataYear.ANIME_EXPO_2026).reversed() }
        .stateIn(applicationScope, SharingStarted.Eagerly, emptyList())

    fun artists(dataYear: DataYear): Flow<List<ArtistSummary>> = when (dataYear) {
        DataYear.ANIME_EXPO_2026 -> artistsAnimeExpo2026
        DataYear.ANIME_EXPO_2023,
        DataYear.ANIME_EXPO_2024,
        DataYear.ANIME_EXPO_2025,
        DataYear.ANIME_NYC_2024,
        DataYear.ANIME_NYC_2025,
            -> flowFromSuspend { database.loadArtists(dataYear).reversed() }
    }

    fun refresh() = refreshFlow.refresh()
}
