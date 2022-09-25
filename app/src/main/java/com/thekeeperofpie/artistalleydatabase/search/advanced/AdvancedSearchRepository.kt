package com.thekeeperofpie.artistalleydatabase.search.advanced

import androidx.collection.LruCache
import com.thekeeperofpie.artistalleydatabase.art.search.ArtAdvancedSearchQuery

/**
 * Stores advanced search queries in memory so that navigation doesn't have to pass around large
 * Parcelable arguments.
 */
object AdvancedSearchRepository {

    private val cache = LruCache<String, ArtAdvancedSearchQuery>(10)

    fun registerQuery(query: ArtAdvancedSearchQuery) {
        cache.put(query.id, query)
    }

    fun findQuery(id: String) = cache.get(id)
}