package com.thekeeperofpie.artistalleydatabase.alley.edit.merch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

@AssistedInject
class MerchEditViewModel(
    private val database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
    @Assisted merchId: Uuid,
    @Assisted private val initialMerch: MerchInfo?,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saveJob = ExclusiveProgressJob(viewModelScope, ::save)
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
        savingState = saveJob.state,
    )

    // TODO: Refresh list screen after save
    fun onClickSave() = saveJob.launch(::captureMerchInfo)

    private fun captureMerchInfo(): MerchInfo {
        val id = state.id.value.text.toString()
        val uuid = state.uuid.value.text.toString()
        val notes = state.notes.value.text.toString()
        return MerchInfo(
            name = id,
            uuid = Uuid.parse(uuid),
            notes = notes,
        )
    }

    private suspend fun save(merchInfo: MerchInfo) = withContext(dispatchers.io) {
        database.saveMerch(initial = initialMerch, updated = merchInfo)
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
