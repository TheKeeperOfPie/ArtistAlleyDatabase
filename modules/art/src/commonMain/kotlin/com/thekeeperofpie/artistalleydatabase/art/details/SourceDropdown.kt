package com.thekeeperofpie.artistalleydatabase.art.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.art.generated.resources.Res
import artistalleydatabase.modules.art.generated.resources.art_entry_source_convention
import artistalleydatabase.modules.art.generated.resources.art_entry_source_convention_label_booth
import artistalleydatabase.modules.art.generated.resources.art_entry_source_convention_label_hall
import artistalleydatabase.modules.art.generated.resources.art_entry_source_convention_label_name
import artistalleydatabase.modules.art.generated.resources.art_entry_source_convention_label_year
import artistalleydatabase.modules.art.generated.resources.art_entry_source_convention_placeholder_booth
import artistalleydatabase.modules.art.generated.resources.art_entry_source_convention_placeholder_hall
import artistalleydatabase.modules.art.generated.resources.art_entry_source_convention_placeholder_name
import artistalleydatabase.modules.art.generated.resources.art_entry_source_convention_placeholder_year
import artistalleydatabase.modules.art.generated.resources.art_entry_source_custom
import artistalleydatabase.modules.art.generated.resources.art_entry_source_header
import artistalleydatabase.modules.art.generated.resources.art_entry_source_unknown
import com.thekeeperofpie.artistalleydatabase.art.sections.SourceType
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.entry.form.DropdownSection
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.text.ForceEnabledTextField
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object SourceDropdown {
    private val DigitOnlyInputTransformation = object : InputTransformation {
        override fun TextFieldBuffer.transformInput() {
            if (!asCharSequence().all { it.isDigit() }) {
                revertAllChanges()
            }
        }
    }

    enum class Option(val text: StringResource) {
        UNKNOWN(Res.string.art_entry_source_unknown),
        CONVENTION(Res.string.art_entry_source_convention),
        CUSTOM(Res.string.art_entry_source_custom),
    }

    context(entryFormScope: EntryFormScope)
    @Composable
    operator fun invoke(
        state: State,
        focusRequester: FocusRequester,
        onFocusChanged: (Boolean) -> Unit,
    ) {
        Column {
            entryFormScope.DropdownSection(
                state = state.dropdown,
                options = Option.entries,
                headerText = { Text(stringResource(Res.string.art_entry_source_header)) },
                optionToText = { stringResource(it.text) },
                focusRequester = focusRequester,
                onFocusChanged = onFocusChanged,
            )

            when (state.selectedOption) {
                Option.UNKNOWN -> Unit
                Option.CONVENTION -> ConventionSection(
                    lockState = { state.lockState },
                    state = state.conventionState,
                )
                Option.CUSTOM -> CustomSection(
                    lockState = state.lockState,
                    state = state.customTextState,
                )
            }
        }
    }

    @Composable
    private fun CustomSection(
        lockState: EntryLockState,
        state: TextFieldState,
    ) {
        val readOnly = !lockState.editable
        ForceEnabledTextField(
            state = state, readOnly = readOnly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        )
    }

    @Composable
    internal fun ConventionSection(
        lockState: () -> EntryLockState,
        state: ConventionState,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            val readOnly = !lockState().editable
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ForceEnabledTextField(
                    state = state.name,
                    label = { Text(stringResource(Res.string.art_entry_source_convention_label_name)) },
                    placeholder = { Text(stringResource(Res.string.art_entry_source_convention_placeholder_name)) },
                    readOnly = readOnly,
                    modifier = Modifier.weight(1f, true),
                )
                ForceEnabledTextField(
                    state = state.year,
                    label = { Text(stringResource(Res.string.art_entry_source_convention_label_year)) },
                    placeholder = { Text(stringResource(Res.string.art_entry_source_convention_placeholder_year)) },
                    readOnly = readOnly,
                    inputTransformation = DigitOnlyInputTransformation,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        autoCorrectEnabled = KeyboardOptions.Default.autoCorrectEnabled,
                    ),
                    modifier = Modifier.weight(1f, true),
                )
            }

            val showSecondRow by remember {
                derivedStateOf {
                    lockState() != EntryLockState.LOCKED ||
                            state.hall.text.isNotEmpty() || state.booth.text.isNotEmpty()
                }
            }
            AnimatedVisibility(
                visible = showSecondRow,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ForceEnabledTextField(
                        state = state.hall,
                        label = { Text(stringResource(Res.string.art_entry_source_convention_label_hall)) },
                        placeholder = { Text(stringResource(Res.string.art_entry_source_convention_placeholder_hall)) },
                        readOnly = readOnly,
                        modifier = Modifier.weight(1f, true),
                    )
                    ForceEnabledTextField(
                        state = state.booth,
                        label = { Text(stringResource(Res.string.art_entry_source_convention_label_booth)) },
                        placeholder = { Text(stringResource(Res.string.art_entry_source_convention_placeholder_booth)) },
                        readOnly = readOnly,
                        modifier = Modifier.weight(1f, true),
                    )
                }
            }
        }
    }

    @Stable
    class State(
        val dropdown: EntryForm2.DropdownState,
        val conventionState: ConventionState,
        val customTextState: TextFieldState,
    ) {
        val lockState get() = dropdown.lockState
        val selectedOption get() = Option.entries[dropdown.selectedIndex]
    }

    @Composable
    fun rememberState(
        initialConventionData: SourceType.Convention = SourceType.Convention(),
        initialCustomText: String = "",
        initialSelectedIndex: Int = 0,
    ): State {
        val conventionState = rememberConventionState(initialConventionData)
        val dropdownState = EntryForm2.rememberDropdownState(initialSelectedIndex)
        val customTextState = rememberTextFieldState(initialText = initialCustomText)
        return remember(dropdownState, conventionState) {
            State(
                dropdown = dropdownState,
                conventionState = conventionState,
                customTextState = customTextState,
            )
        }
    }

    @Stable
    class ConventionState(
        val name: TextFieldState,
        val year: TextFieldState,
        val hall: TextFieldState,
        val booth: TextFieldState,
    )
}

@Composable
private fun rememberConventionState(
    initialConventionData: SourceType.Convention,
): SourceDropdown.ConventionState {
    val name = rememberTextFieldState(initialConventionData.name)
    val year = rememberTextFieldState(initialConventionData.year?.toString().orEmpty())
    val hall = rememberTextFieldState(initialConventionData.hall)
    val booth = rememberTextFieldState(initialConventionData.booth)
    return remember(name, year, hall, booth) {
        SourceDropdown.ConventionState(
            name = name,
            year = year,
            hall = hall,
            booth = booth,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Convention() {
    EntryForm2 {
        SourceDropdown(
            state = SourceDropdown.rememberState(
                initialSelectedIndex = SourceDropdown.Option.entries.indexOf(SourceDropdown.Option.CONVENTION),
                initialConventionData = SourceType.Convention(),
                initialCustomText = "",
            ),
            focusRequester = remember { FocusRequester() },
            onFocusChanged = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConventionLocked() {
    SourceDropdown.ConventionSection(
        state = rememberConventionState(SourceType.Convention(name = "Anime Expo", year = 2025)),
        lockState = { EntryLockState.LOCKED },
    )
}

@Preview(showBackground = true)
@Composable
private fun ConventionLockedSecondRow() {
    SourceDropdown.ConventionSection(
        state = rememberConventionState(
            SourceType.Convention(hall = "Artist Alley", booth = "C39")
        ),
        lockState = { EntryLockState.LOCKED },
    )
}

@Preview(showBackground = true)
@Composable
private fun Custom() {
    EntryForm2 {
        SourceDropdown(
            state = SourceDropdown.rememberState(
                initialConventionData = SourceType.Convention(),
                initialSelectedIndex = SourceDropdown.Option.entries.indexOf(SourceDropdown.Option.CUSTOM),
                initialCustomText = "Some custom source entry",
            ),
            focusRequester = remember { FocusRequester() },
            onFocusChanged = {},
        )
    }
}
