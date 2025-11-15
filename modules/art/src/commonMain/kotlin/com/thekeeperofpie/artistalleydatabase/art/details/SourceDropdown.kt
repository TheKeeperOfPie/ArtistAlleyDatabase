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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.Modifier
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
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.entry.form.DropdownSection
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import com.thekeeperofpie.artistalleydatabase.utils_compose.text.ForceEnabledTextField
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object SourceDropdown {
    private val DigitOnlyInputTransformation = InputTransformation {
        if (!asCharSequence().all { it.isDigit() }) {
            revertAllChanges()
        }
    }

    enum class Option(val text: StringResource) {
        UNKNOWN(Res.string.art_entry_source_unknown),
        CONVENTION(Res.string.art_entry_source_convention),
        CUSTOM(Res.string.art_entry_source_custom),
    }

    context(entryFormScope: EntryFormScope)
    @Composable
    operator fun invoke(state: State) {
        Column {
            entryFormScope.DropdownSection(
                state = state.dropdownState,
                options = Option.entries,
                headerText = { Text(stringResource(Res.string.art_entry_source_header)) },
                optionToText = { stringResource(it.text) },
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
        val dropdownState: EntryForm2.DropdownState = EntryForm2.DropdownState(),
        val conventionState: ConventionState = ConventionState(name = ""),
        val customTextState: TextFieldState = TextFieldState(),
    ) {
        val lockState get() = dropdownState.lockState
        val selectedOption get() = Option.entries[dropdownState.selectedIndex]

        object Saver : ComposeSaver<State, Any> {
            override fun SaverScope.save(value: State) = listOf(
                with(EntryForm2.DropdownState.Saver) { save(value.dropdownState) },
                with(ConventionState.Saver) { save(value.conventionState) },
                with(TextFieldState.Saver) { save(value.customTextState) },
            )

            override fun restore(value: Any): State {
                val (dropdown, conventionState, customTextState) = value as List<*>
                return State(
                    dropdownState = with(EntryForm2.DropdownState.Saver) { restore(dropdown!!) }!!,
                    conventionState = with(ConventionState.Saver) { restore(conventionState!!) },
                    customTextState = with(TextFieldState.Saver) { restore(customTextState!!) }!!,
                )
            }
        }
    }

    @Stable
    class ConventionState(
        val name: TextFieldState = TextFieldState(),
        val year: TextFieldState = TextFieldState(),
        val hall: TextFieldState = TextFieldState(),
        val booth: TextFieldState = TextFieldState(),
    ) {
        constructor(
            name: String = "",
            year: String = "",
            hall: String = "",
            booth: String = "",
        ) : this(
            name = TextFieldState(name),
            year = TextFieldState(year),
            hall = TextFieldState(hall),
            booth = TextFieldState(booth),
        )

        object Saver : ComposeSaver<ConventionState, Any> {
            override fun SaverScope.save(value: ConventionState) = listOf(
                with(TextFieldState.Saver) { save(value.name) },
                with(TextFieldState.Saver) { save(value.year) },
                with(TextFieldState.Saver) { save(value.hall) },
                with(TextFieldState.Saver) { save(value.booth) },
            )

            override fun restore(value: Any): ConventionState {
                val (name, year, hall, booth) = value as List<*>
                return ConventionState(
                    name = with(TextFieldState.Saver) { restore(name!!) }!!,
                    year = with(TextFieldState.Saver) { restore(year!!) }!!,
                    hall = with(TextFieldState.Saver) { restore(hall!!) }!!,
                    booth = with(TextFieldState.Saver) { restore(booth!!) }!!,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Convention() {
    EntryForm2 {
        SourceDropdown(
            state = remember {
                SourceDropdown.State(
                    dropdownState = EntryForm2.DropdownState(
                        initialSelectedIndex = SourceDropdown.Option.entries.indexOf(
                            SourceDropdown.Option.CONVENTION
                        )
                    )
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConventionLocked() {
    SourceDropdown.ConventionSection(
        state = SourceDropdown.ConventionState(
            name = "Anime Expo",
            year = "2025",
        ),
        lockState = { EntryLockState.LOCKED },
    )
}

@Preview(showBackground = true)
@Composable
private fun ConventionLockedSecondRow() {
    SourceDropdown.ConventionSection(
        state = SourceDropdown.ConventionState(hall = "Artist Alley", booth = "C39"),
        lockState = { EntryLockState.LOCKED },
    )
}

@Preview(showBackground = true)
@Composable
private fun Custom() {
    EntryForm2 {
        SourceDropdown(
            state = remember {
                SourceDropdown.State(
                    dropdownState = EntryForm2.DropdownState(
                        initialSelectedIndex = SourceDropdown.Option.entries.indexOf(
                            SourceDropdown.Option.CUSTOM
                        )
                    ),
                    customTextState = TextFieldState("Some custom source entry")
                )
            },
        )
    }
}
