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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.compose.ui.window.PopupProperties
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
                        is ArtEntrySection.LongText -> LongTextSection(it)
                        is ArtEntrySection.Dropdown -> DropdownSection(it)
                    }
                }

                Spacer(Modifier.height(24.dp))
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

    Box {
        val focusRequester = remember { FocusRequester() }
        OpenSectionField(
            value = section.pendingValue,
            onValueChange = { section.pendingValue = it },
            onDone = {
                if (it.isNotEmpty()) {
                    section.contents += it
                    section.pendingValue = ""
                }
            },
            onBackspaceEmpty = {
                if (section.contents.isNotEmpty()) {
                    section.pendingValue = section.contents.removeLast()
                }
            },
            modifier = Modifier
                .onFocusChanged { section.focused = it.isFocused }
                .focusRequester(focusRequester)
        )

        if (section.predictions.isNotEmpty()) {
            DropdownMenu(
                expanded = section.focused,
                onDismissRequest = { focusRequester.freeFocus() },
                properties = PopupProperties(focusable = false),
                modifier = Modifier.fillMaxWidth()
            ) {
                section.predictions.forEach {
                    DropdownMenuItem(
                        onClick = {
                            focusRequester.freeFocus()
                            section.focused = false
                            section.pendingValue = it
                        },
                        text = { Text(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LongTextSection(section: ArtEntrySection.LongText) {
    SectionHeader(stringResource(section.headerRes))

    OpenSectionField(
        value = section.value,
        onValueChange = { section.value = it },
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
    modifier: Modifier = Modifier,
    onValueChange: (value: String) -> Unit = {},
    onDone: (value: String) -> Unit = {},
    onBackspaceEmpty: () -> Unit = {},
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone(value) }),
        modifier = modifier
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
private fun DropdownSection(section: ArtEntrySection.Dropdown) {
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
                    stringResource(section.arrowContentDescription)
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
                R.string.art_entry_artists_header_zero,
                R.string.art_entry_artists_header_one,
                R.string.art_entry_artists_header_many,
                "Lucidsky"
            ),
            ArtEntrySection.MultiText(
                R.string.art_entry_locations_header_zero,
                R.string.art_entry_locations_header_one,
                R.string.art_entry_locations_header_many,
                "Fanime 2022"
            ),
            ArtEntrySection.MultiText(
                R.string.art_entry_series_header_zero,
                R.string.art_entry_series_header_one,
                R.string.art_entry_series_header_many,
                "Dress Up Darling"
            ),
            ArtEntrySection.MultiText(
                R.string.art_entry_characters_header_zero,
                R.string.art_entry_characters_header_one,
                R.string.art_entry_characters_header_many,
                "Marin Kitagawa"
            ),
            PrintSizeDropdown().apply {
                selectedIndex = options.size - 1
            },
            ArtEntrySection.MultiText(
                R.string.art_entry_tags_header_zero,
                R.string.art_entry_tags_header_one,
                R.string.art_entry_tags_header_many,
            ).apply {
                contents.addAll(listOf("cute", "portrait"))
                pendingValue = "schoolgirl uniform"
            },
        )
    )
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