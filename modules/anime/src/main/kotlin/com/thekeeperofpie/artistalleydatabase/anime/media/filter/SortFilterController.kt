package com.thekeeperofpie.artistalleydatabase.anime.media.filter

interface SortFilterController {

    val sections: List<SortFilterSection>
    val state: SortFilterSection.ExpandedState

    fun collapseOnClose(): Boolean
}
