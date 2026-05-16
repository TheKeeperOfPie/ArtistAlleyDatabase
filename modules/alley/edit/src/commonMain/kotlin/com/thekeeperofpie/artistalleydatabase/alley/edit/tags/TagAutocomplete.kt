package com.thekeeperofpie.artistalleydatabase.alley.edit.tags

import androidx.compose.ui.util.fastCoerceAtLeast
import com.github.terrakok.fuzzykot.MicroFuzz
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.karloti.cpq.ConcurrentPriorityQueue
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@SingleIn(AppScope::class)
open class TagAutocomplete(
    applicationScope: ApplicationScope,
    private val dispatchers: CustomDispatchers,
    loadSeries: suspend () -> Map<String, SeriesInfo>,
    loadMerch: suspend () -> Map<String, MerchInfo>,
) {
    @Inject
    constructor(
        applicationScope: ApplicationScope,
        dispatchers: CustomDispatchers,
        database: AlleyEditDatabase,
    ) : this(
        applicationScope = applicationScope,
        dispatchers = dispatchers,
        loadSeries = database::loadSeries,
        loadMerch = database::loadMerch,
    )

    val seriesById = flowFromSuspend { loadSeries() }
        .stateIn(applicationScope, SharingStarted.Eagerly, emptyMap())

    val merchById = flowFromSuspend { loadMerch() }
        .stateIn(applicationScope, SharingStarted.Eagerly, emptyMap())

    private val merchFiltered = merchById.mapState(applicationScope) {
        // Remove deprecated tags
        it.values.filterNot { it.name.contains("Commission") }
            .sortedBy { it.name }
    }

    private val sharedSeriesQueue = ConcurrentPriorityQueue<Pair<SeriesInfo, Int>, String>(
        maxSize = 20,
        comparator = compareByDescending { it.second },
        keySelector = { it.first.id },
    )

    private val sharedMerchQueue = ConcurrentPriorityQueue<Pair<MerchInfo, Int>, String>(
        maxSize = 20,
        comparator = compareByDescending { it.second },
        keySelector = { it.first.name },
    )

    fun seriesPredictions(query: String, allowCustom: Boolean = true) = when {
        query.isBlank() -> flowOf(emptyList())
        query.length < 3 -> flowOf(listOfNotNull(SeriesInfo.fake(query).takeIf { allowCustom }))
        else -> seriesById.mapLatest {
            sharedSeriesQueue.mutate {
                it.values.forEach {
                    add(it to it.calculateScore(query))
                }
            }.items.value.mapNotNull { result -> result.first.takeIf { result.second > 10 } } +
                    listOfNotNull(SeriesInfo.fake(query).takeIf { allowCustom })
        }
    }.flowOn(dispatchers.io)

    private fun SeriesInfo.calculateScore(query: String): Int {
        val scoreId = MicroFuzz.ratio(query, id)
        if (scoreId >= 90) return scoreId
        val scorePreferred = MicroFuzz.ratio(query, titlePreferred)
        if (scorePreferred >= 90) return scorePreferred
        val scoreRomaji = MicroFuzz.ratio(query, titleRomaji)
        if (scoreRomaji >= 90) return scoreRomaji
        val scoreEnglish = MicroFuzz.ratio(query, titleEnglish)
        if (scoreEnglish >= 90) return scoreEnglish
        val scoreSynonyms = synonyms.maxOfOrNull { MicroFuzz.ratio(query, it) } ?: 0
        return scoreId
            .fastCoerceAtLeast(scorePreferred)
            .fastCoerceAtLeast(scoreRomaji)
            .fastCoerceAtLeast(scoreEnglish)
            .fastCoerceAtLeast(scoreSynonyms)
    }

    fun merchPredictions(query: String, allowCustom: Boolean = true) =
        merchFiltered
            .mapLatest {
                if (query.isBlank()) {
                    it
                } else {
                    sharedMerchQueue.mutate {
                        it.forEach {
                            add(it to it.calculateScore(query))
                        }
                    }.items.value.mapNotNull { result -> result.first.takeIf { result.second > 10 } } +
                            listOfNotNull(query.takeIf { it.isNotBlank() }
                                .takeIf { allowCustom }
                                ?.let(MerchInfo::fake))
                }
            }
            .flowOn(dispatchers.io)

    private fun MerchInfo.calculateScore(query: String): Int {
        val scoreName = MicroFuzz.ratio(query, name)
        val notes = notes
        if (scoreName >= 90 || notes == null) return scoreName
        return MicroFuzz.ratio(query, notes)
            .coerceAtLeast(scoreName)
    }
}
