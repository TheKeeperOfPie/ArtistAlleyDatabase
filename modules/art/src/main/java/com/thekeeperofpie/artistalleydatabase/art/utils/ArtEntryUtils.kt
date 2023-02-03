package com.thekeeperofpie.artistalleydatabase.art.utils

import android.content.Context
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.sections.SourceType
import com.thekeeperofpie.artistalleydatabase.form.EntryUtils

object ArtEntryUtils {

    const val TYPE_ID = "art_entry_images"

    fun getImageFile(context: Context, entryId: String) =
        EntryUtils.getImageFile(context, TYPE_ID, entryId)

    fun buildPlaceholderText(appJson: AppJson, entry: ArtEntry) = entry.run {
        val source = when (val source = SourceType.fromEntry(appJson.json, this)) {
            is SourceType.Convention -> (source.name + (source.year?.let { " $it" }
                ?: "") + "\n" + source.hall + " " + source.booth).trim()
            is SourceType.Custom -> source.value
            is SourceType.Online -> source.name
            SourceType.Unknown,
            SourceType.Different -> ""
        }

        val series = series(appJson)
        val characters = characters(appJson)
        val info = if (artists.isNotEmpty()) {
            artists.joinToString("\n")
        } else if (series.isNotEmpty()) {
            series.joinToString("\n") { it.text }
        } else if (characters.isNotEmpty()) {
            characters.joinToString("\n") { it.text }
        } else if (tags.isNotEmpty()) {
            tags.take(10).joinToString("\n")
        } else ""

        val pieces = listOf(source, info, notes)
        if (pieces.any { !it.isNullOrBlank() }) {
            pieces.joinToString("\n")
        } else id
    }
}