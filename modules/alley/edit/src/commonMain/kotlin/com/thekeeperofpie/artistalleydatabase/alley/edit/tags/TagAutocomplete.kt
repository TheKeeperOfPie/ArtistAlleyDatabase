package com.thekeeperofpie.artistalleydatabase.alley.edit.tags

import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.SearchUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn

class TagAutocomplete(
    scope: CoroutineScope,
    database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
) {
    val seriesById = flowFromSuspend { database.loadSeries() }
        .shareIn(scope, SharingStarted.Eagerly, 1)

    val merchById = flowFromSuspend { database.loadMerch() }
        .shareIn(scope, SharingStarted.Eagerly, 1)

    fun seriesPredictions(query: String) = if (query.length < 3) {
        flowOf(emptyList())
    } else {
        seriesById.flatMapLatest {
            flow {
                SearchUtils.incrementallyPartition(
                    values = it.values,
                    { it.titlePreferred.contains(query, ignoreCase = true) },
                    { it.titleRomaji.contains(query, ignoreCase = true) },
                    { it.titleEnglish.contains(query, ignoreCase = true) },
                    { it.synonyms.any { it.contains(query, ignoreCase = true) } },
                )
            }
        }.flowOn(dispatchers.io)
    }

    fun merchPredictions(query: String) =
        merchById
            .mapLatest {
                it.values
                    .filter { it.name.contains(query, ignoreCase = true) }
                    .sortedBy { it.name }
            }
            .flowOn(dispatchers.io)
}
