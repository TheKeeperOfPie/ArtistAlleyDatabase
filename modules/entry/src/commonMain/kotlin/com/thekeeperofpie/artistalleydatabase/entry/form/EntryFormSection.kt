package com.thekeeperofpie.artistalleydatabase.entry.form

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import artistalleydatabase.modules.entry.generated.resources.Res
import artistalleydatabase.modules.entry.generated.resources.lock_state_different_content_description
import artistalleydatabase.modules.entry.generated.resources.lock_state_locked_content_description
import artistalleydatabase.modules.entry.generated.resources.lock_state_unlocked_content_description
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

sealed class EntryFormSection(val initialLockState: LockState? = null) {

    abstract fun clearSection()

    class MultiText(
        content: List<Entry> = emptyList(),
        pendingNewValue: String = "",
        lockState: LockState? = LockState.UNLOCKED,
        initialLockState: LockState? = lockState,
    ) : EntryFormSection(initialLockState) {
        val content = content.toMutableStateList()
        var pendingNewValue by mutableStateOf(TextFieldValue(pendingNewValue))
        var lockState by mutableStateOf(lockState)

        override fun clearSection() {
            Snapshot.withMutableSnapshot {
                content.clear()
                pendingNewValue = TextFieldValue()
            }
        }

        fun toSavedState() = SavedState(
            content = content,
            pendingNewValue = pendingNewValue.text,
            initialLockState = initialLockState,
            lockState = lockState,
        )

        @Serializable
        data class SavedState(
            val content: List<Entry>,
            val pendingNewValue: String,
            val initialLockState: LockState?,
            val lockState: LockState?,
        ) {
            fun toMultiText() = MultiText(
                content = content,
                pendingNewValue = pendingNewValue,
                initialLockState = initialLockState,
                lockState = lockState,
            )
        }

        @Serializable
        @Immutable
        sealed interface Entry {
            val id: String
            val text: String
            val serializedValue: String get() = text
            val searchableValue: String get() = text

            @Serializable
            data class Custom(override val text: String) : Entry {
                override val id = "custom_$text"
            }

            @Serializable
            data object Different : Entry {
                override val id = "different"
                override val text = ""
            }

            @Serializable
            data class Prefilled<T>(
                val value: T,
                override val id: String,
                override val text: String,
                val image: String? = null,
                val imageLink: String? = null,
                val secondaryImage: String? = null,
                val secondaryImageLink: String? = null,
                val titleText: String = text,
                val subtitleText: String? = null,
                override val serializedValue: String,
                override val searchableValue: String,
            ) : Entry
        }
    }

    enum class LockState(
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

        companion object {
            fun from(value: Boolean?) = value?.let {
                if (it) LOCKED else UNLOCKED
            } ?: DIFFERENT

            val Saver = StateUtils.nullableEnumSaver<LockState>()
        }
    }
}
