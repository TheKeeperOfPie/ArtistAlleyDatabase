package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable

@Serializable
data class ListDiff<T>(
    val added: List<T>?,
    val deleted: List<T>?,
) {
    companion object {
        fun <T> diffList(previous: List<T>?, next: List<T>?) = if (next == null) {
            null
        } else {
            ListDiff(
                added = (next - previous?.toSet().orEmpty()).ifEmpty { null },
                deleted = (previous.orEmpty() - next.toSet()).ifEmpty { null },
            )
        }
    }
}
