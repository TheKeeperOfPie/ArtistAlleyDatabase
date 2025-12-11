package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import kotlinx.serialization.Serializable
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
    val lastEditTime: Instant,
)
