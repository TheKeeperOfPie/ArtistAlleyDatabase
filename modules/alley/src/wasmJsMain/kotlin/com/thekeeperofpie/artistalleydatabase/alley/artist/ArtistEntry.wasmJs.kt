package com.thekeeperofpie.artistalleydatabase.alley.artist

import kotlinx.serialization.Serializable

@Serializable
actual data class ArtistEntry actual constructor(
    actual val id: String,
    actual val booth: String,
    actual val name: String,
    actual val summary: String?,
    actual val links: List<String>,
    actual val storeLinks: List<String>,
    actual val catalogLinks: List<String>,
    actual val driveLink: String?,
    actual val favorite: Boolean,
    actual val ignored: Boolean,
    actual val notes: String?,
    actual val seriesInferred: List<String>,
    actual val seriesConfirmed: List<String>,
    actual val merchInferred: List<String>,
    actual val merchConfirmed: List<String>,
    // Used fo random ordering while maintaining a stable key
    actual val counter: Int,
) {
    actual fun copy(
        id: String,
        booth: String,
        name: String,
        summary: String?,
        links: List<String>,
        storeLinks: List<String>,
        catalogLinks: List<String>,
        driveLink: String?,
        favorite: Boolean,
        ignored: Boolean,
        notes: String?,
        seriesInferred: List<String>,
        seriesConfirmed: List<String>,
        merchInferred: List<String>,
        merchConfirmed: List<String>,
    ) = copy(
        id = id,
        booth = booth,
        name = name,
        summary = summary,
        links = links,
        storeLinks = storeLinks,
        catalogLinks = catalogLinks,
        driveLink = driveLink,
        favorite = favorite,
        ignored = ignored,
        notes = notes,
        seriesInferred = seriesInferred,
        seriesConfirmed = seriesConfirmed,
        merchInferred = merchInferred,
        merchConfirmed = merchConfirmed,
        counter = this.counter,
    )
}
