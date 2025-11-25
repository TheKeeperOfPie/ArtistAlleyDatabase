package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@AssistedInject
class SeriesEditViewModel(
    private val database: AlleyEditDatabase,
    @Assisted seriesId: Uuid,
    @Assisted private val initialSeries: SeriesInfo?,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saved = savedStateHandle.getMutableStateFlow<Boolean?>("saved", null)
    val state = SeriesEditScreen.State(
        id = savedStateHandle.saveable(
            key = "id",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(initialSeries?.id) },
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
                EntryForm2.SingleTextState.fromValue(initialSeries?.aniListId?.toString())
            },
        ),
        aniListType = savedStateHandle.saveable(
            key = "aniListType",
            saver = EntryForm2.SingleTextState.Saver,
            // TODO: Use enum for type
            init = { EntryForm2.SingleTextState.fromValue(initialSeries?.aniListType.orEmpty()) },
        ),
        wikipediaId = savedStateHandle.saveable(
            key = "wikipediaId",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(initialSeries?.wikipediaId?.toString()) },
        ),
        source = savedStateHandle.saveable(
            key = "source",
            saver = EntryForm2.DropdownState.Saver,
            init = {
                EntryForm2.DropdownState(
                    initialSeries?.source?.let(SeriesSource.entries::indexOf)
                        ?: SeriesSource.NONE.ordinal
                )
            },
        ),
        titleEnglish = savedStateHandle.saveable(
            key = "titleEnglish",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(initialSeries?.titleEnglish) },
        ),
        titleRomaji = savedStateHandle.saveable(
            key = "titleRomaji",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(initialSeries?.titleRomaji) },
        ),
        titleNative = savedStateHandle.saveable(
            key = "titleNative",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(initialSeries?.titleNative) },
        ),
        titlePreferred = savedStateHandle.saveable(
            key = "titlePreferred",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(initialSeries?.titlePreferred) },
        ),
        link = savedStateHandle.saveable(
            key = "link",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(initialSeries?.link.orEmpty()) },
        ),
        notes = savedStateHandle.saveable(
            key = "notes",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(initialSeries?.notes.orEmpty()) },
        ),
        saved = saved,
    )

    // TODO: Saving indicator and exit on done
    // TODO: Refresh list screen after save
    fun onClickSave() {
        // TODO: Apply error validation from UI
        val id = state.id.value.text.toString()
        val uuid = state.uuid.value.text.toString()
        val notes = state.notes.value.text.toString()
        val aniListId = state.aniListId.value.text.toString()
        val aniListType = state.aniListType.value.text.toString()
        val wikipediaId = state.wikipediaId.value.text.toString()
        val source = SeriesSource.entries[state.source.selectedIndex]
        val titlePreferred = state.titlePreferred.value.text.toString()
        val titleEnglish = state.titleEnglish.value.text.toString()
        val titleRomaji = state.titleRomaji.value.text.toString()
        val titleNative = state.titleNative.value.text.toString()
        val link = state.link.value.text.toString()
        state.saved.value = false
        viewModelScope.launch {
            database.saveSeries(
                initial = initialSeries,
                updated = SeriesInfo(
                    id = id,
                    uuid = Uuid.parse(uuid),
                    notes = notes,
                    aniListId = aniListId.ifBlank { null }?.toLong(),
                    aniListType = aniListType,
                    wikipediaId = wikipediaId.ifBlank { null }?.toLong(),
                    source = source,
                    titlePreferred = titlePreferred,
                    titleEnglish = titleEnglish,
                    titleRomaji = titleRomaji,
                    titleNative = titleNative,
                    link = link,
                ),
            )
            saved.value = true
        }
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
