package com.thekeeperofpie.artistalleydatabase.art

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
                        is ArtEntrySection.Dropdown<*> -> Dropdown2Section(it)
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
        modifier = modifier.padding(top = 12.dp, bottom = 10.dp, start = 16.dp, end = 16.dp)
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
            if (it.isNotEmpty()) {
                section.contents += it
                section.pendingValue = ""
            }
        },
        {
            section.pendingValue = section.contents.removeLast()
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
    onBackspaceEmpty: () -> Unit,
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
                if (it.type == KeyEventType.KeyUp) {
                    return@onKeyEvent when (it.key) {
                        Key.Enter -> {
                            onDone(value)
                            true
                        }
                        Key.Backspace -> {
                            if (value.isEmpty()) {
                                onBackspaceEmpty()
                                true
                            } else false
                        }
                        else -> false
                    }
                } else false
            }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun <T> Dropdown2Section(section: ArtEntrySection.Dropdown<T>) {
    SectionHeader(stringResource(section.headerRes))

    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        val selectedItem = section.selectedItem()

        val (focusRequester) = FocusRequester.createRefs()
        val interactionSource = remember { MutableInteractionSource() }

        OutlinedTextField(
            value = selectedItem.fieldText(),
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    stringResource(R.string.art_entry_size_dropdown_content_description)
                )
            },
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    onClick = {
                        section.expanded = true
                        focusRequester.requestFocus()
                    },
                    interactionSource = interactionSource,
                    indication = null
                )
        )

        DropdownMenu(
            expanded = section.expanded,
            onDismissRequest = { section.expanded = false },
            Modifier.fillMaxWidth()
        ) {
            section.options.forEachIndexed { index, item ->
                DropdownMenuItem(
                    onClick = {
                        section.expanded = false
                        section.selectedIndex = index
                    },
                    text = { item.DropdownItemText() }
                )
            }
        }
    }

    section.selectedItem()
        .takeIf { it.hasCustomView }
        ?.Content()
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
            SizeDropdown().apply {
                selectedIndex = options.size - 1
            },
            ArtEntrySection.MultiText(
                R.string.add_entry_tags_header_zero,
                R.string.add_entry_tags_header_one,
                R.string.add_entry_tags_header_many,
            ).apply {
                contents.addAll(listOf("cute", "portrait"))
                pendingValue = "schoolgirl uniform"
            },
        )
    )
}

class SizeDropdown : ArtEntrySection.Dropdown<PrintSize>(
    R.string.add_entry_size_header,
    R.string.add_entry_size_label_width,
    R.string.add_entry_size_label_height,
    PrintSize.PORTRAITS
        .map { Item.Basic(it, it.textRes) }
        .plus(
            Item.TwoFields(
                R.string.add_entry_size_label_width,
                R.string.add_entry_size_label_height
            )
        )
        .toMutableStateList(),
) {

    @Composable
    override fun textOf(value: PrintSize) = stringResource(value.textRes)

    @Suppress("UNCHECKED_CAST")
    fun finalWidth() = when (val item = selectedItem()) {
        is Item.Basic<*> -> (item as Item.Basic<PrintSize>).value.printWidth
        is Item.TwoFields -> item.customValue0.toIntOrNull()
    }

    @Suppress("UNCHECKED_CAST")
    fun finalHeight() = when (val item = selectedItem()) {
        is Item.Basic<*> -> (item as Item.Basic<PrintSize>).value.printHeight
        is Item.TwoFields -> item.customValue1.toIntOrNull()
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