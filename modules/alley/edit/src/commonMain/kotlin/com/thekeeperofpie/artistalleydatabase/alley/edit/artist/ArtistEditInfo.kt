package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import kotlin.uuid.Uuid

data class ArtistEditInfo(
    val id: Uuid,
    val booth: String?,
    val name: String,
    val summary: String?,
    val links: List<String>,
    val storeLinks: List<String>,
    val catalogLinks: List<String>,
    val notes: String?,
    val commissions: List<String>,
    val seriesInferred: List<String>,
    val seriesConfirmed: List<String>,
    val merchInferred: List<String>,
    val merchConfirmed: List<String>,
)
