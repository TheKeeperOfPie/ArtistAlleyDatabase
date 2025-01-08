package com.thekeeperofpie.artistalleydatabase.alley.artist

import kotlinx.serialization.Serializable

@Serializable
data class ArtistEntry(
    val id: String,
    val booth: String,
    val name: String,
    val summary: String? = null,
    val links: List<String> = emptyList(),
    val storeLinks: List<String> = emptyList(),
    val catalogLinks: List<String> = emptyList(),
    val driveLink: String? = null,
    val favorite: Boolean = false,
    val ignored: Boolean = false,
    val notes: String? = null,
    val seriesInferred: List<String> = emptyList(),
    val seriesConfirmed: List<String> = emptyList(),
    val merchInferred: List<String> = emptyList(),
    val merchConfirmed: List<String> = emptyList(),
    // Used for random ordering while maintaining a stable key
    val counter: Int = 1,
)
