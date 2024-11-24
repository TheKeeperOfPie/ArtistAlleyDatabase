package com.thekeeperofpie.artistalleydatabase.art.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import artistalleydatabase.modules.art.generated.resources.art_entry_source_dropdown_content_description
import artistalleydatabase.modules.art.generated.resources.art_entry_source_header
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryModel
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.utils_compose.observableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.stringResource

class SourceDropdown(locked: LockState? = null) : EntrySection.Dropdown(
    headerRes = Res.string.art_entry_source_header,
    arrowContentDescription = Res.string.art_entry_source_dropdown_content_description,
    lockState = locked,
) {

    val conventionSectionItem = ConventionSectionItem()
    private val unknownSectionItem = UnknownSectionItem()
    private val customSectionItem = CustomSectionItem()

    init {
        options.addAll(
            Section.entries.map {
                when (it) {
                    Section.UNKNOWN -> unknownSectionItem
                    Section.CONVENTION -> conventionSectionItem
                    Section.CUSTOM -> customSectionItem
                }
            }
        )
    }

    private enum class Section {
        UNKNOWN,
        CONVENTION,
        CUSTOM,
    }

    fun initialize(entry: ArtEntryModel, lockState: LockState?) {
        when (val source = entry.source) {
            is SourceType.Convention -> {
                conventionSectionItem.setValues(source)
                selectedIndex = Section.CONVENTION.ordinal
            }
            SourceType.Different -> {
                options += DifferentSectionItem()
                selectedIndex = options.lastIndex
            }
            is SourceType.Custom -> {
                customSectionItem.value = source.value
                selectedIndex = Section.CUSTOM.ordinal
            }
            null,
            is SourceType.Online,
            SourceType.Unknown,
                -> {
                selectedIndex = Section.UNKNOWN.ordinal
            }
        }
        this.lockState = lockState
    }

    fun addDifferent() {
        options.add(DifferentSectionItem())
        selectedIndex = options.lastIndex
    }

    override fun selectedItem() = super.selectedItem() as SourceItem

    sealed class SourceItem : Item {
        abstract fun toSource(): SourceType
    }

    class ConventionSectionItem : SourceItem() {

        private var name by observableStateOf("") { emitNew() }
        private var year by observableStateOf("") { emitNew() }
        private var hall by observableStateOf("") { emitNew() }
        private var booth by observableStateOf("") { emitNew() }

        private var flow = MutableStateFlow(SourceType.Convention())

        fun setValues(convention: SourceType.Convention) {
            name = convention.name
            year = convention.year?.toString().orEmpty()
            hall = convention.hall
            booth = convention.booth
        }

        private fun emitNew() {
            flow.tryEmit(toSource())
        }

        fun updates() = flow.asStateFlow()

        /**
         * @return true if anything changed
         */
        fun updateHallBoothIfEmpty(
            expectedName: String,
            expectedYear: Int,
            newHall: String,
            newBooth: String,
        ): Boolean {
            if (name == expectedName && year == expectedYear.toString()) {
                when {
                    hall.isEmpty() && booth.isEmpty() -> {
                        hall = newHall
                        booth = newBooth
                    }
                    hall.isEmpty() -> if (booth == newBooth) {
                        hall = newHall
                    }
                    booth.isEmpty() -> if (hall == newHall) {
                        booth = newBooth
                    }
                    else -> return false
                }
                return true
            }
            return false
        }

        override val hasCustomView = true

        override fun toSource() = SourceType.Convention(name, year.toIntOrNull(), hall, booth)

        @Composable
        override fun fieldText() = stringResource(Res.string.art_entry_source_convention)

        @Composable
        override fun DropdownItemText() = Text(fieldText())

        @Composable
        override fun Content(lockState: LockState?) {
            val showSecondRow = lockState != LockState.LOCKED ||
                    (hall.isNotEmpty() || booth.isNotEmpty())
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp,
                        bottom = if (showSecondRow) 4.dp else 10.dp
                    )
            ) {
                TextField(
                    value = name,
                    label = { Text(stringResource(Res.string.art_entry_source_convention_label_name)) },
                    placeholder = {
                        Text(stringResource(Res.string.art_entry_source_convention_placeholder_name))
                    },
                    readOnly = lockState?.editable == false,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .focusable(lockState?.editable != false)
                        .weight(1f, true),
                )
                TextField(
                    value = year,
                    label = { Text(stringResource(Res.string.art_entry_source_convention_label_year)) },
                    placeholder = {
                        Text(stringResource(Res.string.art_entry_source_convention_placeholder_year))
                    },
                    readOnly = lockState?.editable == false,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        autoCorrectEnabled = KeyboardOptions.Default.autoCorrectEnabled,
                    ),
                    onValueChange = { year = it },
                    modifier = Modifier
                        .focusable(lockState?.editable != false)
                        .weight(1f, true),
                )
            }

            AnimatedVisibility(
                visible = showSecondRow,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp)
                ) {
                    TextField(
                        value = hall,
                        label = { Text(stringResource(Res.string.art_entry_source_convention_label_hall)) },
                        placeholder = {
                            Text(stringResource(Res.string.art_entry_source_convention_placeholder_hall))
                        },
                        readOnly = lockState?.editable == false,
                        onValueChange = { hall = it },
                        modifier = Modifier
                            .focusable(lockState?.editable != false)
                            .weight(1f, true),
                    )
                    TextField(
                        value = booth,
                        label = { Text(stringResource(Res.string.art_entry_source_convention_label_booth)) },
                        placeholder = {
                            Text(stringResource(Res.string.art_entry_source_convention_placeholder_booth))
                        },
                        readOnly = lockState?.editable == false,
                        onValueChange = { booth = it },
                        modifier = Modifier
                            .focusable(lockState?.editable != false)
                            .weight(1f, true),
                    )
                }
            }
        }
    }

    private class CustomSectionItem : SourceItem() {

        var value by mutableStateOf("")

        override val hasCustomView = true

        override fun toSource() = SourceType.Custom(value.trim())

        @Composable
        override fun fieldText() = stringResource(Res.string.art_entry_source_custom)

        @Composable
        override fun DropdownItemText() = Text(fieldText())

        @Composable
        override fun Content(lockState: LockState?) {
            TextField(
                value = value,
                onValueChange = { value = it },
                readOnly = lockState?.editable == false,
                modifier = Modifier
                    .focusable(lockState?.editable != false)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
            )
        }
    }

    private class UnknownSectionItem : SourceItem() {

        override val hasCustomView = false

        override fun toSource() = SourceType.Unknown

        @Composable
        override fun fieldText() = stringResource(SourceType.Unknown.textRes)

        @Composable
        override fun DropdownItemText() = Text(fieldText())
    }

    private class DifferentSectionItem : SourceItem() {

        override val hasCustomView = false

        override fun toSource() = SourceType.Different

        @Composable
        override fun fieldText() = stringResource(SourceType.Different.textRes)

        @Composable
        override fun DropdownItemText() = Text(fieldText())
    }
}
