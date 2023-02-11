package com.thekeeperofpie.artistalleydatabase.cds.search

import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchQuery

data class CdSearchQuery(
    override val query: String = "",
): EntrySearchQuery