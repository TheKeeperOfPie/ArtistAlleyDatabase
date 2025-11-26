package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.AlleyEditDestination
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.AniListType
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

@AssistedInject
class SeriesEditViewModel(
    private val database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
    @Assisted seriesId: Uuid,
    @Assisted private val editInfo: AlleyEditDestination.SeriesEdit?,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saveJob = ExclusiveProgressJob(viewModelScope, ::captureSeriesInfo, ::save)
    private val initialSeries = editInfo?.series
    val state = SeriesEditScreen.State(
        id = savedStateHandle.saveable(
            key = "id",
            saver = EntryForm2.SingleTextState.Saver,
            init = { initialValue(initialSeries?.id, SeriesColumn.CANONICAL) },
        ),
        uuid = savedStateHandle.saveable(
            key = "uuid",
            saver = EntryForm2.SingleTextState.Saver,
            init = { initialValue(seriesId.toString(), SeriesColumn.UUID) },
        ),
        aniListId = savedStateHandle.saveable(
            key = "aniListId",
            saver = EntryForm2.SingleTextState.Saver,
            init = {
                initialValue(initialSeries?.aniListId?.toString(), SeriesColumn.ANILIST_ID)
            },
        ),
        aniListType = savedStateHandle.saveable(
            key = "aniListType",
            saver = EntryForm2.DropdownState.Saver,
            init = {
                val index = initialSeries?.aniListType?.let(AniListType.entries::indexOf)
                    ?: AniListType.NONE.ordinal
                EntryForm2.DropdownState(
                    initialSelectedIndex = index,
                    initialLockState = if (index == AniListType.NONE.ordinal) {
                        EntryLockState.UNLOCKED
                    } else {
                        EntryLockState.LOCKED
                    }
                )
            },
        ),
        wikipediaId = savedStateHandle.saveable(
            key = "wikipediaId",
            saver = EntryForm2.SingleTextState.Saver,
            init = {
                initialValue(
                    initialSeries?.wikipediaId?.toString(),
                    SeriesColumn.WIKIPEDIA_ID
                )
            },
        ),
        source = savedStateHandle.saveable(
            key = "source",
            saver = EntryForm2.DropdownState.Saver,
            init = {
                val index = initialSeries?.source?.let(SeriesSource.entries::indexOf)
                    ?: SeriesSource.NONE.ordinal
                EntryForm2.DropdownState(
                    initialSelectedIndex = index,
                    initialLockState = if (index == SeriesSource.NONE.ordinal) {
                        EntryLockState.UNLOCKED
                    } else {
                        EntryLockState.LOCKED
                    }
                )
            },
        ),
        titleEnglish = savedStateHandle.saveable(
            key = "titleEnglish",
            saver = EntryForm2.SingleTextState.Saver,
            init = { initialValue(initialSeries?.titleEnglish, SeriesColumn.TITLE_ENGLISH) },
        ),
        titleRomaji = savedStateHandle.saveable(
            key = "titleRomaji",
            saver = EntryForm2.SingleTextState.Saver,
            init = { initialValue(initialSeries?.titleRomaji, SeriesColumn.TITLE_ROMAJI) },
        ),
        titleNative = savedStateHandle.saveable(
            key = "titleNative",
            saver = EntryForm2.SingleTextState.Saver,
            init = { initialValue(initialSeries?.titleNative, SeriesColumn.TITLE_NATIVE) },
        ),
        titlePreferred = savedStateHandle.saveable(
            key = "titlePreferred",
            saver = EntryForm2.SingleTextState.Saver,
            init = { initialValue(initialSeries?.titlePreferred, SeriesColumn.TITLE_PREFERRED) },
        ),
        link = savedStateHandle.saveable(
            key = "link",
            saver = EntryForm2.SingleTextState.Saver,
            init = { initialValue(initialSeries?.link.orEmpty(), SeriesColumn.EXTERNAL_LINK) },
        ),
        notes = savedStateHandle.saveable(
            key = "notes",
            saver = EntryForm2.SingleTextState.Saver,
            init = { initialValue(initialSeries?.notes.orEmpty(), SeriesColumn.NOTES) },
        ),
        savingState = saveJob.state,
    )

    // TODO: Refresh list screen after save
    fun onClickSave() = saveJob.launch()

    private fun initialValue(
        value: String?,
        seriesColumn: SeriesColumn,
    ): EntryForm2.SingleTextState {
        val isSelectedColumn = editInfo?.seriesColumn == seriesColumn
        return EntryForm2.SingleTextState(
            value = TextFieldState(
                initialText = value.orEmpty(),
                initialSelection = TextRange(0, if (isSelectedColumn) value.orEmpty().length else 0)
            ),
            initialLockState = if (value.isNullOrBlank() || isSelectedColumn) {
                EntryLockState.UNLOCKED
            } else {
                EntryLockState.LOCKED
            }
        )
    }

    private fun captureSeriesInfo(): SeriesInfo {
        // TODO: Apply error validation from UI
        val id = state.id.value.text.toString()
        val uuid = state.uuid.value.text.toString()
        val notes = state.notes.value.text.toString()
        val aniListId = state.aniListId.value.text.toString()
        val aniListType = AniListType.entries[state.aniListType.selectedIndex]
        val wikipediaId = state.wikipediaId.value.text.toString()
        val source = SeriesSource.entries[state.source.selectedIndex]
        val titlePreferred = state.titlePreferred.value.text.toString()
        val titleEnglish = state.titleEnglish.value.text.toString()
        val titleRomaji = state.titleRomaji.value.text.toString()
        val titleNative = state.titleNative.value.text.toString()
        val link = state.link.value.text.toString()
        return SeriesInfo(
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
        )
    }

    private suspend fun save(seriesInfo: SeriesInfo) = withContext(dispatchers.io) {
        database.saveSeries(initial = initialSeries, updated = seriesInfo)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            seriesId: Uuid,
            editInfo: AlleyEditDestination.SeriesEdit?,
            savedStateHandle: SavedStateHandle,
        ): SeriesEditViewModel
    }
}
