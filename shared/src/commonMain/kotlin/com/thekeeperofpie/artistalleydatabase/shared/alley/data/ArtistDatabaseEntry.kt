package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.serialization.Serializable

interface ArtistDatabaseEntry {
    val year: DataYear
    val id: String
    val booth: String?
    val name: String
    val summary: String?
    val links: List<String>
    val storeLinks: List<String>
    val catalogLinks: List<String>
    val driveLink: String?
    val notes: String?
    val commissions: List<String>
    val seriesInferred: List<String>
    val seriesConfirmed: List<String>
    val merchInferred: List<String>
    val merchConfirmed: List<String>
    val images: List<CatalogImage>
    val counter: Long

    @Serializable
    data class Impl(
        override val year: DataYear,
        override val id: String,
        override val booth: String?,
        override val name: String,
        override val summary: String?,
        override val links: List<String>,
        override val storeLinks: List<String>,
        override val catalogLinks: List<String>,
        override val driveLink: String?,
        override val notes: String?,
        override val commissions: List<String>,
        override val seriesInferred: List<String>,
        override val seriesConfirmed: List<String>,
        override val merchInferred: List<String>,
        override val merchConfirmed: List<String>,
        override val images: List<CatalogImage>,
        override val counter: Long,
    ) : ArtistDatabaseEntry
}
