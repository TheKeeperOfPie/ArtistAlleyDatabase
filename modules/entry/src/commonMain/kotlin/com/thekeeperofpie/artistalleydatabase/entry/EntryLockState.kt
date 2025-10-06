package com.thekeeperofpie.artistalleydatabase.entry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.ui.graphics.vector.ImageVector
import artistalleydatabase.modules.entry.generated.resources.Res
import artistalleydatabase.modules.entry.generated.resources.lock_state_different_content_description
import artistalleydatabase.modules.entry.generated.resources.lock_state_locked_content_description
import artistalleydatabase.modules.entry.generated.resources.lock_state_unlocked_content_description
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
import org.jetbrains.compose.resources.StringResource

enum class EntryLockState(
    val editable: Boolean,
    val icon: ImageVector,
    val contentDescription: StringResource,
) {
    LOCKED(
        editable = false,
        icon = Icons.Default.Lock,
        contentDescription = Res.string.lock_state_locked_content_description
    ),
    UNLOCKED(
        editable = true,
        icon = Icons.Default.LockOpen,
        contentDescription = Res.string.lock_state_unlocked_content_description
    ),
    DIFFERENT(
        editable = true,
        icon = Icons.Default.LockReset,
        contentDescription = Res.string.lock_state_different_content_description
    ),
    ;

    fun toSerializedValue() = when (this) {
        LOCKED -> true
        UNLOCKED -> false
        DIFFERENT -> null
    }

    fun rotateLockState(wasEverDifferent: Boolean) = when (this) {
        LOCKED -> UNLOCKED
        UNLOCKED -> if (wasEverDifferent) DIFFERENT else LOCKED
        DIFFERENT -> LOCKED
    }

    companion object {
        fun from(value: Boolean?) = value?.let {
            if (it) LOCKED else UNLOCKED
        } ?: DIFFERENT

        val Saver = StateUtils.nullableEnumSaver<EntryLockState>()
    }
}
