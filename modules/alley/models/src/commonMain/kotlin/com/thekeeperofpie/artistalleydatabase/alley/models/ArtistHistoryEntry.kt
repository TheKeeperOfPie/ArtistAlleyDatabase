package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class ArtistHistoryEntry(
    val status: ArtistStatus?,
    val booth: String?,
    val name: String?,
    val summary: String?,
    val socialLinks: List<String>?,
    val storeLinks: List<String>?,
    val portfolioLinks: List<String>?,
    val catalogLinks: List<String>?,
    val notes: String?,
    val commissions: List<String>?,
    val seriesInferred: List<String>?,
    val seriesConfirmed: List<String>?,
    val merchInferred: List<String>?,
    val merchConfirmed: List<String>?,
    val images: List<CatalogImage>?,
    val editorNotes: String?,
    val lastEditor: String?,
    val timestamp: Instant,
    val formTimestamp: Instant?,
) {
    companion object {
        fun create(
            before: ArtistDatabaseEntry?,
            after: ArtistDatabaseEntry,
            formTimestamp: Instant?,
        ) = ArtistHistoryEntry(
            status = after.status.takeIf { it != before?.status },
            booth = after.booth.takeIf { it != before?.booth },
            name = after.name.takeIf { it != before?.name },
            summary = after.summary.takeIf { it != before?.summary },
            socialLinks = after.socialLinks.takeIf { it != before?.socialLinks },
            storeLinks = after.storeLinks.takeIf { it != before?.storeLinks },
            portfolioLinks = after.portfolioLinks.takeIf { it != before?.portfolioLinks },
            catalogLinks = after.catalogLinks.takeIf { it != before?.catalogLinks },
            notes = after.notes.takeIf { it != before?.notes },
            commissions = after.commissions.takeIf { it != before?.commissions },
            seriesInferred = after.seriesInferred.takeIf { it != before?.seriesInferred },
            seriesConfirmed = after.seriesConfirmed.takeIf { it != before?.seriesConfirmed },
            merchInferred = after.merchInferred.takeIf { it != before?.merchInferred },
            merchConfirmed = after.merchConfirmed.takeIf { it != before?.merchConfirmed },
            images = after.images.takeIf { it != before?.images },
            editorNotes = after.editorNotes.takeIf { it != before?.editorNotes },
            lastEditor = after.lastEditor,
            timestamp = after.lastEditTime ?: Clock.System.now(),
            formTimestamp = formTimestamp,
        )

        fun rebuild(
            dataYear: DataYear,
            artistId: Uuid,
            list: List<ArtistHistoryEntry>,
        ): ArtistDatabaseEntry.Impl {
            var status: ArtistStatus? = null
            var booth: String? = null
            var name: String? = null
            var summary: String? = null
            var socialLinks: List<String>? = null
            var storeLinks: List<String>? = null
            var portfolioLinks: List<String>? = null
            var catalogLinks: List<String>? = null
            var notes: String? = null
            var commissions: List<String>? = null
            var seriesInferred: List<String>? = null
            var seriesConfirmed: List<String>? = null
            var merchInferred: List<String>? = null
            var merchConfirmed: List<String>? = null
            var images: List<CatalogImage>? = null
            var editorNotes: String? = null
            var lastEditor: String? = null

            list.forEach {
                status = status ?: it.status
                booth = booth ?: it.booth
                name = name ?: it.name
                summary = summary ?: it.summary
                socialLinks = socialLinks ?: it.socialLinks
                storeLinks = storeLinks ?: it.storeLinks
                portfolioLinks = portfolioLinks ?: it.portfolioLinks
                catalogLinks = catalogLinks ?: it.catalogLinks
                notes = notes ?: it.notes
                commissions = commissions ?: it.commissions
                seriesInferred = seriesInferred ?: it.seriesInferred
                seriesConfirmed = seriesConfirmed ?: it.seriesConfirmed
                merchInferred = merchInferred ?: it.merchInferred
                merchConfirmed = merchConfirmed ?: it.merchConfirmed
                images = images ?: it.images
                editorNotes = editorNotes ?: it.editorNotes
                lastEditor = lastEditor ?: it.lastEditor
            }

            return ArtistDatabaseEntry.Impl(
                year = dataYear,
                id = artistId.toString(),
                status = status ?: ArtistStatus.UNKNOWN,
                booth = booth,
                name = name.orEmpty(),
                summary = summary,
                socialLinks = socialLinks.orEmpty(),
                storeLinks = storeLinks.orEmpty(),
                portfolioLinks = portfolioLinks.orEmpty(),
                catalogLinks = catalogLinks.orEmpty(),
                driveLink = null,
                notes = notes,
                commissions = commissions.orEmpty(),
                seriesInferred = seriesInferred.orEmpty(),
                seriesConfirmed = seriesConfirmed.orEmpty(),
                merchInferred = merchInferred.orEmpty(),
                merchConfirmed = merchConfirmed.orEmpty(),
                images = images.orEmpty(),
                counter = 0L,
                editorNotes = editorNotes,
                lastEditor = lastEditor,
                lastEditTime = Clock.System.now(),
                verifiedArtist = list.any { it.formTimestamp != null },
            )
        }

        fun applyOver(initial: ArtistDatabaseEntry.Impl, entry: ArtistHistoryEntry) =
            initial.copy(
                status = entry.status ?: initial.status,
                booth = entry.booth ?: initial.booth,
                name = entry.name ?: initial.name,
                summary = entry.summary ?: initial.summary,
                socialLinks = entry.socialLinks ?: initial.socialLinks,
                storeLinks = entry.storeLinks ?: initial.storeLinks,
                portfolioLinks = entry.portfolioLinks ?: initial.portfolioLinks,
                catalogLinks = entry.catalogLinks ?: initial.catalogLinks,
                notes = entry.notes ?: initial.notes,
                commissions = entry.commissions ?: initial.commissions,
                seriesInferred = entry.seriesInferred ?: initial.seriesInferred,
                seriesConfirmed = entry.seriesConfirmed ?: initial.seriesConfirmed,
                merchInferred = entry.merchInferred ?: initial.merchInferred,
                merchConfirmed = entry.merchConfirmed ?: initial.merchConfirmed,
                images = entry.images ?: initial.images,
                editorNotes = entry.editorNotes ?: initial.editorNotes,
                lastEditor = null,
                lastEditTime = Clock.System.now(),
            )
    }
}
