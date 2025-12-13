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
    val links: List<String>?,
    val storeLinks: List<String>?,
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
) {
    companion object {
        fun create(before: ArtistDatabaseEntry?, after: ArtistDatabaseEntry) = ArtistHistoryEntry(
            status = after.status.takeIf { it != before?.status },
            booth = after.booth.takeIf { it != before?.booth }
                ?.ifBlank { null },
            name = after.name.takeIf { it != before?.name }
                ?.ifBlank { null },
            summary = after.summary.takeIf { it != before?.summary }
                ?.ifBlank { null },
            links = after.links.takeIf { it != before?.links }
                ?.ifEmpty { null },
            storeLinks = after.storeLinks.takeIf { it != before?.storeLinks }
                ?.ifEmpty { null },
            catalogLinks = after.catalogLinks.takeIf { it != before?.catalogLinks }
                ?.ifEmpty { null },
            notes = after.notes.takeIf { it != before?.notes }
                ?.ifBlank { null },
            commissions = after.commissions.takeIf { it != before?.commissions }
                ?.ifEmpty { null },
            seriesInferred = after.seriesInferred.takeIf { it != before?.seriesInferred }
                ?.ifEmpty { null },
            seriesConfirmed = after.seriesConfirmed.takeIf { it != before?.seriesConfirmed }
                ?.ifEmpty { null },
            merchInferred = after.merchInferred.takeIf { it != before?.merchInferred }
                ?.ifEmpty { null },
            merchConfirmed = after.merchConfirmed.takeIf { it != before?.merchConfirmed }
                ?.ifEmpty { null },
            images = after.images.takeIf { it != before?.images }
                ?.ifEmpty { null },
            editorNotes = after.editorNotes.takeIf { it != before?.editorNotes }
                ?.ifBlank { null },
            lastEditor = after.lastEditor,
            timestamp = after.lastEditTime ?: Clock.System.now(),
        )

        fun rebuild(dataYear: DataYear, artistId: Uuid, list: List<ArtistHistoryEntry>): ArtistDatabaseEntry.Impl {
            var status: ArtistStatus? = null
            var booth: String? = null
            var name: String? = null
            var summary: String? = null
            var links: List<String>? = null
            var storeLinks: List<String>? = null
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
                links = links ?: it.links
                storeLinks = storeLinks ?: it.storeLinks
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
                booth = booth ,
                name = name.orEmpty(),
                summary = summary,
                links = links.orEmpty(),
                storeLinks = storeLinks.orEmpty(),
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
            )
        }
    }
}
