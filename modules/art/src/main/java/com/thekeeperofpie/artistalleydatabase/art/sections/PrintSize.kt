package com.thekeeperofpie.artistalleydatabase.art.sections

import androidx.annotation.StringRes
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.art.BuildConfig
import com.thekeeperofpie.artistalleydatabase.art.R
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import kotlin.math.roundToInt

sealed class PrintSize(
    val printWidth: Int?,
    val printHeight: Int?,
    @StringRes val textRes: Int
) {

    companion object {

        val PORTRAITS = listOf(
            Unknown,
            Portrait8HalfBy11,
            Portrait11x17,
            Portrait12x18,
            Portrait13x19,
        )

        val LANDSCAPES = listOf(
            Unknown,
            Landscape11x8Half,
            Landscape17x11,
            Landscape18x12,
            Landscape19x13,
        )

        init {
            if (BuildConfig.DEBUG) {
                // NOTE: These two arrays must always be the same size
                assert(PORTRAITS.size == LANDSCAPES.size)
            }
        }
    }

    object Unknown : PrintSize(null, null, UtilsStringR.unknown)

    object Portrait8HalfBy11 :
        PrintSize((8.5 * 25.4).roundToInt(), (11 * 25.4).toInt(), R.string.print_size_8_5x11_inches)

    object Landscape11x8Half :
        PrintSize((1 * 25.4).toInt(), (8.5 * 25.4).toInt(), R.string.print_size_11x8_5_inches)

    object Portrait11x17 :
        PrintSize((11 * 25.4).toInt(), (17 * 25.4).toInt(), R.string.print_size_11x17_inches)

    object Landscape17x11 :
        PrintSize((17 * 25.4).toInt(), (11 * 25.4).toInt(), R.string.print_size_17x11_inches)

    object Portrait12x18 :
        PrintSize((12 * 25.4).toInt(), (18 * 25.4).toInt(), R.string.print_size_12x18_inches)

    object Landscape18x12 :
        PrintSize((18 * 25.4).toInt(), (12 * 25.4).toInt(), R.string.print_size_18x12_inches)

    object Portrait13x19 :
        PrintSize((13 * 25.4).toInt(), (19 * 25.4).toInt(), R.string.print_size_13x19_inches)

    object Landscape19x13 :
        PrintSize((19 * 25.4).toInt(), (13 * 25.4).toInt(), R.string.print_size_19x13_inches)
}

class PrintSizeCustomTextFields : EntrySection.Dropdown.Item {

    var width by mutableStateOf("")
    var height by mutableStateOf("")

    override val hasCustomView = true

    @Composable
    override fun fieldText() = stringResource(UtilsStringR.custom)

    @Composable
    override fun DropdownItemText() = Text(fieldText())

    @Composable
    override fun Content(lockState: EntrySection.LockState?) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
        ) {
            TextField(
                value = width,
                label = { Text(stringResource(R.string.add_entry_size_label_width)) },
                onValueChange = { width = it },
                readOnly = lockState?.editable == false,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    autoCorrectEnabled = KeyboardOptions.Default.autoCorrectEnabled,
                ),
                modifier = Modifier
                    .focusable(lockState?.editable != false)
                    .weight(1f, true),
            )
            TextField(
                value = height,
                label = { Text(stringResource(R.string.add_entry_size_label_height)) },
                onValueChange = { height = it },
                readOnly = lockState?.editable == false,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    autoCorrectEnabled = KeyboardOptions.Default.autoCorrectEnabled,
                ),
                modifier = Modifier
                    .focusable(lockState?.editable != false)
                    .weight(1f, true),
            )
        }
    }
}
