package com.thekeeperofpie.artistalleydatabase.alley.artist

import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel

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
) {
    // TODO: Sort by type
    val linkModels by lazy {
        links.map { LinkModel.parseLinkModel(it) }
            .sortedWith(nullsFirst<LinkModel> { first, second -> first.link.compareTo(second.link) })
    }
    val storeLinkModels by lazy {
        storeLinks.map { LinkModel.parseLinkModel(it) }
            .sortedWith(nullsFirst<LinkModel> { first, second -> first.link.compareTo(second.link) })
    }
}
