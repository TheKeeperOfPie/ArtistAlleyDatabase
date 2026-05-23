package com.thekeeperofpie.artistalleydatabase.alley.edit.form

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateSet
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll

internal object FormUtils {
    fun <T> applyValue(
        state: EntryForm2.State,
        list: SnapshotStateList<T>,
        value: List<T>,
        mergeBehavior: FormMergeBehavior,
    ) {
        when (mergeBehavior) {
            FormMergeBehavior.APPEND -> {
                if (value.isNotEmpty()) {
                    list.replaceAll((list.toList() + value).distinct())
                }
            }
            FormMergeBehavior.REPLACE -> {
                list.replaceAll(value)
                state.lockState = if (value.isEmpty()) {
                    EntryLockState.UNLOCKED
                } else {
                    EntryLockState.LOCKED
                }
            }
            FormMergeBehavior.IGNORE -> {
                if (list.isEmpty()) {
                    list.replaceAll(value)
                    if (value.isNotEmpty()) {
                        state.lockState = EntryLockState.LOCKED
                    }
                }
            }
        }
    }

    fun <T> applyValue(
        state: EntryForm2.State,
        set: SnapshotStateSet<T>,
        value: Set<T>,
        mergeBehavior: FormMergeBehavior,
    ) {
        when (mergeBehavior) {
            FormMergeBehavior.APPEND -> {
                if (value.isNotEmpty()) {
                    set.replaceAll(set.toSet() + value)
                }
            }
            FormMergeBehavior.REPLACE -> {
                set.replaceAll(value)
                state.lockState = if (value.isEmpty()) {
                    EntryLockState.UNLOCKED
                } else {
                    EntryLockState.LOCKED
                }
            }
            FormMergeBehavior.IGNORE -> {
                if (set.isEmpty()) {
                    set.replaceAll(value)
                    if (value.isNotEmpty()) {
                        state.lockState = EntryLockState.LOCKED
                    }
                }
            }
        }
    }

    fun applyValue(
        state: EntryForm2.SingleTextState,
        value: String?,
        mergeBehavior: FormMergeBehavior,
    ) {
        val valueOrEmpty = value.orEmpty()
        when (mergeBehavior) {
            // For non-lists, both append and replace do a replace
            FormMergeBehavior.APPEND,
            FormMergeBehavior.REPLACE -> {
                state.value.setTextAndPlaceCursorAtEnd(valueOrEmpty)
            }
            FormMergeBehavior.IGNORE -> {
                if (state.value.text.isEmpty()) {
                    state.value.setTextAndPlaceCursorAtEnd(valueOrEmpty)
                }
            }
        }

        if (valueOrEmpty.isNotBlank()) {
            state.lockState = EntryLockState.LOCKED
        }
    }
}
