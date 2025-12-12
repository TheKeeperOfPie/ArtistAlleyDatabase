package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

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
    }
}
