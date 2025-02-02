package com.thekeeperofpie.artistalleydatabase.alley.artist

data class ArtistEntry(
    val id: String,
    val booth: String?,
    val name: String,
    val summary: String?,
    val links: List<String>,
    val storeLinks: List<String>,
    val catalogLinks: List<String>,
    val driveLink: String?,
    val notes: String?,
    val seriesInferred: List<String>,
    val seriesConfirmed: List<String>,
    val merchInferred: List<String>,
    val merchConfirmed: List<String>,
    val counter: Long,
)
