package com.thekeeperofpie.artistalleydatabase.alley.edit.tags

import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.SearchUtils
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn

@SingleIn(AppScope::class)
@Inject
class TagAutocomplete(
    applicationScope: ApplicationScope,
    database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
) {
    val seriesById = flowFromSuspend { database.loadSeries() }
        .shareIn(applicationScope, SharingStarted.Eagerly, 1)

    val merchById = flowFromSuspend { database.loadMerch() }
        .shareIn(applicationScope, SharingStarted.Eagerly, 1)

    fun seriesPredictions(query: String) = when {
        query.isBlank() -> flowOf(emptyList())
        query.length < 3 -> flowOf(listOf(SeriesInfo.fake(query)))
        else -> seriesById.flatMapLatest {
            flow {
                SearchUtils.incrementallyPartition(
                    values = it.values,
                    { it.titlePreferred.contains(query, ignoreCase = true) },
                    { it.titleRomaji.contains(query, ignoreCase = true) },
                    { it.titleEnglish.contains(query, ignoreCase = true) },
                    { it.synonyms.any { it.contains(query, ignoreCase = true) } },
                    finalTransform = { it + SeriesInfo.fake(query) },
                )
            }
        }
    }.flowOn(dispatchers.io)

    fun merchPredictions(query: String) =
        merchById
            .mapLatest {
                it.values
                    .filter { it.name.contains(query, ignoreCase = true) }
                    .sortedBy { it.name } +
                        listOfNotNull(query.takeIf { it.isNotBlank() }
                            ?.let(MerchInfo::fake))
            }
            .flowOn(dispatchers.io)
}
