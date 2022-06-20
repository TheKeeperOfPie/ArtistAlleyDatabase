package com.thekeeperofpie.artistalleydatabase.art

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.R

abstract class ArtEntryViewModel : ViewModel() {

    val artistSection = ArtEntrySection.MultiText(
        R.string.art_entry_artists_header_zero,
        R.string.art_entry_artists_header_one,
        R.string.art_entry_artists_header_many,
    )
    val seriesSection = ArtEntrySection.MultiText(
        R.string.art_entry_series_header_zero,
        R.string.art_entry_series_header_one,
        R.string.art_entry_series_header_many,
    )
    val characterSection = ArtEntrySection.MultiText(
        R.string.art_entry_characters_header_zero,
        R.string.art_entry_characters_header_one,
        R.string.art_entry_characters_header_many,
    )
    val tagSection = ArtEntrySection.MultiText(
        R.string.art_entry_tags_header_zero,
        R.string.art_entry_tags_header_one,
        R.string.art_entry_tags_header_many,
    )

    val printSizeSection = PrintSizeDropdown()

    val sourceSection = SourceDropdown()

    val sections = listOf(
        artistSection,
        sourceSection,
        seriesSection,
        characterSection,
        printSizeSection,
        tagSection,
    )

    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    fun onImageSizeResult(width: Int, height: Int) {
        printSizeSection.onSizeChange(width, height)
    }
}