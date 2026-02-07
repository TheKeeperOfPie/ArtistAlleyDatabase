package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable

@Serializable
data class HistoryListDiff(
    val added: List<String>?,
    val deleted: List<String>?,
) {
    companion object {
        fun diffList(previous: List<String>?, next: List<String>?) = if (next == null) {
            null
        } else {
            HistoryListDiff(
                added = (next - previous?.toSet().orEmpty()).ifEmpty { null },
                deleted = (previous.orEmpty() - next.toSet()).ifEmpty { null },
            )
        }
    }
}
