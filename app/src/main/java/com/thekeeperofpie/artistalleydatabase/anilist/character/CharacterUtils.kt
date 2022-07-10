package com.thekeeperofpie.artistalleydatabase.anilist.character

object CharacterUtils {

    fun buildCanonicalName(
        entry: CharacterEntry
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
                last == null -> "$first $middle"
                first == null -> "$middle $last"
                else -> "$first $middle $last"
            }
        }
        last == null -> first
        first == null -> last
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