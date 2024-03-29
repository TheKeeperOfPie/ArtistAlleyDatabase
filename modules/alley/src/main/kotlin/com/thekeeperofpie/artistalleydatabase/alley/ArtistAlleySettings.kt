package com.thekeeperofpie.artistalleydatabase.alley

interface ArtistAlleySettings {

    var lastKnownCsvSize: Long
    var displayType: String
    var artistsSortOption: String
    var artistsSortAscending: Boolean
    var showRegion: Boolean
    var showGridByDefault: Boolean
}
