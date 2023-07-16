package com.thekeeperofpie.artistalleydatabase.anime.filter

import androidx.compose.runtime.Composable

interface SortFilterController {

    val sections: List<SortFilterSection>
    val state: SortFilterSection.ExpandedState

    @Composable
    fun collapseOnClose(): Boolean?
}
