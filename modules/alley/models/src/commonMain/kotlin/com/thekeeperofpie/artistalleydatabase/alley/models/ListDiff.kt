package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable

@Serializable
data class ListDiff<T>(
    val added: List<T>?,
    val deleted: List<T>?,
    val after: List<T>?,
) {
    companion object {
        fun <T> diffList(previous: List<T>?, next: List<T>?) = if (next == null) {
            null
        } else {
            ListDiff(
                added = (next - previous?.toSet().orEmpty()).ifEmpty { null },
                deleted = (previous.orEmpty() - next.toSet()).ifEmpty { null },
                after = next,
            )
        }

        fun <T> diffSet(previous: Set<T>?, next: Set<T>?) = if (next == null) {
            null
        } else {
            ListDiff(
                added = (next - previous.orEmpty()).ifEmpty { null }?.toList(),
                deleted = (previous.orEmpty() - next).ifEmpty { null }?.toList(),
                after = next.toList(),
            )
        }
    }
}
