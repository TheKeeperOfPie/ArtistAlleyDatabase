package com.thekeeperofpie.artistalleydatabase.art.grid

import android.content.Context
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.SourceType

class ArtEntryGridModel(
    context: Context,
    val value: ArtEntry,
) {
    val localImageFile = context.filesDir.resolve("entry_images/${value.id}")
        .takeIf { it.exists() }

    val placeholderText = if (localImageFile != null) "" else value.run {
        val source = when (val source = SourceType.fromEntry(value)) {
            is SourceType.Convention -> (source.name + (source.year?.let { " $it" }
                ?: "") + "\n" + source.hall + " " + source.booth).trim()
            is SourceType.Custom -> source.value
            is SourceType.Online -> source.name
            SourceType.Unknown -> ""
        }

        val info = if (artists.isNotEmpty()) {
            artists.joinToString("\n")
        } else if (series.isNotEmpty()) {
            series.joinToString("\n")
        } else if (characters.isNotEmpty()) {
            characters.joinToString("\n")
        } else if (tags.isNotEmpty()) {
            tags.take(10).joinToString("\n")
        } else ""

        val pieces = listOf(source, info, notes)
        if (pieces.any { !it.isNullOrBlank() }) {
            pieces.joinToString("\n")
        } else id
    }
}