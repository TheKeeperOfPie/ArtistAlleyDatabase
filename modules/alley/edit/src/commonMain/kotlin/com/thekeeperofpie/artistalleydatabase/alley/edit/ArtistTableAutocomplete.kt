package com.thekeeperofpie.artistalleydatabase.alley.edit

import com.github.terrakok.fuzzykot.MicroFuzz
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistTable
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.StampRallyUtils
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.karloti.cpq.ConcurrentPriorityQueue
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@SingleIn(AppScope::class)
open class ArtistTableAutocomplete(
    applicationScope: ApplicationScope,
    private val dispatchers: CustomDispatchers,
    loadTables: suspend (DataYear) -> List<ArtistTable>,
) {
    private val defaultFlow = ReadOnlyStateFlow(emptyList<ArtistTable>())
    private val defaultFlowByBooth = ReadOnlyStateFlow(emptyMap<String, ArtistTable>())

    @Inject
    constructor(
        applicationScope: ApplicationScope,
        dispatchers: CustomDispatchers,
        database: AlleyEditDatabase,
    ) : this(
        applicationScope = applicationScope,
        dispatchers = dispatchers,
        loadTables = database::loadTables,
    )

    private val animeExpo2026 = flowFromSuspend { loadTables(DataYear.ANIME_EXPO_2026) }
        .stateIn(applicationScope, SharingStarted.Eagerly, emptyList())

    private val animeExpo2026ByBooth = animeExpo2026.mapState(applicationScope) {
        it.associateBy { it.booth }
    }

    private val sharedQueue = ConcurrentPriorityQueue<Pair<ArtistTable, Int>, String>(
        maxSize = 20,
        comparator = compareByDescending { it.second },
        keySelector = { it.first.booth },
    )

    fun tablesByBooth(dataYear: DataYear) = when (dataYear) {
        DataYear.ANIME_EXPO_2023,
        DataYear.ANIME_EXPO_2024,
        DataYear.ANIME_EXPO_2025,
        DataYear.ANIME_NYC_2024,
        DataYear.ANIME_NYC_2025,
            -> defaultFlowByBooth
        DataYear.ANIME_EXPO_2026 -> animeExpo2026ByBooth
    }

    fun predictions(dataYear: DataYear, query: String) = when {
        query.isBlank() -> defaultFlow
        else -> when (dataYear) {
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_EXPO_2024,
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025,
                -> defaultFlow
            DataYear.ANIME_EXPO_2026 ->
                animeExpo2026.mapLatest {
                    val tables = sharedQueue.mutate {
                        it.forEach {
                            val score = if (it.booth.contains(query, ignoreCase = true)) {
                                100
                            } else {
                                MicroFuzz.ratio(query, it.name ?: it.booth)
                            }
                            add(it to score)
                        }
                    }.items.value.mapNotNull { result -> result.first.takeIf { result.second > 10 } }
                    val queryAsBooth = StampRallyUtils.toValidBooth(query)
                    val queryAsTable = queryAsBooth?.let { ArtistTable(booth = it, name = null) }
                        ?.takeIf { tables.none { it.booth == queryAsBooth } }
                    tables + listOfNotNull(queryAsTable)
                }
        }
    }.flowOn(dispatchers.io)
}
