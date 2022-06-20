package com.thekeeperofpie.artistalleydatabase.art

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.R

abstract class ArtEntryViewModel : ViewModel() {

    val artistSection = ArtEntrySection.MultiText(
        R.string.add_entry_artists_header_zero,
        R.string.add_entry_artists_header_one,
        R.string.add_entry_artists_header_many,
    )
    val locationSection = ArtEntrySection.MultiText(
        R.string.add_entry_locations_header_zero,
        R.string.add_entry_locations_header_one,
        R.string.add_entry_locations_header_many,
    )
    val seriesSection = ArtEntrySection.MultiText(
        R.string.add_entry_series_header_zero,
        R.string.add_entry_series_header_one,
        R.string.add_entry_series_header_many,
    )
    val characterSection = ArtEntrySection.MultiText(
        R.string.add_entry_characters_header_zero,
        R.string.add_entry_characters_header_one,
        R.string.add_entry_characters_header_many,
    )
    val tagSection = ArtEntrySection.MultiText(
        R.string.add_entry_tags_header_zero,
        R.string.add_entry_tags_header_one,
        R.string.add_entry_tags_header_many,
    )

    val printSizeSection = SizeDropdown()

    val sections = listOf(
        artistSection,
        locationSection,
        seriesSection,
        characterSection,
        printSizeSection,
        tagSection,
    )

    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    fun onImageSizeResult(width: Int, height: Int) {
        if (width > height) {
            PrintSize.LANDSCAPES.forEachIndexed { index, printSize ->
                printSizeSection.options[index] =
                    ArtEntrySection.Dropdown.Item.Basic(printSize, printSize.textRes)
            }
        } else {
            PrintSize.PORTRAITS.forEachIndexed { index, printSize ->
                printSizeSection.options[index] =
                    ArtEntrySection.Dropdown.Item.Basic(printSize, printSize.textRes)
            }
        }
    }
}