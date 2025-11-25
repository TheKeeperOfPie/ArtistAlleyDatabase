package com.thekeeperofpie.artistalleydatabase.alley.edit.merch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@AssistedInject
class MerchEditViewModel(
    private val database: AlleyEditDatabase,
    @Assisted merchId: Uuid,
    @Assisted private val initialMerch: MerchInfo?,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saved = savedStateHandle.getMutableStateFlow<Boolean?>("saved", null)
    val state = MerchEditScreen.State(
        id = savedStateHandle.saveable(
            key = "id",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(initialMerch?.name) },
        ),
        uuid = savedStateHandle.saveable(
            key = "uuid",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(merchId.toString()) },
        ),
        notes = savedStateHandle.saveable(
            key = "notes",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(initialMerch?.notes.orEmpty()) },
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
        state.saved.value = false
        viewModelScope.launch {
            database.saveMerch(
                initial = initialMerch,
                updated = MerchInfo(
                    name = id,
                    uuid = Uuid.parse(uuid),
                    notes = notes,
                ),
            )
            saved.value = true
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            merchId: Uuid,
            merch: MerchInfo?,
            savedStateHandle: SavedStateHandle,
        ): MerchEditViewModel
    }
}
