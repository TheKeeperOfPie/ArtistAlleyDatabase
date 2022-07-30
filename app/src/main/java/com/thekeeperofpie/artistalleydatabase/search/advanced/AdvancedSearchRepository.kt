package com.thekeeperofpie.artistalleydatabase.search.advanced

import androidx.collection.LruCache

/**
 * Stores advanced search queries in memory so that navigation doesn't have to pass around large
 * Parcelable arguments.
 */
object AdvancedSearchRepository {

    private val cache = LruCache<String, AdvancedSearchQuery>(10)

    fun registerQuery(query: AdvancedSearchQuery) {
        cache.put(query.id, query)
    }

    fun findQuery(id: String) = cache.get(id)
}