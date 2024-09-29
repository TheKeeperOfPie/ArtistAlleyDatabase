package com.thekeeperofpie.artistalleydatabase.art.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import artistalleydatabase.modules.art.generated.resources.Res
import artistalleydatabase.modules.art.generated.resources.art_entry_artists_header_many
import artistalleydatabase.modules.art.generated.resources.art_entry_artists_header_one
import artistalleydatabase.modules.art.generated.resources.art_entry_artists_header_zero
import artistalleydatabase.modules.art.generated.resources.art_entry_characters_header_many
import artistalleydatabase.modules.art.generated.resources.art_entry_characters_header_one
import artistalleydatabase.modules.art.generated.resources.art_entry_characters_header_zero
import artistalleydatabase.modules.art.generated.resources.art_entry_locations_header_many
import artistalleydatabase.modules.art.generated.resources.art_entry_locations_header_one
import artistalleydatabase.modules.art.generated.resources.art_entry_locations_header_zero
import artistalleydatabase.modules.art.generated.resources.art_entry_series_header_many
import artistalleydatabase.modules.art.generated.resources.art_entry_series_header_one
import artistalleydatabase.modules.art.generated.resources.art_entry_series_header_zero
import artistalleydatabase.modules.art.generated.resources.art_entry_tags_header_many
import artistalleydatabase.modules.art.generated.resources.art_entry_tags_header_one
import artistalleydatabase.modules.art.generated.resources.art_entry_tags_header_zero
import com.thekeeperofpie.artistalleydatabase.art.sections.PrintSizeDropdown
import com.thekeeperofpie.artistalleydatabase.entry.EntryForm
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

class SampleArtEntrySectionsProvider : PreviewParameterProvider<List<EntrySection>> {
    override val values = sequenceOf(
        listOf(
            EntrySection.MultiText(
                Res.string.art_entry_artists_header_zero,
                Res.string.art_entry_artists_header_one,
                Res.string.art_entry_artists_header_many,
                "Lucidsky"
            ),
            EntrySection.MultiText(
                Res.string.art_entry_locations_header_zero,
                Res.string.art_entry_locations_header_one,
                Res.string.art_entry_locations_header_many,
                "Fanime 2022"
            ),
            EntrySection.MultiText(
                Res.string.art_entry_series_header_zero,
                Res.string.art_entry_series_header_one,
                Res.string.art_entry_series_header_many,
                "Dress Up Darling"
            ),
            EntrySection.MultiText(
                Res.string.art_entry_characters_header_zero,
                Res.string.art_entry_characters_header_one,
                Res.string.art_entry_characters_header_many,
                "Marin Kitagawa"
            ),
            PrintSizeDropdown().apply {
                selectedIndex = options.size - 1
            },
            EntrySection.MultiText(
                Res.string.art_entry_tags_header_zero,
                Res.string.art_entry_tags_header_one,
                Res.string.art_entry_tags_header_many,
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
    @PreviewParameter(SampleArtEntrySectionsProvider::class) sections: List<EntrySection>,
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
            onAnySectionFocused = {},
        )
    }
}
