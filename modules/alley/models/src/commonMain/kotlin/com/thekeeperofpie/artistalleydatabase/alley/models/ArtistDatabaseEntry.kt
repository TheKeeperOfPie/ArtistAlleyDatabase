package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface ArtistDatabaseEntry {
    val year: DataYear
    val id: String
    val status: ArtistStatus
    val booth: String?
    val name: String
    val summary: String?
    val socialLinks: List<String>
    val storeLinks: List<String>
    val portfolioLinks: List<String>
    val catalogLinks: List<String>
    val driveLink: String?
    val notes: String?
    val commissions: List<String>
    val seriesInferred: List<String>
    val seriesConfirmed: List<String>
    val merchInferred: List<String>
    val merchConfirmed: List<String>
    val images: List<DatabaseImage>
    val fallbackImages: List<DatabaseImage>
    val fallbackImageYear: DataYear?
    val tempImages: List<DatabaseImage>
    val embeds: Map<String, DatabaseImage>
    val editorNotes: String?
    val lastEditor: String?
    val lastEditTime: Instant?
    val verifiedArtist: Boolean
    val newArtist: Boolean

    @Serializable
    data class Impl(
        override val year: DataYear,
        override val id: String,
        override val status: ArtistStatus,
        override val booth: String?,
        override val name: String,
        override val summary: String?,
        override val socialLinks: List<String>,
        override val storeLinks: List<String>,
        override val portfolioLinks: List<String>,
        override val catalogLinks: List<String>,
        override val driveLink: String?,
        override val notes: String?,
        override val commissions: List<String>,
        override val seriesInferred: List<String>,
        override val seriesConfirmed: List<String>,
        override val merchInferred: List<String>,
        override val merchConfirmed: List<String>,
        val _images: List<DatabaseImage>,
        override val fallbackImageYear: DataYear?,
        override val tempImages: List<DatabaseImage>,
        override val embeds: Map<String, DatabaseImage>,
        override val editorNotes: String?,
        override val lastEditor: String?,
        override val lastEditTime: Instant?,
        override val verifiedArtist: Boolean,
        override val newArtist: Boolean,
    ) : ArtistDatabaseEntry {
        override val images: List<DatabaseImage>
            get() = _images.takeIf { fallbackImageYear == null }.orEmpty()
        override val fallbackImages: List<DatabaseImage>
            get() = _images.takeIf { fallbackImageYear != null }.orEmpty()

    }

    companion object {

        // Need to ignore metadata for equality
        fun hasChanged(before: Impl?, after: Impl?) =
            before?.copy(
                embeds = emptyMap(),
                verifiedArtist = false,
                newArtist = false,
                lastEditTime = null,
                lastEditor = null
            ) != after?.copy(
                embeds = emptyMap(),
                verifiedArtist = false,
                newArtist = false,
                lastEditTime = null,
                lastEditor = null
            )

        fun legacy(
            year: DataYear,
            id: String,
            booth: String?,
            name: String,
            summary: String?,
            socialLinks: List<String>,
            storeLinks: List<String>,
            catalogLinks: List<String>,
            driveLink: String?,
            notes: String?,
            commissions: List<String>,
            seriesInferred: List<String>,
            seriesConfirmed: List<String>,
            merchInferred: List<String>,
            merchConfirmed: List<String>,
            images: List<DatabaseImage>,
            fallbackImageYear: DataYear?,
            newArtist: Boolean,
        ) = Impl(
            year = year,
            id = id,
            booth = booth,
            name = name,
            summary = summary,
            socialLinks = socialLinks,
            storeLinks = storeLinks,
            portfolioLinks = catalogLinks.takeIf { images.isEmpty() }.orEmpty(),
            catalogLinks = catalogLinks.takeIf { images.isNotEmpty() }.orEmpty(),
            driveLink = driveLink,
            notes = notes,
            commissions = commissions,
            seriesInferred = seriesInferred,
            seriesConfirmed = seriesConfirmed,
            merchInferred = merchInferred,
            merchConfirmed = merchConfirmed,
            _images = images,
            fallbackImageYear = fallbackImageYear,
            tempImages = emptyList(),
            embeds = emptyMap(),
            status = ArtistStatus.UNKNOWN,
            editorNotes = null,
            lastEditor = null,
            lastEditTime = null,
            verifiedArtist = false,
            newArtist = newArtist,
        )
    }
}

fun ArtistDatabaseEntry.toArtistSummary() = ArtistSummary(
    id = Uuid.parse(id),
    booth = booth,
    name = name,
    socialLinks = socialLinks,
    storeLinks = storeLinks,
    portfolioLinks = portfolioLinks,
    catalogLinks = catalogLinks,
    seriesInferred = seriesInferred,
    seriesConfirmed = seriesConfirmed,
    merchInferred = merchInferred,
    merchConfirmed = merchConfirmed,
    images = images,
)
