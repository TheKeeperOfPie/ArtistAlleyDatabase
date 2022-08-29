package com.thekeeperofpie.artistalleydatabase.cds.search

import com.thekeeperofpie.artistalleydatabase.form.search.EntrySearchQuery

data class CdSearchQuery(
    override val query: String = "",
): EntrySearchQuery