package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ArtistEntryDiff(
    val images: ListDiff<DatabaseImage>?,
    val booth: String?,
    val name: String?,
    val summary: String?,
    val notes: String?,
    val socialLinks: ListDiff<String>?,
    val storeLinks: ListDiff<String>?,
    val portfolioLinks: ListDiff<String>?,
    val catalogLinks: ListDiff<String>?,
    val commissions: ListDiff<String>?,
    val seriesInferred: ListDiff<String>?,
    val seriesConfirmed: ListDiff<String>?,
    val merchInferred: ListDiff<String>?,
    val merchConfirmed: ListDiff<String>?,
    val formNotes: String,
    val timestamp: Instant,
)
