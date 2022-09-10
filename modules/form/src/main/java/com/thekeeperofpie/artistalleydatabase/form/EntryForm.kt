package com.thekeeperofpie.artistalleydatabase.form

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIcon
import com.thekeeperofpie.artistalleydatabase.compose.bottomBorder
import com.thekeeperofpie.artistalleydatabase.compose.dropdown.DropdownMenu
import com.thekeeperofpie.artistalleydatabase.compose.dropdown.DropdownMenuItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ColumnScope.EntryForm(
    areSectionsLoading: () -> Boolean = { false },
    sections: () -> List<EntrySection> = { emptyList() },
) {
    if (areSectionsLoading()) {
        CircularProgressIndicator(
            Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )
    } else {
        Column {
            sections().forEach {
                when (it) {
                    is EntrySection.MultiText -> MultiTextSection(it)
                    is EntrySection.LongText -> LongTextSection(it)
                    is EntrySection.Dropdown -> DropdownSection(it)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    text: @Composable () -> String,
    modifier: Modifier = Modifier,
    lockState: () -> EntrySection.LockState? = { null },
    onClick: () -> Unit = {},
) {
    Row(Modifier.clickable(true, onClick = onClick)) {
        Text(
            text = text(),
            style = MaterialTheme.typography.labelLarge,
            modifier = modifier
                .weight(1f, true)
                .padding(top = 12.dp, bottom = 10.dp, start = 16.dp, end = 16.dp)
        )

        @Suppress("NAME_SHADOWING")
        val lockState = lockState()
        if (lockState != null) {
            Icon(
                imageVector = when (lockState) {
                    EntrySection.LockState.LOCKED -> Icons.Default.Lock
                    EntrySection.LockState.UNLOCKED -> Icons.Default.LockOpen
                    EntrySection.LockState.DIFFERENT -> Icons.Default.LockReset
                },
                contentDescription = when (lockState) {
                    EntrySection.LockState.LOCKED -> R.string.lock_state_locked_content_description
                    EntrySection.LockState.UNLOCKED -> R.string.lock_state_unlocked_content_description
                    EntrySection.LockState.DIFFERENT -> R.string.lock_state_different_content_description
                }.let { stringResource(it) },
                modifier = Modifier.padding(top = 12.dp, bottom = 10.dp, start = 16.dp, end = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MultiTextSection(section: EntrySection.MultiText) {
    when (section.contentSize()) {
        0 -> section.headerZero
        1 -> section.headerOne
        else -> section.headerMany
    }
        .let { stringResource(it) }
        .let {
            SectionHeader(
                text = { it },
                lockState = { section.lockState },
                onClick = {
                    if (section.pendingValue.isNotEmpty()) {
                        section.addContent(section.pendingEntry())
                        section.pendingValue = ""
                    }
                    section.rotateLockState()
                }
            )
        }

    section.forEachContentIndexed { index, value ->
        var showOverflow by remember { mutableStateOf(false) }
        Box {
            PrefilledSectionField(
                index,
                value,
                onValueChange = {
                    val entry = section.content(index)
                    if (entry.text != it) {
                        section.setContent(index, EntrySection.MultiText.Entry.Custom(it))
                    }
                },
                onClickMore = { showOverflow = !showOverflow },
                onDone = {
                    section.addContent(
                        index,
                        EntrySection.MultiText.Entry.Custom("")
                    )
                },
                lockState = { section.lockState }
            )

            Box(
                Modifier
                    .width(48.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp)
            ) {
                androidx.compose.material3.DropdownMenu(
                    expanded = showOverflow,
                    onDismissRequest = { showOverflow = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            section.removeContentAt(index)
                            showOverflow = false
                        }
                    )
                    if (index > 0) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.move_up)) },
                            onClick = {
                                section.swapContent(index, index - 1)
                                showOverflow = false
                            }
                        )
                    }
                    if (index < section.contentSize() - 1) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.move_down)) },
                            onClick = {
                                section.swapContent(index, index + 1)
                                showOverflow = false
                            }
                        )
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = section.lockState?.editable != false,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Box {
            val focusRequester = remember { FocusRequester() }
            val bringIntoViewRequester = remember { BringIntoViewRequester() }
            val coroutineScope = rememberCoroutineScope()
            var focused by rememberSaveable { mutableStateOf(false) }
            OpenSectionField(
                value = { section.pendingValue },
                onValueChange = { section.pendingValue = it },
                onDone = {
                    if (it.isNotEmpty()) {
                        section.addContent(EntrySection.MultiText.Entry.Custom(it))
                        section.pendingValue = ""
                    }
                },
                lockState = { section.lockState },
                modifier = Modifier
                    .focusable(section.lockState?.editable != false)
                    .onFocusChanged { focused = it.isFocused }
                    .focusRequester(focusRequester)
                    .bringIntoViewRequester(bringIntoViewRequester)
            )

            if (section.lockState?.editable != false
                && section.pendingValue.isNotBlank()
                && section.predictions.isNotEmpty()
            ) {
                DropdownMenu(
                    expanded = focused,
                    onDismissRequest = { focusRequester.freeFocus() },
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    items(section.predictions.size, key = {
                        when (val entry = section.predictions[it]) {
                            is EntrySection.MultiText.Entry.Custom -> entry.text
                            EntrySection.MultiText.Entry.Different -> entry
                            is EntrySection.MultiText.Entry.Prefilled<*> -> entry.id
                        }
                    }) {
                        val entry = section.predictions[it]
                        DropdownMenuItem(
                            onClick = {
                                focusRequester.requestFocus()
                                section.onPredictionChosen(it)
                                coroutineScope.launch {
                                    delay(500)
                                    bringIntoViewRequester.bringIntoView()
                                }
                            },
                            text = {
                                val titleText: @Composable () -> String
                                val subtitleText: () -> String?
                                val image: () -> String?
                                val imageLink: () -> String?
                                when (entry) {
                                    is EntrySection.MultiText.Entry.Custom -> {
                                        titleText = { entry.text }
                                        subtitleText = { null }
                                        image = { null }
                                        imageLink = { null }
                                    }
                                    is EntrySection.MultiText.Entry.Prefilled<*> -> {
                                        titleText = { entry.titleText }
                                        subtitleText = { entry.subtitleText }
                                        image = { entry.image }
                                        imageLink = { entry.imageLink }
                                    }
                                    EntrySection.MultiText.Entry.Different -> {
                                        titleText = { stringResource(R.string.different) }
                                        subtitleText = { null }
                                        image = { null }
                                        imageLink = { null }
                                    }
                                }.run { /*exhaust*/ }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.animateContentSize(),
                                ) {
                                    EntryImage(
                                        image = image,
                                        link = imageLink,
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .heightIn(min = 54.dp)
                                            .width(42.dp)
                                    )
                                    Column(
                                        Modifier
                                            .weight(1f, true)
                                            .padding(
                                                start = 16.dp,
                                                end = 16.dp,
                                                top = 8.dp,
                                                bottom = 8.dp,
                                            )
                                    ) {
                                        Text(
                                            text = titleText(),
                                            maxLines = 1,
                                            style = MaterialTheme.typography.labelLarge
                                        )

                                        @Suppress("NAME_SHADOWING")
                                        val subtitleText = subtitleText()
                                        if (subtitleText != null) {
                                            Text(
                                                text = subtitleText,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(start = 24.dp)
                                            )
                                        }
                                    }

                                    entry.trailingIcon?.let { imageVector ->
                                        Icon(
                                            imageVector = imageVector,
                                            contentDescription = entry.trailingIconContentDescription
                                                ?.let { stringResource(it) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LongTextSection(section: EntrySection.LongText) {
    SectionHeader(
        text = { stringResource(section.headerRes) },
        lockState = { section.lockState },
        onClick = { section.rotateLockState() }
    )

    OutlinedTextField(
        value = section.value,
        onValueChange = { section.value = it },
        readOnly = section.lockState?.editable == false,
        modifier = Modifier
            .focusable(section.lockState?.editable != false)
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PrefilledSectionField(
    index: Int,
    entry: EntrySection.MultiText.Entry,
    onValueChange: (value: String) -> Unit = {},
    onClickMore: () -> Unit = {},
    onDone: () -> Unit = {},
    lockState: () -> EntrySection.LockState? = { null },
) {
    val backgroundShape =
        if (index == 0) RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp) else RectangleShape

    when (entry) {
        is EntrySection.MultiText.Entry.Custom -> {
            @Suppress("NAME_SHADOWING")
            val lockState = lockState()
            TextField(
                value = entry.text,
                onValueChange = { onValueChange(it) },
                readOnly = lockState?.editable == false,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                trailingIcon = {
                    AnimatedVisibility(
                        visible = lockState?.editable != false,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        IconButton(onClick = onClickMore) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(
                                    R.string.more_actions_content_description
                                ),
                            )
                        }
                    }
                },
                modifier = Modifier
                    .focusable(lockState?.editable != false)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                    .onKeyEvent {
                        if (it.type == KeyEventType.KeyUp && it.key == Key.Enter) {
                            onDone()
                            true
                        } else false
                    }
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = backgroundShape,
                    )
            )
        }
        is EntrySection.MultiText.Entry.Prefilled<*> -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .height(IntrinsicSize.Min)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = backgroundShape,
                    )
                    .bottomBorder(1.dp, MaterialTheme.colorScheme.onSurfaceVariant)
                    .animateContentSize(),
            ) {
                EntryImage(
                    image = { entry.image },
                    link = { entry.imageLink },
                    modifier = Modifier
                        .fillMaxHeight()
                        .heightIn(min = 72.dp)
                        .width(56.dp)
                        .run {
                            if (index == 0) clip(RoundedCornerShape(topStart = 4.dp)) else this
                        }
                )

                Column(
                    Modifier
                        .weight(1f, true)
                        .padding(16.dp)
                ) {
                    Text(text = entry.titleText)
                    if (entry.subtitleText != null) {
                        Text(
                            text = entry.subtitleText,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(start = 24.dp)
                        )
                    }
                }

                @Suppress("NAME_SHADOWING")
                val lockState = lockState()
                val editable = lockState?.editable != false
                entry.trailingIcon?.let { imageVector ->
                    Icon(
                        imageVector = imageVector,
                        contentDescription = entry.trailingIconContentDescription
                            ?.let { stringResource(it) },
                    )
                }

                if (entry.trailingIcon == null) {
                    val alpha by animateFloatAsState(
                        targetValue = if (editable) 1f else 0f,
                    )
                    IconButton(
                        onClick = onClickMore,
                        Modifier.alpha(alpha),
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(
                                R.string.more_actions_content_description
                            ),
                        )
                    }
                } else {
                    AnimatedVisibility(
                        visible = editable,
                        enter = expandHorizontally(
                            expandFrom = Alignment.CenterHorizontally
                        ) + fadeIn(),
                        exit = shrinkHorizontally(
                            shrinkTowards = Alignment.CenterHorizontally
                        ) + fadeOut(),
                    ) {
                        IconButton(onClick = onClickMore) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(
                                    R.string.more_actions_content_description
                                ),
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = !editable,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally(),
                    ) {
                        Spacer(Modifier.width(16.dp))
                    }
                }
            }
        }
        EntrySection.MultiText.Entry.Different -> {
            TextField(
                value = stringResource(R.string.different),
                onValueChange = {},
                readOnly = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        entry.trailingIcon?.let { imageVector ->
                            @Suppress("NAME_SHADOWING")
                            val lockState = lockState()
                            Icon(
                                imageVector = imageVector,
                                contentDescription = entry.trailingIconContentDescription
                                    ?.let { stringResource(it) },
                                modifier = if (lockState?.editable != false) Modifier else
                                    Modifier.padding(start = 16.dp),
                            )
                        }

                        IconButton(onClick = onClickMore) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(
                                    R.string.more_actions_content_description
                                ),
                            )
                        }
                    }
                },
                modifier = Modifier
                    .focusable(false)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = backgroundShape,
                    )
            )
        }
    }
}

@Composable
fun EntryImage(
    image: () -> String?,
    modifier: Modifier = Modifier,
    link: () -> String?,
) {
    val uriHandler = LocalUriHandler.current
    Box(
        modifier
            .clickable(
                onClick = { link()?.let { uriHandler.openUri(it) } },
                onClickLabel = stringResource(R.string.label_open_entry_link),
                role = Role.Image,
            )
            .animateContentSize()
    ) {
        var showPlaceholder by rememberSaveable { mutableStateOf(true) }

        @Suppress("NAME_SHADOWING")
        val image = image()
        if (showPlaceholder || image == null) {
            Spacer(
                Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
                    .alpha(0.38f)
            )
        }

        if (image != null) {
            AsyncImage(
                model =
                ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.entry_image_content_description),
                onLoading = { showPlaceholder = true },
                onSuccess = { showPlaceholder = false },
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .matchParentSize()
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun OpenSectionField(
    value: () -> String,
    modifier: Modifier = Modifier,
    onValueChange: (value: String) -> Unit = {},
    onDone: (value: String) -> Unit = {},
    lockState: () -> EntrySection.LockState? = { null },
) {
    @Suppress("NAME_SHADOWING")
    val value = value()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = lockState()?.editable == false,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSection(section: EntrySection.Dropdown) {
    SectionHeader(
        text = { stringResource(section.headerRes) },
        lockState = { section.lockState },
        onClick = { section.rotateLockState() }
    )

    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        val editable = section.lockState?.editable != false
        ExposedDropdownMenuBox(
            expanded = section.expanded && editable,
            onExpandedChange = {
                if (editable) {
                    section.expanded = !section.expanded
                }
            },
            Modifier.fillMaxWidth()
        ) {
            TextField(
                readOnly = true,
                value = section.selectedItem().fieldText(),
                onValueChange = { },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = editable,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        TrailingDropdownIcon(
                            expanded = section.expanded,
                            contentDescription = section.arrowContentDescription,
                        )
                    }
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = section.expanded,
                onDismissRequest = { section.expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                section.options.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        onClick = {
                            section.expanded = false
                            section.selectedIndex = index
                        },
                        text = { item.DropdownItemText() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    section.selectedItem()
        .takeIf { it.hasCustomView }
        ?.Content(section.lockState)
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