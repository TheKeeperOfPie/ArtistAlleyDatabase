package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import kotlinx.coroutines.flow.FlowCollector

object SearchUtils {

    context(collector: FlowCollector<List<T>>)
    suspend fun <T> incrementallyPartition(values: Collection<T>, vararg filters: (T) -> Boolean) {
        val results = mutableListOf<T>()
        var remaining = values
        filters.forEach {
            val (newResults, newRemaining) = remaining.partition(it)
            results.addAll(newResults)
            remaining = newRemaining
            collector.emit(results)
        }
    }
}
