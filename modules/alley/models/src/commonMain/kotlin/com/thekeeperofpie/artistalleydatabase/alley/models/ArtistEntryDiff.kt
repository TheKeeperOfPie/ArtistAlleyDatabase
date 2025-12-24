package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable

@Serializable
data class ArtistEntryDiff(
    val booth: String?,
    val name: String?,
    val summary: String?,
    val notes: String?,
    val links: Diff?,
    val storeLinks: Diff?,
    val catalogLinks: Diff?,
    val commissions: Diff?,
    val seriesInferred: Diff?,
    val seriesConfirmed: Diff?,
    val merchInferred: Diff?,
    val merchConfirmed: Diff?,
) {
    companion object {
        fun diffList(previous: List<String>?, next: List<String>?) = if (next == null) {
            null
        } else {
            Diff(
                added = (next - previous?.toSet().orEmpty()).ifEmpty { null },
                removed = (previous.orEmpty() - next.toSet()).ifEmpty { null },
            )
        }
    }

    @Serializable
    data class Diff(
        val added: List<String>?,
        val removed: List<String>?,
    )
}
