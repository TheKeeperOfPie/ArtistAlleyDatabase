package com.thekeeperofpie.artistalleydatabase.alley.artist

import kotlinx.serialization.Serializable

@Serializable
expect class ArtistEntry {
    val id: String
    val booth: String
    val name: String
    val summary: String?
    val links: List<String>
    val storeLinks: List<String>
    val catalogLinks: List<String>
    val driveLink: String?
    val favorite: Boolean
    val ignored: Boolean
    val notes: String?
    val seriesInferred: List<String>
    val seriesConfirmed: List<String>
    val merchInferred: List<String>
    val merchConfirmed: List<String>

    // Used for random ordering while maintaining a stable key
    val counter: Int

    constructor(
        id: String,
        booth: String,
        name: String,
        summary: String? = null,
        links: List<String> = emptyList(),
        storeLinks: List<String> = emptyList(),
        catalogLinks: List<String> = emptyList(),
        driveLink: String? = null,
        favorite: Boolean = false,
        ignored: Boolean = false,
        notes: String? = null,
        seriesInferred: List<String> = emptyList(),
        seriesConfirmed: List<String> = emptyList(),
        merchInferred: List<String> = emptyList(),
        merchConfirmed: List<String> = emptyList(),
        counter: Int = 1,
    )

    fun copy(
        id: String = this.id,
        booth: String = this.booth,
        name: String = this.name,
        summary: String? = this.summary,
        links: List<String> = this.links,
        storeLinks: List<String> = this.storeLinks,
        catalogLinks: List<String> = this.catalogLinks,
        driveLink: String? = this.driveLink,
        favorite: Boolean = this.favorite,
        ignored: Boolean = this.ignored,
        notes: String? = this.notes,
        seriesInferred: List<String> = this.seriesInferred,
        seriesConfirmed: List<String> = this.seriesConfirmed,
        merchInferred: List<String> = this.merchInferred,
        merchConfirmed: List<String> = this.merchConfirmed,
    ): ArtistEntry
}
