package com.thekeeperofpie.artistalleydatabase.art

import android.util.JsonReader
import android.util.JsonToken
import android.util.Log
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.R
import okhttp3.internal.closeQuietly

sealed class SourceType(val serializedType: String, @StringRes val textRes: Int) {

    companion object {

        fun fromEntry(entry: ArtEntry): SourceType {
            val value = entry.sourceValue
            return when (entry.sourceType) {
                "unknown" -> Unknown
                "convention" -> {
                    var name = ""
                    var year = ""
                    var hall = ""
                    var booth = ""

                    if (value != null) {
                        try {
                            val reader = JsonReader(value.reader())
                            reader.beginObject()
                            reader.isLenient = true
                            while (reader.peek() != JsonToken.END_OBJECT) {
                                when (reader.nextName()) {
                                    "name" -> name = reader.nextString()
                                    "year" -> year = reader.nextString()
                                    "hall" -> hall = reader.nextString()
                                    "booth" -> booth = reader.nextString()
                                }
                            }
                            reader.endObject()
                            reader.closeQuietly()
                        } catch (e: Exception) {
                            Log.d("SourceType", "Error parsing convention type of $value", e)
                        }
                    }

                    Convention(name, year.toIntOrNull(), hall, booth)
                }
                else -> if (value.isNullOrBlank()) Unknown else {
                    Custom(value)
                }
            }
        }
    }

    data class Convention(
        val name: String,
        val year: Int?,
        val hall: String,
        val booth: String,
    ) : SourceType("convention", R.string.art_entry_source_convention)

    data class Online(
        val name: String,
        val url: String,
    ) : SourceType("online", R.string.art_entry_source_online)

    object Unknown : SourceType("unknown", R.string.art_entry_source_unknown)

    data class Custom(val value: String) : SourceType("custom", R.string.art_entry_source_custom)
}

class SourceDropdown(locked: Boolean? = null) : ArtEntrySection.Dropdown(
    R.string.art_entry_source_header,
    R.string.art_entry_source_dropdown_content_description,
    listOf(
        Item.Basic<SourceType>(SourceType.Unknown, SourceType.Unknown.textRes),
        SourceConventionSectionItem(),
        SourceCustomSectionItem(),
    ).toMutableStateList(),
    locked,
) {

    fun initialize(entry: ArtEntry) {
        val value = entry.sourceValue
        when (entry.sourceType) {
            "unknown" -> {
                selectedIndex = 0
            }
            "convention" -> {
                selectedIndex = 1
                if (value != null) {
                    (options[1] as SourceConventionSectionItem).run {
                        val reader = JsonReader(value.reader())
                        reader.beginObject()
                        reader.isLenient = true
                        while (reader.peek() != JsonToken.END_OBJECT) {
                            when (reader.nextName()) {
                                "name" -> name = reader.nextString()
                                "year" -> year = reader.nextString()
                                "hall" -> hall = reader.nextString()
                                "booth" -> booth = reader.nextString()
                            }
                        }
                        reader.endObject()
                        reader.closeQuietly()
                    }
                }
            }
            else -> {
                if (value.isNullOrBlank()) {
                    selectedIndex = 0
                } else {
                    selectedIndex = 2
                    (options[2] as SourceCustomSectionItem).value = value
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun finalTypeToValue() = when (val item = selectedItem()) {
        is Item.Basic<*> -> (item as Item.Basic<SourceType>).value.serializedType to ""
        is SourceConventionSectionItem -> "convention" to run {
            // TODO: Find a better way to write out the JSON
            """
                {
                  "name": "${item.name.trim()}",
                  "year": "${item.year.trim()}",
                  "hall": "${item.hall.trim()}",
                  "booth": "${item.booth.trim()}"
                }
            """.trimIndent()
        }
        is SourceCustomSectionItem -> "custom" to item.value.trim()
        else -> throw IllegalArgumentException()
    }
}

class SourceConventionSectionItem : ArtEntrySection.Dropdown.Item {

    var name by mutableStateOf("")
    var year by mutableStateOf("")
    var hall by mutableStateOf("")
    var booth by mutableStateOf("")

    override val hasCustomView = true

    @Composable
    override fun fieldText() = stringResource(R.string.art_entry_source_convention)

    @Composable
    override fun DropdownItemText() = Text(fieldText())

    @Composable
    override fun Content(locked: Boolean?) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)
        ) {
            TextField(
                value = name,
                label = { Text(stringResource(R.string.art_entry_source_convention_label_name)) },
                placeholder = {
                    Text(stringResource(R.string.art_entry_source_convention_placeholder_name))
                },
                readOnly = locked == true,
                onValueChange = { name = it },
                modifier = Modifier
                    .focusable(locked != true)
                    .weight(1f, true),
            )
            TextField(
                value = year,
                label = { Text(stringResource(R.string.art_entry_source_convention_label_year)) },
                placeholder = {
                    Text(stringResource(R.string.art_entry_source_convention_placeholder_year))
                },
                readOnly = locked == true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = { year = it },
                modifier = Modifier
                    .focusable(locked != true)
                    .weight(1f, true),
            )
        }

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

class SourceCustomSectionItem : ArtEntrySection.Dropdown.Item {

    var value by mutableStateOf("")

    override val hasCustomView = true

    @Composable
    override fun fieldText() = stringResource(R.string.art_entry_source_custom)

    @Composable
    override fun DropdownItemText() = Text(fieldText())

    @Composable
    override fun Content(locked: Boolean?) {
        TextField(
            value = value,
            onValueChange = { value = it },
            readOnly = locked == true,
            modifier = Modifier
                .focusable(locked != true)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        )
    }
}