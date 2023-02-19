package com.thekeeperofpie.artistalleydatabase.cds.search

import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchQuery

data class CdSearchQuery(
    override val query: String = "",
    val includeCatalogIds: Boolean = false,
    val includeTitles: Boolean = false,
    val includePerformers: Boolean = false,
    val includeComposers: Boolean = false,
    val includeSeries: Boolean = false,
    val includeCharacters: Boolean = false,
    val includeDiscs: Boolean = false,
    val includeTags: Boolean = false,
    val includeNotes: Boolean = false,
    val includeOther: Boolean = false,
    val locked: Boolean = false,
    val unlocked: Boolean = false,
): EntrySearchQuery {
    val includeAll = !includeCatalogIds && !includeTitles && !includePerformers
            && !includeComposers && !includeSeries && !includeCharacters && !includeDiscs
            && !includeTags && !includeNotes && !includeOther && !locked && !unlocked
}