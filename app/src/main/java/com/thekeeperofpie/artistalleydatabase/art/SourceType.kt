package com.thekeeperofpie.artistalleydatabase.art

import android.util.Log
import androidx.annotation.StringRes
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryModel
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntrySection
import com.thekeeperofpie.artistalleydatabase.utils.observableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
sealed class SourceType(
    @Transient val serializedType: String = "",
    @Transient @StringRes val textRes: Int = -1,
) {

    abstract fun serializedValue(json: Json): String

    companion object {

        private const val TAG = "SourceType"

        fun fromEntry(json: Json, entry: ArtEntry): SourceType {
            val value = entry.sourceValue
            return when (entry.sourceType) {
                "unknown" -> Unknown
                "convention" -> {
                    if (value != null) {
                        try {
                            json.decodeFromString<Convention>(value)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse ${entry.sourceType}, sourceValue $value")
                            Convention()
                        }
                    } else Convention()
                }
                else -> if (value.isNullOrBlank()) Unknown else {
                    Custom(value)
                }
            }
        }
    }

    @Serializable
    data class Convention(
        val name: String = "",
        val year: Int? = null,
        val hall: String = "",
        val booth: String = "",
    ) : SourceType("convention", R.string.art_entry_source_convention) {
        override fun serializedValue(json: Json) = json.encodeToString(this)
    }

    data class Online(
        val name: String,
        val url: String,
    ) : SourceType("online", R.string.art_entry_source_online) {
        override fun serializedValue(json: Json) = json.encodeToString(this)
    }

    data class Custom(val value: String) : SourceType("custom", R.string.art_entry_source_custom) {
        override fun serializedValue(json: Json) = value
    }

    object Unknown : SourceType("unknown", R.string.art_entry_source_unknown) {
        override fun serializedValue(json: Json) = ""
    }

    /**
     * [Different] is internal value used for multi-edit to represent multiply not equal fields
     * across multiple entries. It should never be serialized to disk.
     */
    object Different : SourceType("different", R.string.art_entry_source_different) {
        override fun serializedValue(json: Json) = ""
    }
}

class SourceDropdown(locked: LockState? = null) : ArtEntrySection.Dropdown(
    headerRes = R.string.art_entry_source_header,
    arrowContentDescription = R.string.art_entry_source_dropdown_content_description,
    lockState = locked,
) {

    val conventionSectionItem = SourceConventionSectionItem()
    private val unknownSectionItem = SourceUnknownSectionItem()
    private val customSectionItem = SourceCustomSectionItem()

    init {
        options = mutableStateListOf(
            unknownSectionItem,
            conventionSectionItem,
            customSectionItem,
        )
    }

    fun initialize(json: Json, entry: ArtEntryModel) {
        val value = entry.sourceValue
        when (entry.sourceType) {
            is SourceType.Convention -> {
                if (value != null) {
                    val data = json.decodeFromString<SourceType.Convention>(value)
                    conventionSectionItem.setValues(data)
                }
                selectedIndex = options.indexOf(conventionSectionItem)
            }
            SourceType.Different -> {
                options += SourceDifferentSectionItem()
                selectedIndex = options.lastIndex
            }
            null,
            is SourceType.Custom,
            is SourceType.Online,
            SourceType.Unknown -> {
                if (value.isNullOrBlank()) {
                    selectedIndex = options.indexOf(unknownSectionItem)
                } else {
                    customSectionItem.value = value
                    selectedIndex = options.indexOf(customSectionItem)
                }
            }
        }
    }

    fun addDifferent() {
        options += SourceDifferentSectionItem()
        selectedIndex = options.lastIndex
    }

    override fun selectedItem() = super.selectedItem() as SourceItem

    sealed class SourceItem : Item {
        abstract fun toSource(): SourceType
    }
}

class SourceConventionSectionItem : SourceDropdown.SourceItem() {

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

    fun updateHallBoothIfEmpty(
        expectedName: String,
        expectedYear: Int,
        newHall: String,
        newBooth: String
    ) {
        if (name == expectedName && year == expectedYear.toString()
            && hall.isEmpty() && booth.isEmpty()
        ) {
            hall = newHall
            booth = newBooth
        }
    }

    override val hasCustomView = true

    override fun toSource() = SourceType.Convention(name, year.toIntOrNull(), hall, booth)

    @Composable
    override fun fieldText() = stringResource(R.string.art_entry_source_convention)

    @Composable
    override fun DropdownItemText() = Text(fieldText())

    @Composable
    override fun Content(lockState: ArtEntrySection.LockState?) {
        val showSecondRow = lockState != ArtEntrySection.LockState.LOCKED ||
                (hall.isNotEmpty() && booth.isNotEmpty())
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
                label = { Text(stringResource(R.string.art_entry_source_convention_label_name)) },
                placeholder = {
                    Text(stringResource(R.string.art_entry_source_convention_placeholder_name))
                },
                readOnly = lockState?.editable == false,
                onValueChange = { name = it },
                modifier = Modifier
                    .focusable(lockState?.editable != false)
                    .weight(1f, true),
            )
            TextField(
                value = year,
                label = { Text(stringResource(R.string.art_entry_source_convention_label_year)) },
                placeholder = {
                    Text(stringResource(R.string.art_entry_source_convention_placeholder_year))
                },
                readOnly = lockState?.editable == false,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
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
                    label = { Text(stringResource(R.string.art_entry_source_convention_label_hall)) },
                    placeholder = {
                        Text(stringResource(R.string.art_entry_source_convention_placeholder_hall))
                    },
                    onValueChange = { hall = it },
                    modifier = Modifier.weight(1f, true),
                )
                TextField(
                    value = booth,
                    label = { Text(stringResource(R.string.art_entry_source_convention_label_booth)) },
                    placeholder = {
                        Text(stringResource(R.string.art_entry_source_convention_placeholder_booth))
                    },
                    onValueChange = { booth = it },
                    modifier = Modifier.weight(1f, true),
                )
            }
        }
    }
}

class SourceCustomSectionItem : SourceDropdown.SourceItem() {

    var value by mutableStateOf("")

    override val hasCustomView = true

    override fun toSource() = SourceType.Custom(value.trim())

    @Composable
    override fun fieldText() = stringResource(R.string.art_entry_source_custom)

    @Composable
    override fun DropdownItemText() = Text(fieldText())

    @Composable
    override fun Content(lockState: ArtEntrySection.LockState?) {
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

class SourceUnknownSectionItem : SourceDropdown.SourceItem() {

    override val hasCustomView = false

    override fun toSource() = SourceType.Unknown

    @Composable
    override fun fieldText() = stringResource(SourceType.Unknown.textRes)

    @Composable
    override fun DropdownItemText() = Text(fieldText())
}

class SourceDifferentSectionItem : SourceDropdown.SourceItem() {

    override val hasCustomView = false

    override fun toSource() = SourceType.Different

    @Composable
    override fun fieldText() = stringResource(SourceType.Different.textRes)

    @Composable
    override fun DropdownItemText() = Text(fieldText())
}