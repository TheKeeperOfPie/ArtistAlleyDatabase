package com.thekeeperofpie.artistalleydatabase.art

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.R

@Composable
fun ColumnScope.ArtEntryForm(
    areSectionsLoading: Boolean = false,
    sections: List<ArtEntrySection> = emptyList(),
) {
    Crossfade(
        targetState = areSectionsLoading,
        Modifier
            .fillMaxWidth()
    ) {
        if (it) {
            CircularProgressIndicator(
                Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            Column {

                sections.forEach {
                    when (it) {
                        is ArtEntrySection.MultiText -> MultiTextSection(it)
                        is ArtEntrySection.MultiDropdown2<*> -> Dropdown2Section(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier.padding(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
    )
}

@Composable
private fun MultiTextSection(section: ArtEntrySection.MultiText) {
    when (section.contents.size) {
        0 -> section.headerZero
        1 -> section.headerOne
        else -> section.headerMany
    }
        .let { stringResource(it) }
        .let { SectionHeader(it) }

    section.contents.forEachIndexed { index, value ->
        PrefilledSectionField(
            value,
            onValueChange = {
                section.contents[index] = it
            },
            onValueDelete = {
                section.contents.removeAt(index)
            })
    }
    OpenSectionField(
        section.pendingValue,
        { section.pendingValue = it },
        {
            section.contents += it
            section.pendingValue = ""
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PrefilledSectionField(
    value: String,
    onValueChange: (value: String) -> Unit = {},
    onValueDelete: () -> Unit = {},
) {
    TextField(
        value = value,
        onValueChange = { onValueChange(it) },
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .onKeyEvent {
                if (it.type == KeyEventType.KeyUp && it.key == Key.Backspace) {
                    if (value.isEmpty()) {
                        onValueDelete()
                    }
                    true
                } else false
            }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun OpenSectionField(
    value: String,
    onValueChange: (value: String) -> Unit,
    onDone: (value: String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone(value) }),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .onKeyEvent {
                if (it.type == KeyEventType.KeyUp && it.key == Key.Enter) {
                    onDone(value)
                    true
                } else false
            }
    )
}

@Composable
private fun <T> Dropdown2Section(section: ArtEntrySection.MultiDropdown2<T>) {
    SectionHeader(stringResource(section.headerRes))

    Box(
        Modifier
            .clickable { section.expanded = true }
            .fillMaxWidth()
    ) {
        val selectedItemText = if (section.selectedOption < section.predefinedOptions.size) {
            section.textOf(section.predefinedOptions[section.selectedOption])
        } else {
            stringResource(R.string.custom)
        }

        Text(
            selectedItemText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
        )

        DropdownMenu(
            expanded = section.expanded,
            onDismissRequest = { section.expanded = false },
            Modifier.fillMaxWidth()
        ) {
            section.predefinedOptions.forEachIndexed { index, item ->
                DropdownMenuItem(
                    onClick = {
                        section.expanded = false
                        section.selectedOption = index
                    },
                    text = { Text(section.textOf(item)) }
                )
            }

            DropdownMenuItem(
                onClick = {
                    section.expanded = false
                    section.selectedOption = section.predefinedOptions.size
                },
                text = { Text(stringResource(R.string.custom)) }
            )
        }
    }
}

@Composable
fun ImagesSelectBox(
    onImagesSelected: (List<Uri>) -> Unit,
    onImageSelectError: (Exception?) -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val imageSelectLauncher = rememberLauncherForActivityResult(
        GetMultipleContentsChooser,
        onImagesSelected
    )

    ImageSelectBoxInner(
        launcher = imageSelectLauncher,
        onImageSelectError = onImageSelectError,
        content
    )
}

@Composable
fun ImageSelectBox(
    onImageSelected: (Uri?) -> Unit,
    onImageSelectError: (Exception?) -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val imageSelectLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
        onImageSelected
    )

    ImageSelectBoxInner(
        launcher = imageSelectLauncher,
        onImageSelectError = onImageSelectError,
        content
    )
}

@Composable
private fun ImageSelectBoxInner(
    launcher: ManagedActivityResultLauncher<String, *>,
    onImageSelectError: (Exception?) -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        Modifier
            .wrapContentHeight()
            .heightIn(0.dp, 400.dp)
            .verticalScroll(rememberScrollState())
            .clickable(onClick = {
                try {
                    launcher.launch("image/*")
                } catch (e: Exception) {
                    onImageSelectError(e)
                }
            })
    ) {
        content()
    }
}

private object GetMultipleContentsChooser : ActivityResultContracts.GetMultipleContents() {
    @CallSuper
    override fun createIntent(context: Context, input: String): Intent {
        return Intent.createChooser(super.createIntent(context, input), null)
    }
}

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

    open class MultiDropdown2<T>(
        @StringRes val headerRes: Int,
        var predefinedOptions: SnapshotStateList<T> = mutableStateListOf()
    ) : ArtEntrySection() {
        var expanded by mutableStateOf(false)
        var selectedOption by mutableStateOf(0)
        var customValue0 by mutableStateOf("")
        var customValue1 by mutableStateOf("")

        @Composable
        open fun textOf(value: T) = value.toString()
    }
}

class SampleArtEntrySectionsProvider : PreviewParameterProvider<List<ArtEntrySection>> {
    override val values = sequenceOf(
        listOf(
            ArtEntrySection.MultiText(
                R.string.add_entry_artists_header_zero,
                R.string.add_entry_artists_header_one,
                R.string.add_entry_artists_header_many,
                "Lucidsky"
            ),
            ArtEntrySection.MultiText(
                R.string.add_entry_locations_header_zero,
                R.string.add_entry_locations_header_one,
                R.string.add_entry_locations_header_many,
                "Fanime 2022"
            ),
            ArtEntrySection.MultiText(
                R.string.add_entry_series_header_zero,
                R.string.add_entry_series_header_one,
                R.string.add_entry_series_header_many,
                "Dress Up Darling"
            ),
            ArtEntrySection.MultiText(
                R.string.add_entry_characters_header_zero,
                R.string.add_entry_characters_header_one,
                R.string.add_entry_characters_header_many,
                "Marin Kitagawa"
            ),
            ArtEntrySection.MultiText(
                R.string.add_entry_tags_header_zero,
                R.string.add_entry_tags_header_one,
                R.string.add_entry_tags_header_many,
            ).apply {
                contents.addAll(listOf("cute", "portrait"))
                pendingValue = "schoolgirl uniform"
            },
            SizeDropdown(
                predefinedOptions = mutableStateListOf(*PrintSize.PORTRAITS.toTypedArray())
            )
        )
    )
}

class SizeDropdown(
    predefinedOptions: SnapshotStateList<PrintSize> = mutableStateListOf(),
) : ArtEntrySection.MultiDropdown2<PrintSize>(
    R.string.add_entry_size_header,
    predefinedOptions
) {

    @Composable
    override fun textOf(value: PrintSize) = stringResource(value.textRes)

    fun finalWidth() = if (selectedOption == predefinedOptions.size) {
        customValue0.toIntOrNull()
    } else {
        predefinedOptions[selectedOption].printWidth
    }

    fun finalHeight() = if (selectedOption == predefinedOptions.size) {
        customValue0.toIntOrNull()
    } else {
        predefinedOptions[selectedOption].printHeight
    }
}

@Preview
@Composable
fun Preview(
    @PreviewParameter(SampleArtEntrySectionsProvider::class) sections: List<ArtEntrySection>
) {
    Column {
        ArtEntryForm(
            sections = sections
        )
    }
}