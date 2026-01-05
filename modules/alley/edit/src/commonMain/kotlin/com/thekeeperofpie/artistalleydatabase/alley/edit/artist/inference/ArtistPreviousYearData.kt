package com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference

data class ArtistPreviousYearData(
    val artistId: String,
    val name: String?,
    val summary: String?,
    val socialLinks: List<String>,
    val storeLinks: List<String>,
    val seriesInferred: List<String>,
    val merchInferred: List<String>,
)
