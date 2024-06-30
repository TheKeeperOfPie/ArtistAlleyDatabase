package com.thekeeperofpie.artistalleydatabase.alley

import kotlinx.coroutines.flow.MutableStateFlow

interface ArtistAlleySettings {

    val lastKnownArtistsCsvSize: MutableStateFlow<Long>
    val lastKnownStampRalliesCsvSize: MutableStateFlow<Long>
    val displayType: MutableStateFlow<String>
    val artistsSortOption: MutableStateFlow<String>
    val artistsSortAscending: MutableStateFlow<Boolean>
    val stampRalliesSortOption: MutableStateFlow<String>
    val stampRalliesSortAscending: MutableStateFlow<Boolean>
    val showGridByDefault: MutableStateFlow<Boolean>
    val showRandomCatalogImage: MutableStateFlow<Boolean>
    val showOnlyConfirmedTags: MutableStateFlow<Boolean>
    val forceOneDisplayColumn: MutableStateFlow<Boolean>
}
