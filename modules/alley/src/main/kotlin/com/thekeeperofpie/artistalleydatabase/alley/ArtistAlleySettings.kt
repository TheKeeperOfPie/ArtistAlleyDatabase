package com.thekeeperofpie.artistalleydatabase.alley

interface ArtistAlleySettings {

    var lastKnownArtistsCsvSize: Long
    var lastKnownStampRalliesCsvSize: Long
    var displayType: String
    var artistsSortOption: String
    var artistsSortAscending: Boolean
    var stampRalliesSortOption: String
    var stampRalliesSortAscending: Boolean
    var showGridByDefault: Boolean
}
