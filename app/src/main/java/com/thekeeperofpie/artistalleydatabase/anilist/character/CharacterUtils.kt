package com.thekeeperofpie.artistalleydatabase.anilist.character

import com.thekeeperofpie.artistalleydatabase.anilist.DatabaseCharacterEntry

object CharacterUtils {

    fun buildCanonicalName(
        entry: CharacterEntry
    ) = buildCanonicalName(
        first = entry.name?.first,
        middle = entry.name?.middle,
        last = entry.name?.last,
    )

    fun buildCanonicalName(
        entry: DatabaseCharacterEntry
    ) = buildCanonicalName(
        first = entry.name?.first,
        middle = entry.name?.middle,
        last = entry.name?.last,
    )

    fun buildCanonicalName(
        first: String?,
        middle: String?,
        last: String?,
    ) = when {
        !middle.isNullOrBlank() -> {
            when {
                last.isNullOrBlank() -> when {
                    first.isNullOrBlank() -> middle
                    else -> "$first $middle"
                }
                first.isNullOrBlank() -> when {
                    last.isBlank() -> middle
                    else -> "$middle $last"
                }
                else -> "$first $middle $last"
            }
        }
        last.isNullOrBlank() -> first
        first.isNullOrBlank() -> last
        else -> "$last $first"
    }.takeUnless(String?::isNullOrBlank)

    fun buildDisplayName(
        canonicalName: String,
        alternative: List<String>? = null,
    ) = canonicalName + alternative.orEmpty()
        .filterNot(String?::isNullOrBlank)
        .takeUnless(Collection<*>::isEmpty)
        ?.joinToString(prefix = " (", separator = ", ", postfix = ")")
        .orEmpty()
}