package com.thekeeperofpie.artistalleydatabase.alley.edit.merch

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.launch
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
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
    @Assisted val initialMerch: MerchInfo?,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saveJob = ExclusiveProgressJob(viewModelScope, ::save)
    private val deleteJob = ExclusiveProgressJob(viewModelScope, ::delete)
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
        categoriesState = savedStateHandle.saveable(
            key = "categoriesState",
            saver = EntryForm2.SingleTextState.Saver,
        ) {
            EntryForm2.SingleTextState(
                initialLockState = if (initialMerch?.categories?.isNotEmpty() == true) {
                    EntryLockState.LOCKED
                } else {
                    EntryLockState.UNLOCKED
                }
            )
        },
        categories = savedStateHandle.saveable(
            key = "categories",
            saver = StateUtils.snapshotListJsonSaver(),
        ) { initialMerch?.categories.orEmpty().toMutableStateList() },
        notes = savedStateHandle.saveable(
            key = "notes",
            saver = EntryForm2.SingleTextState.Saver,
            init = { EntryForm2.SingleTextState.fromValue(initialMerch?.notes.orEmpty()) },
        ),
        savingState = saveJob.state,
        deleteProgress = deleteJob.state,
    )

    // TODO: Refresh list screen after save
    fun onClickSave() = saveJob.launch(::captureMerchInfo)
    fun onConfirmDelete() = deleteJob.launch()

    private fun captureMerchInfo(): MerchInfo {
        val id = state.id.value.text.toString()
        val categories = state.categories +
                listOfNotNull(state.categoriesState.value.text.toString().ifEmpty { null })
        val uuid = state.uuid.value.text.toString()
        val notes = state.notes.value.text.toString()
        return MerchInfo(
            name = id,
            categories = categories.toSet(),
            uuid = Uuid.parse(uuid),
            notes = notes,
        )
    }

    private suspend fun save(merchInfo: MerchInfo) = withContext(dispatchers.io) {
        database.saveMerch(initial = initialMerch, updated = merchInfo)
    }

    private suspend fun delete() = withContext(dispatchers.io) {
        database.deleteMerch(initialMerch!!)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            merchId: Uuid,
            initialMerch: MerchInfo?,
            savedStateHandle: SavedStateHandle,
        ): MerchEditViewModel
    }
}
