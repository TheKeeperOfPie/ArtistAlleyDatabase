package com.thekeeperofpie.artistalleydatabase.art

import android.content.Context
import kotlinx.serialization.json.Json

object ArtEntryUtils {

    fun getImageFile(context: Context, id: String) = context.filesDir
        .resolve("art_entry_images/${id}")

    fun buildPlaceholderText(json: Json, entry: ArtEntry) = entry.run {
        val source = when (val source = SourceType.fromEntry(json, this)) {
            is SourceType.Convention -> (source.name + (source.year?.let { " $it" }
                ?: "") + "\n" + source.hall + " " + source.booth).trim()
            is SourceType.Custom -> source.value
            is SourceType.Online -> source.name
            SourceType.Unknown,
            SourceType.Different -> ""
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