package com.thekeeperofpie.artistalleydatabase.art.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.thekeeperofpie.artistalleydatabase.art.R
import com.thekeeperofpie.artistalleydatabase.art.sections.PrintSizeDropdown
import com.thekeeperofpie.artistalleydatabase.entry.EntryForm
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection

class SampleArtEntrySectionsProvider : PreviewParameterProvider<List<EntrySection>> {
    override val values = sequenceOf(
        listOf(
            EntrySection.MultiText(
                R.string.art_entry_artists_header_zero,
                R.string.art_entry_artists_header_one,
                R.string.art_entry_artists_header_many,
                "Lucidsky"
            ),
            EntrySection.MultiText(
                R.string.art_entry_locations_header_zero,
                R.string.art_entry_locations_header_one,
                R.string.art_entry_locations_header_many,
                "Fanime 2022"
            ),
            EntrySection.MultiText(
                R.string.art_entry_series_header_zero,
                R.string.art_entry_series_header_one,
                R.string.art_entry_series_header_many,
                "Dress Up Darling"
            ),
            EntrySection.MultiText(
                R.string.art_entry_characters_header_zero,
                R.string.art_entry_characters_header_one,
                R.string.art_entry_characters_header_many,
                "Marin Kitagawa"
            ),
            PrintSizeDropdown().apply {
                selectedIndex = options.size - 1
            },
            EntrySection.MultiText(
                R.string.art_entry_tags_header_zero,
                R.string.art_entry_tags_header_one,
                R.string.art_entry_tags_header_many,
            ).apply {
                setContents(
                    entries = listOf(
                        EntrySection.MultiText.Entry.Custom("cute"),
                        EntrySection.MultiText.Entry.Custom("portrait")
                    ),
                    lockState = null,
                )
                pendingValue = pendingValue.copy("schoolgirl uniform")
            },
        )
    )
}

@Preview
@Composable
fun Preview(
    @PreviewParameter(SampleArtEntrySectionsProvider::class) sections: List<EntrySection>
) {
    Column {
        EntryForm(
            sections = {
                sections.apply {
                    (first() as EntrySection.MultiText).lockState =
                        EntrySection.LockState.LOCKED
                }
            },
            onNavigate = {},
        )
    }
}
