package com.thekeeperofpie.artistalleydatabase.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.compose.MinWidthTextField
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
sealed class SettingsSection(open val id: String) {

    @Composable
    abstract fun Content(modifier: Modifier)

    class Switch(
        @StringRes private val labelTextRes: Int,
        private val property: MutableStateFlow<Boolean>,
    ) : SettingsSection("switch-$labelTextRes") {

        @Composable
        override fun Content(modifier: Modifier) {
            val checked by property.collectAsState()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
            ) {
                Text(
                    text = stringResource(labelTextRes),
                    Modifier
                        .weight(1f)
                )
                Switch(checked = checked, onCheckedChange = { property.value = it })
            }
        }
    }

    class Button(
        @StringRes private val labelTextRes: Int,
        @StringRes private val buttonTextRes: Int,
        private val onClick: () -> Unit,
    ) : SettingsSection("button-$labelTextRes") {

        @Composable
        override fun Content(modifier: Modifier) {
            ButtonRow(labelTextRes = labelTextRes, buttonTextRes = buttonTextRes, onClick = onClick)
        }
    }

    class Dropdown<T>(
        @StringRes private val labelTextRes: Int,
        id: String = "dropdown-$labelTextRes",
        private val options: List<T>,
        private val optionToText: @Composable (T) -> String,
        private val initialSelectedOption: T? = null,
        private val onItemSelected: (T) -> Unit = {},
        @StringRes private val buttonTextRes: Int? = null,
        private val onClickButton: (T) -> Unit = {},
    ) : SettingsSection(id) {
        constructor(
            @StringRes labelTextRes: Int,
            options: List<T>,
            optionToText: @Composable (T) -> String,
            property: MutableStateFlow<T>,
        ) : this(
            labelTextRes = labelTextRes,
            options = options,
            optionToText = optionToText,
            initialSelectedOption = property.value,
            onItemSelected = { property.value = it },
        )

        @Composable
        override fun Content(modifier: Modifier) {
            var expanded by remember { mutableStateOf(false) }
            var selectedIndex by rememberSaveable {
                mutableIntStateOf(options.indexOf(initialSelectedOption).coerceAtLeast(0))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .then(modifier)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f),
                ) {
                    TextField(
                        value = optionToText(options[selectedIndex]),
                        onValueChange = {},
                        label = { Text(stringResource(labelTextRes)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        options.forEachIndexed { index, item ->
                            DropdownMenuItem(
                                text = { Text(optionToText(item)) },
                                onClick = {
                                    selectedIndex = index
                                    expanded = false
                                    onItemSelected(options[index])
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                if (buttonTextRes != null) {
                    FilledTonalButton(onClick = { onClickButton(options[selectedIndex]) }) {
                        Text(text = stringResource(buttonTextRes))
                    }
                }
            }
        }
    }

    abstract class Custom(id: String) : SettingsSection(id)

    data class Placeholder(override val id: String) : SettingsSection(id) {
        @Composable
        override fun Content(modifier: Modifier) {
            throw IllegalStateException("Placeholder should not be invoked")
        }
    }

    class Subsection(
        val icon: ImageVector,
        @StringRes val labelTextRes: Int,
        val children: List<SettingsSection>,
    ) : SettingsSection("subsection-$labelTextRes") {
        @Composable
        override fun Content(modifier: Modifier) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(modifier)
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(labelTextRes)
                )
                Text(
                    text = stringResource(labelTextRes),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }

    class Text(
        @StringRes val titleTextRes: Int,
        @StringRes val descriptionTextRes: Int,
    ) : SettingsSection("text-$titleTextRes-$descriptionTextRes") {
        @Composable
        override fun Content(modifier: Modifier) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .then(modifier)
            ) {
                Text(
                    text = stringResource(titleTextRes),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(descriptionTextRes),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    class TextByString(
        id: String,
        val title: @Composable () -> String,
        val description: @Composable () -> String,
    ) : SettingsSection(id) {
        @Composable
        override fun Content(modifier: Modifier) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .then(modifier)
            ) {
                Text(
                    text = title(),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = description(),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    class TextField(
        @StringRes private val labelTextRes: Int,
        initialValue: String,
        private val onValueChange: (String) -> Unit,
    ) : SettingsSection("textField-$labelTextRes") {

        private var value by mutableStateOf(initialValue)

        @Composable
        override fun Content(modifier: Modifier) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(labelTextRes),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        .weight(1f)
                )
                MinWidthTextField(
                    value = value,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                    onValueChange = {
                        value = it
                        onValueChange(it)
                    },
                    minWidth = 120.dp,
                )
            }
        }
    }
}
