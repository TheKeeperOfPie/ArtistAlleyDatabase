package com.thekeeperofpie.artistalleydatabase.art

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.R

sealed class ArtEntrySection {

    class MultiText(
        @StringRes val headerZero: Int,
        @StringRes val headerOne: Int,
        @StringRes val headerMany: Int,
        initialPendingValue: String = "",
    ) : ArtEntrySection() {
        val contents = mutableStateListOf<String>()
        var pendingValue by mutableStateOf(initialPendingValue)

        fun finalContents() = (contents + pendingValue).filter { it.isNotEmpty() }
    }

    open class Dropdown<T>(
        @StringRes val headerRes: Int,
        @StringRes val customValueLabelRes0: Int,
        @StringRes val customValueLabelRes1: Int,
        var options: SnapshotStateList<Item> = mutableStateListOf(),
    ) : ArtEntrySection() {
        var expanded by mutableStateOf(false)
        var selectedIndex by mutableStateOf(0)

        @Composable
        open fun textOf(value: T) = value.toString()

        fun selectedItem() = options[selectedIndex]

        sealed class Item(val hasCustomView: Boolean) {

            @Composable
            abstract fun fieldText(): String

            @Composable
            abstract fun DropdownItemText()

            @Composable
            open fun Content() {
            }

            class Basic<T>(val value: T, @StringRes val textRes: Int) : Item(false) {

                @Composable
                override fun fieldText() = stringResource(textRes)

                @Composable
                override fun DropdownItemText() = Text(fieldText())
            }

            class TwoFields(
                @StringRes val customValueLabelRes0: Int,
                @StringRes val customValueLabelRes1: Int,
            ) : Item(true) {

                var customValue0 by mutableStateOf("")
                var customValue1 by mutableStateOf("")

                @Composable
                override fun fieldText() = stringResource(R.string.custom)

                @Composable
                override fun DropdownItemText() = Text(fieldText())

                @Composable
                override fun Content() {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
                            .safeContentPadding()
                    ) {
                        TextField(
                            value = customValue0,
                            label = { Text(stringResource(customValueLabelRes0)) },
                            onValueChange = { customValue0 = it },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.weight(1f, true),
                        )
                        TextField(
                            value = customValue1,
                            label = { Text(stringResource(customValueLabelRes1)) },
                            onValueChange = { customValue1 = it },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.weight(1f, true),
                        )
                    }
                }
            }
        }
    }
}