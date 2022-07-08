package com.thekeeperofpie.artistalleydatabase.art

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ColumnScope.ArtEntryForm(
    areSectionsLoading: Boolean = false,
    sections: List<ArtEntrySection> = emptyList(),
) {
    if (areSectionsLoading) {
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

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    locked: Boolean? = null,
    onClick: () -> Unit = {},
) {
    Row(Modifier.clickable(true, onClick = onClick)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = modifier
                .weight(1f, true)
                .padding(top = 12.dp, bottom = 10.dp, start = 16.dp, end = 16.dp)
        )

        if (locked != null) {
            Icon(
                imageVector = if (locked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = stringResource(R.string.art_entry_lock_content_description),
                modifier = Modifier.padding(top = 12.dp, bottom = 10.dp, start = 16.dp, end = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MultiTextSection(section: ArtEntrySection.MultiText) {
    when (section.contents.size) {
        0 -> section.headerZero
        1 -> section.headerOne
        else -> section.headerMany
    }
        .let { stringResource(it) }
        .let {
            SectionHeader(
                text = it,
                locked = section.locked,
                onClick = {
                    if (section.pendingValue.isNotEmpty()) {
                        section.contents += section.pendingEntry()
                        section.pendingValue = ""
                    }
                    section.locked = section.locked?.not()
                }
            )
        }

    section.contents.forEachIndexed { index, value ->
        var showOverflow by remember { mutableStateOf(false) }
        Box {
            PrefilledSectionField(
                value,
                onValueChange = {
                    val entry = section.contents[index]
                    if (entry.entryText != it) {
                        section.contents[index] = ArtEntrySection.MultiText.Entry(it)
                    }
                },
                onClickMore = { showOverflow = !showOverflow },
                onDone = { section.contents.add(index, ArtEntrySection.MultiText.Entry("")) },
                locked = section.locked
            )

            Box(
                Modifier
                    .width(48.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp)
            ) {
                DropdownMenu(
                    expanded = showOverflow,
                    onDismissRequest = { showOverflow = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            section.contents.removeAt(index)
                            showOverflow = false
                        }
                    )
                    if (index > 0) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.move_up)) },
                            onClick = {
                                val oldValue = section.contents[index]
                                section.contents[index] = section.contents[index - 1]
                                section.contents[index - 1] = oldValue
                                showOverflow = false
                            }
                        )
                    }
                    if (index < section.contents.size - 1) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.move_down)) },
                            onClick = {
                                val oldValue = section.contents[index]
                                section.contents[index] = section.contents[index + 1]
                                section.contents[index + 1] = oldValue
                                showOverflow = false
                            }
                        )
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = section.locked != true,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Box {
            val focusRequester = remember { FocusRequester() }
            val bringIntoViewRequester = remember { BringIntoViewRequester() }
            val coroutineScope = rememberCoroutineScope()
            var focused by remember { mutableStateOf(false) }
            OpenSectionField(
                value = section.pendingValue,
                onValueChange = { section.pendingValue = it },
                onDone = {
                    if (it.isNotEmpty()) {
                        section.contents += ArtEntrySection.MultiText.Entry(it)
                        section.pendingValue = ""
                    }
                },
                locked = section.locked,
                modifier = Modifier
                    .focusable(section.locked != true)
                    .onFocusChanged { focused = it.isFocused }
                    .focusRequester(focusRequester)
                    .bringIntoViewRequester(bringIntoViewRequester)
            )

            if (section.locked != true
                && section.pendingValue.isNotBlank()
                && section.predictions.isNotEmpty()
            ) {
                DropdownMenu(
                    expanded = focused,
                    onDismissRequest = { focusRequester.freeFocus() },
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    section.predictions.forEach {
                        DropdownMenuItem(
                            onClick = {
                                focusRequester.requestFocus()
                                section.contents += it
                                section.pendingValue = ""
                                coroutineScope.launch {
                                    delay(500)
                                    bringIntoViewRequester.bringIntoView()
                                }
                            },
                            text = {
                                Column {
                                    Text(
                                        text = it.titleText,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                    if (it.subtitleText != null) {
                                        Text(
                                            text = it.subtitleText,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(start = 24.dp)
                                        )
                                    }
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LongTextSection(section: ArtEntrySection.LongText) {
    SectionHeader(
        text = stringResource(section.headerRes),
        locked = section.locked,
        onClick = { section.locked = section.locked?.not() }
    )

    OutlinedTextField(
        value = section.value,
        onValueChange = { section.value = it },
        readOnly = section.locked == true,
        modifier = Modifier
            .focusable(section.locked != true)
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PrefilledSectionField(
    entry: ArtEntrySection.MultiText.Entry,
    onValueChange: (value: String) -> Unit = {},
    onClickMore: () -> Unit = {},
    onDone: () -> Unit = {},
    locked: Boolean? = null,
) {
    TextField(
        value = entry.entryText,
        onValueChange = { onValueChange(it) },
        readOnly = locked == true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        trailingIcon = {
            AnimatedVisibility(
                visible = locked != true,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                IconButton(onClick = onClickMore) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(
                            R.string.art_entry_more_actions_content_description
                        ),
                    )
                }
            }
        },
        modifier = Modifier
            .focusable(locked != true)
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .onKeyEvent {
                if (it.type == KeyEventType.KeyUp && it.key == Key.Enter) {
                    onDone()
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
    locked: Boolean? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = locked == true,
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
                        else -> false
                    }
                } else false
            }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DropdownSection(section: ArtEntrySection.Dropdown) {
    SectionHeader(
        text = stringResource(section.headerRes),
        locked = section.locked,
        onClick = { section.locked = section.locked?.not() }
    )

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
                        if (section.locked != true) {
                            section.expanded = true
                            focusRequester.requestFocus()
                        }
                    },
                    interactionSource = interactionSource,
                    indication = null
                )
        )

        if (section.locked != true) {
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
    }

    section.selectedItem()
        .takeIf { it.hasCustomView }
        ?.Content(section.locked)
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
                contents.addAll(
                    listOf(
                        ArtEntrySection.MultiText.Entry("cute"),
                        ArtEntrySection.MultiText.Entry("portrait")
                    )
                )
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
            sections = sections.apply {
                (first() as ArtEntrySection.MultiText).locked = true
            }
        )
    }
}