package com.thekeeperofpie.artistalleydatabase.alley.home

import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffoldState

internal class AlleyHomeNavigationState(
    val artistsScaffoldState: SortFilterBottomScaffoldState,
    val browseScaffoldState: SortFilterBottomScaffoldState,
    val favoritesScaffoldState: SortFilterBottomScaffoldState,
    val mapScaffoldState: SortFilterBottomScaffoldState,
    val ralliesScaffoldState: SortFilterBottomScaffoldState,
)
