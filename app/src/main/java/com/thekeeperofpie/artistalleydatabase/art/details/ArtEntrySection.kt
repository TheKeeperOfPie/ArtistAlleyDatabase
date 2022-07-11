package com.thekeeperofpie.artistalleydatabase.art.details

import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class ArtEntrySection(locked: Boolean? = null) {

    var locked by mutableStateOf(locked)

    class MultiText(
        @StringRes val headerZero: Int,
        @StringRes val headerOne: Int,
        @StringRes val headerMany: Int,
        initialPendingValue: String = "",
        locked: Boolean? = null
    ) : ArtEntrySection(locked) {
        val contents = mutableStateListOf<Entry>()
        private var _pendingValue by mutableStateOf(initialPendingValue)
        private var pendingValueUpdates = MutableStateFlow("")
        var pendingValue: String
            get() = _pendingValue
            set(value) {
                _pendingValue = value
                pendingValueUpdates.tryEmit(value)
            }

        // TODO: Predictions for existing prefilled fields
        var predictions by mutableStateOf(emptyList<Entry>())

        fun pendingEntry() = Entry.Custom(pendingValue)

        fun valueUpdates() = pendingValueUpdates.asStateFlow()

        fun finalContents() = (contents + Entry.Custom(pendingValue.trim()))
            .filterNot { it.serializedValue.isBlank() }

        sealed class Entry(
            val text: String,
            val serializedValue: String = text,
            val searchableValue: String = text,
        ) {
            class Custom(text: String, val id: String? = null) : Entry(text)

            class Prefilled(
                val id: String,
                text: String,
                val image: String? = null,
                val titleText: String = text,
                val subtitleText: String? = null,
                serializedValue: String,
                searchableValue: String,
            ) : Entry(text, serializedValue, searchableValue)
        }
    }

    class LongText(
        @StringRes val headerRes: Int,
        initialPendingValue: String = "",
        locked: Boolean? = null,
    ) : ArtEntrySection(locked) {
        var value by mutableStateOf(initialPendingValue)
    }

    open class Dropdown(
        @StringRes val headerRes: Int,
        @StringRes val arrowContentDescription: Int,
        var options: SnapshotStateList<Item> = mutableStateListOf(),
        locked: Boolean? = null,
    ) : ArtEntrySection(locked) {
        var expanded by mutableStateOf(false)
        var selectedIndex by mutableStateOf(0)

        fun selectedItem() = options[selectedIndex]

        interface Item {

            val hasCustomView: Boolean

            @Composable
            fun fieldText(): String

            @Composable
            fun DropdownItemText()

            @Composable
            fun Content(locked: Boolean?) {
            }

            class Basic<T>(val value: T, @StringRes val textRes: Int) : Item {

                override val hasCustomView = false

                @Composable
                override fun fieldText() = stringResource(textRes)

                @Composable
                override fun DropdownItemText() = Text(fieldText())
            }
        }
    }
}