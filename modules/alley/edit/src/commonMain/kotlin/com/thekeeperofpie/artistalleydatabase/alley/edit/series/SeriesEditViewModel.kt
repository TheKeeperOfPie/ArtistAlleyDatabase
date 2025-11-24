package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.uuid.Uuid

@AssistedInject
class SeriesEditViewModel(
    @Assisted seriesId: Uuid,
    @Assisted series: SeriesInfo?,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val mode: SeriesEditScreen.Mode = if (series == null) {
        SeriesEditScreen.Mode.ADD
    } else {
        SeriesEditScreen.Mode.EDIT
    }

    val state = SeriesEditScreen.State(
        id = savedStateHandle.saveable(
            key = "id",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(series?.id) },
        ),
        uuid = savedStateHandle.saveable(
            key = "uuid",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(seriesId.toString()) },
        ),
        aniListId = savedStateHandle.saveable(
            key = "aniListId",
            saver = EntryForm2.SingleTextState.Saver,
            init = {
                EntryForm2.SingleTextState(
                    TextFieldState(series?.aniListId?.toString().orEmpty())
                )
            },
        ),
        aniListType = savedStateHandle.saveable(
            key = "aniListType",
            saver = EntryForm2.SingleTextState.Saver,
            // TODO: Use enum for type
            init = { EntryForm2.SingleTextState.fromValue(series?.aniListType.orEmpty()) },
        ),
        wikipediaId = savedStateHandle.saveable(
            key = "id",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(series?.id) },
        ),
        source = savedStateHandle.saveable(
            key = "source",
            saver = EntryForm2.DropdownState.Saver,
            init = {
                EntryForm2.DropdownState(
                    series?.source?.let(SeriesSource.entries::indexOf) ?: SeriesSource.NONE.ordinal
                )
            },
        ),
        titleEnglish = savedStateHandle.saveable(
            key = "titleEnglish",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(series?.titleEnglish) },
        ),
        titleRomaji = savedStateHandle.saveable(
            key = "titleRomaji",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(series?.titleRomaji) },
        ),
        titleNative = savedStateHandle.saveable(
            key = "titleNative",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(series?.titleNative) },
        ),
        titlePreferred = savedStateHandle.saveable(
            key = "titlePreferred",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(series?.titlePreferred) },
        ),
        link = savedStateHandle.saveable(
            key = "link",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(series?.link.orEmpty()) },
        ),
        notes = savedStateHandle.saveable(
            key = "notes",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(series?.notes.orEmpty()) },
        ),
    )

    fun onClickSave() {
        // TODO
    }

    @AssistedFactory
    interface Factory {
        fun create(
            seriesId: Uuid,
            series: SeriesInfo?,
            savedStateHandle: SavedStateHandle,
        ): SeriesEditViewModel
    }
}
