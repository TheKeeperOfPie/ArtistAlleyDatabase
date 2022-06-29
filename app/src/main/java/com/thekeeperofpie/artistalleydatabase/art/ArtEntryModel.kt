package com.thekeeperofpie.artistalleydatabase.art

import android.content.Context

class ArtEntryModel(
    context: Context,
    val value: ArtEntry,
) {
    val localImageFile = context.filesDir.resolve("entry_images/${value.id}")
        .takeIf { it.exists() }

    val placeholderText = if (localImageFile != null) "" else value.run {
        val prefix = when (val source = SourceType.fromEntry(value)) {
            is SourceType.Convention -> (source.name + (source.year?.let { " $it" }
                ?: "") + "\n" + source.hall + " " + source.booth).trim()
            is SourceType.Custom -> source.value
            is SourceType.Online -> source.name
            SourceType.Unknown -> ""
        }

        val suffix = if (artists.isNotEmpty()) {
            artists.joinToString("\n")
        } else if (series.isNotEmpty()) {
            series.joinToString("\n")
        } else if (characters.isNotEmpty()) {
            characters.joinToString("\n")
        } else if (tags.isNotEmpty()) {
            tags.take(10).joinToString("\n")
        } else ""

        if (prefix.isNotBlank() || suffix.isNotBlank()) {
            prefix + "\n" + suffix
        } else id
    }
}