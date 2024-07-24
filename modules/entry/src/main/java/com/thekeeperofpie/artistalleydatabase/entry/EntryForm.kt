@file:Suppress("NAME_SHADOWING")
@file:OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class
)

package com.thekeeperofpie.artistalleydatabase.entry

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIcon
import com.thekeeperofpie.artistalleydatabase.compose.ZoomPanState
import com.thekeeperofpie.artistalleydatabase.compose.bottomBorder
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.optionalClickable
import com.thekeeperofpie.artistalleydatabase.compose.rememberZoomPanState
import kotlinx.coroutines.launch

@Composable
fun EntryForm(
    modifier: Modifier = Modifier,
    areSectionsLoading: () -> Boolean = { false },
    sections: () -> List<EntrySection> = { emptyList() },
    onNavigate: (String) -> Unit,
) {
    Box(modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState = areSectionsLoading(),
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = 200, delayMillis = 150)) togetherWith
                        fadeOut(animationSpec = tween(100))
            },
            label = "Entry form section fade in",
        ) {
            if (it) {
                // TODO: Remove this in favor of bottom sheet?
                // This forces it to be bigger than the screen so that it
                // covers all of the previous screen content.
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(2000.dp)) {
                    CircularProgressIndicator(
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.TopCenter)
                    )
                }
            } else {
                Column {
                    val sections = sections()
                    val sectionFocusRequesters = remember(sections) {
                        sections.map { FocusRequester() }
                    }
                    val onFocusNext = { index: Int ->
                        try {
                            (index + 1 until sections.size)
                                .find {
                                    when (val nextSection = sections[it]) {
                                        is EntrySection.Custom<*> -> false
                                        is EntrySection.Dropdown,
                                        is EntrySection.LongText,
                                        is EntrySection.MultiText,
                                        -> nextSection.lockState?.editable != false
                                    }
                                }
                                ?.let { sectionFocusRequesters[it] }
                                ?.requestFocus()
                        } catch (ignored: Throwable) {
                            // FocusRequester will throw if it isn't attached
                        }
                    }
                    val onFocusPrevious = { index: Int ->
                        try {
                            val focusRequester = (index - 1 downTo 0)
                                .find {
                                    // TODO: Previous will only move if the section is unlocked,
                                    //  which means it's not actually possible to navigate back to
                                    //  a section after committing one with the next button. It's
                                    //  not clear if this should be changed, because the idea of
                                    //  locking is that it should be a difficult operation to unlock
                                    when (val previousSection = sections[it]) {
                                        is EntrySection.Custom<*> -> false
                                        is EntrySection.Dropdown,
                                        is EntrySection.LongText,
                                        is EntrySection.MultiText,
                                        -> previousSection.lockState?.editable != false
                                    }
                                }
                                ?.let { sectionFocusRequesters[it] }
                            focusRequester?.requestFocus()
                            focusRequester != null
                        } catch (ignored: Throwable) {
                            // FocusRequester will throw if it isn't attached
                            false
                        }
                    }
                    sections.forEachIndexed { index, section ->
                        val focusRequester = sectionFocusRequesters[index]
                        when (section) {
                            is EntrySection.MultiText -> {
                                MultiTextSection(
                                    section = section,
                                    focusRequester = focusRequester,
                                    onNavigate = onNavigate,
                                    onFocusPrevious = { onFocusPrevious(index) },
                                    onFocusNext = { onFocusNext(index) },
                                )
                            }
                            is EntrySection.LongText -> LongTextSection(section, focusRequester)
                            is EntrySection.Dropdown -> DropdownSection(section, focusRequester)
                            // TODO: Custom section FocusRequester
                            is EntrySection.Custom<*> -> CustomSection(section)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
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
    @Suppress("NAME_SHADOWING")
    val lockState = lockState()
    Row(Modifier.clickable(lockState != null, onClick = onClick)) {
        Text(
            text = text(),
            style = MaterialTheme.typography.labelLarge,
            modifier = modifier
                .weight(1f, true)
                .padding(top = 12.dp, bottom = 10.dp, start = 16.dp, end = 16.dp)
        )

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
private fun MultiTextSection(
    section: EntrySection.MultiText,
    focusRequester: FocusRequester,
    onNavigate: (String) -> Unit,
    onFocusPrevious: () -> Boolean,
    onFocusNext: () -> Unit,
) {
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
                    if (section.pendingValue.text.isNotEmpty()) {
                        section.addContent(section.pendingEntry())
                        section.pendingValue = section.pendingValue.copy(text = "")
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
                lockState = { section.lockState },
                onNavigate = onNavigate,
                navRoute = section.navRoute,
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

    var focused by rememberSaveable { mutableStateOf(false) }
    AnimatedVisibility(
        // TODO: Allow showing open field even when locked in search panel
        visible = section.lockState?.editable != false,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        EntryPrefilledAutocompleteDropdown(
            text = { section.pendingValue.text },
            predictions = section.predictions,
            showPredictions = {
                section.lockState?.editable != false
                        && section.pendingValue.text.isNotBlank()
            },
            onPredictionChosen = section::onPredictionChosen,
        ) { bringIntoViewRequester ->
            OpenSectionField(
                section = section,
                onFocusPrevious = onFocusPrevious,
                onFocusNext = onFocusNext,
                bringIntoViewRequester = bringIntoViewRequester,
                focusRequester = focusRequester,
                onFocusChanged = { focused = it },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
            )
        }
    }
}

@Composable
private fun LongTextSection(section: EntrySection.LongText, focusRequester: FocusRequester) {
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
            .focusRequester(focusRequester)
            .focusable(section.lockState?.editable != false)
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}

@Composable
private fun PrefilledSectionField(
    index: Int,
    entry: EntrySection.MultiText.Entry,
    onValueChange: (value: String) -> Unit = {},
    onClickMore: () -> Unit = {},
    onDone: () -> Unit = {},
    lockState: () -> EntrySection.LockState? = { null },
    onNavigate: (String) -> Unit,
    // TODO: Move to a higher level shared navigation callback (currently anime module only)
    navRoute: ((EntrySection.MultiText.Entry) -> String)? = null,
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
                    Row {
                        if (navRoute != null) {
                            IconButton(onClick = { onNavigate(navRoute(entry)) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = stringResource(
                                        R.string.entry_open_more_content_description
                                    ),
                                )
                            }
                        }

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
                    .bottomBorder(MaterialTheme.colorScheme.onSurfaceVariant)
                    .animateContentSize(),
            ) {
                EntryImage(
                    image = { entry.image },
                    link = { entry.imageLink },
                    modifier = Modifier
                        .fillMaxHeight()
                        .heightIn(min = 72.dp)
                        .width(56.dp)
                        .conditionally(index == 0) {
                            clip(RoundedCornerShape(topStart = 4.dp))
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

                val secondaryImage = entry.secondaryImage
                if (secondaryImage != null) {
                    EntryImage(
                        image = { secondaryImage },
                        link = { entry.secondaryImageLink },
                        modifier = Modifier
                            .fillMaxHeight()
                            .heightIn(min = 72.dp)
                            .width(56.dp)
                            .conditionally(index == 0) {
                                clip(RoundedCornerShape(topEnd = 4.dp))
                            }
                    )
                }

                if (entry.trailingIcon == null && secondaryImage == null) {
                    val alpha by animateFloatAsState(
                        targetValue = if (editable) 1f else 0f,
                        label = "Entry trailing icon alpha",
                    )
                    IconButton(
                        onClick = {
                            // Alpha doesn't disable click target, so check it manually
                            if (editable) {
                                onClickMore()
                            }
                        },
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

                    if (secondaryImage == null) {
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
    contentScale: ContentScale = ContentScale.FillWidth,
    uriHandler: UriHandler = LocalUriHandler.current,
) {
    Box(
        modifier
            .optionalClickable(
                onClick = link()?.let { { uriHandler.openUri(it) } },
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
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.entry_image_content_description),
                onLoading = { showPlaceholder = true },
                onSuccess = { showPlaceholder = false },
                contentScale = contentScale,
                modifier = Modifier
                    .matchParentSize()
            )
        }
    }
}

@Composable
private fun OpenSectionField(
    section: EntrySection.MultiText,
    onFocusPrevious: () -> Boolean,
    onFocusNext: () -> Unit,
    bringIntoViewRequester: BringIntoViewRequester,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    OpenSectionField(
        value = { section.pendingValue },
        onValueChange = { section.pendingValue = it },
        onNext = {
            if (section.contentSize() > 0) {
                section.rotateLockState()
            }
            onFocusNext()
        },
        onBackspace = {
            if (section.contentSize() > 0) {
                val removed = section.removeContentAt(section.contentSize() - 1)
                section.pendingValue = TextFieldValue(
                    text = removed.text,
                    selection = TextRange(removed.text.length),
                )
                true
            } else {
                onFocusPrevious()
            }
        },
        onDone = {
            val text = section.pendingValue.text
            if (text.isNotEmpty()) {
                section.addContent(EntrySection.MultiText.Entry.Custom(text))
                section.pendingValue = section.pendingValue.copy(text = "")
            }
        },
        lockState = { section.lockState },
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { onFocusChanged(it.isFocused) }
            .bringIntoViewRequester(bringIntoViewRequester)
    )
}

@Composable
private fun OpenSectionField(
    value: () -> TextFieldValue,
    modifier: Modifier = Modifier,
    onValueChange: (value: TextFieldValue) -> Unit,
    onNext: KeyboardActionScope.() -> Unit,
    onBackspace: () -> Boolean,
    onDone: () -> Unit,
    lockState: () -> EntrySection.LockState?,
) {
    @Suppress("NAME_SHADOWING")
    val value = value()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = lockState()?.editable == false,
        keyboardOptions = KeyboardOptions(imeAction = if (value.text.isEmpty()) ImeAction.Next else ImeAction.Done),
        keyboardActions = KeyboardActions(onNext = onNext, onDone = { onDone() }),
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .onKeyEvent {
                if (it.type == KeyEventType.KeyUp) {
                    return@onKeyEvent when (it.key) {
                        Key.Backspace -> onBackspace()
                        Key.Enter -> {
                            onDone()
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
private fun DropdownSection(section: EntrySection.Dropdown, focusRequester: FocusRequester) {
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
                if (section.lockState?.editable != false) {
                    section.expanded = !section.expanded
                }
            },
            Modifier.fillMaxWidth()
        ) {
            TextField(
                readOnly = true,
                value = section.selectedItem().fieldText(),
                onValueChange = {},
                trailingIcon = {
                    AnimatedVisibility(
                        visible = editable,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        TrailingDropdownIcon(
                            expanded = section.expanded,
                            contentDescription = stringResource(section.arrowContentDescription),
                        )
                    }
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .menuAnchor(MenuAnchorType.PrimaryEditable)
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
private fun CustomSection(section: EntrySection.Custom<*>) {
    SectionHeader(
        text = { stringResource(section.headerRes()) },
        lockState = { section.lockState },
        onClick = { section.rotateLockState() }
    )

    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        section.Content(section.lockState)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MultiImageSelectBox(
    pagerState: PagerState,
    imageState: () -> EntryImageState,
    cropState: () -> CropUtils.CropState,
    onClickOpenImage: (index: Int) -> Unit,
    imageContent: @Composable (image: EntryImage, zoomPanState: ZoomPanState) -> Unit,
) {
    val imageSelectMultipleLauncher = rememberLauncherForActivityResult(
        GetMultipleContentsChooser,
        imageState().onAdded,
    )
    val imageSelectSingleLauncher = rememberLauncherForActivityResult(
        imageState().imageContentWithIndexChooser,
    ) { (index, uri) -> imageState().onSelected(index, uri) }

    val imageCropDocumentLauncher = if (cropState().imageCropNeedsDocument()) {
        rememberLauncherForActivityResult(
            object : ActivityResultContract<Int, Pair<Int, Uri?>>() {
                private var chosenIndex = 0

                @CallSuper
                override fun createIntent(context: Context, input: Int): Intent {
                    chosenIndex = input
                    return Intent(Intent.ACTION_CREATE_DOCUMENT)
                        .setType("image/png")
                        .putExtra(Intent.EXTRA_TITLE, CropUtils.CROP_IMAGE_FILE_NAME)
                        .putExtra(
                            DocumentsContract.EXTRA_INITIAL_URI,
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS
                            ).toUri()
                        )
                }

                override fun parseResult(resultCode: Int, intent: Intent?): Pair<Int, Uri?> {
                    return chosenIndex to intent.takeIf { resultCode == Activity.RESULT_OK }?.data
                }
            }
        ) { (index, uri) -> cropState().onImageCropDocumentChosen(index, uri) }
    } else null

    val imageCropLauncher = rememberLauncherForActivityResult(
        object : ActivityResultContract<Int, Int?>() {
            private var chosenIndex = 0

            override fun createIntent(context: Context, input: Int): Intent {
                chosenIndex = input
                return CropUtils.cropIntent()
            }

            override fun parseResult(resultCode: Int, intent: Intent?) =
                chosenIndex.takeIf { resultCode == Activity.RESULT_OK }
        }
    ) { cropState().onCropFinished(it) }

    val cropDocumentRequestedIndex = cropState().cropDocumentRequestedIndex()
    LaunchedEffect(cropDocumentRequestedIndex) {
        if (cropDocumentRequestedIndex != -1) {
            imageCropDocumentLauncher?.launch(cropDocumentRequestedIndex)
        }
    }

    val cropReadyIndex = cropState().cropReadyIndex()
    LaunchedEffect(cropReadyIndex) {
        if (cropReadyIndex != -1) {
            imageCropLauncher.launch(cropReadyIndex)
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    val zoomPanStates = remember { mutableMapOf<Int, ZoomPanState>() }
    val userScrollEnabled by remember {
        derivedStateOf {
            zoomPanStates[pagerState.currentPage]?.canPanExternal() != false
        }
    }
    val images = imageState().images()
    HorizontalPager(
        state = pagerState,
        userScrollEnabled = userScrollEnabled,
        modifier = Modifier.heightIn(max = 10000.dp)
    ) { index ->
        if (index == images.size) {
            AddImagePagerPage(
                onAddClick = { imageSelectMultipleLauncher.launch("image/*") },
            )
        } else {
            val image = images[index]
            val uri = image.croppedUri ?: image.uri
            if (uri == null) {
                // TODO: Null image placeholder
            } else {
                val coroutineScope = rememberCoroutineScope()
                val zoomPanState = rememberZoomPanState()
                DisposableEffect(image, index) {
                    zoomPanStates[index] = zoomPanState
                    onDispose {
                        zoomPanStates.remove(index)
                    }
                }
                Box(
                    modifier = Modifier
                        .wrapContentHeight()
                        .verticalScroll(rememberScrollState())
                        .pointerInput(zoomPanState) {
                            detectTapGestures(
                                onTap = { showMenu = true },
                                onLongPress = {
                                    val cropState = cropState()
                                    if (cropState.cropReadyIndex() == index) {
                                        imageCropLauncher.launch(index)
                                    } else {
                                        cropState.onImageRequestCrop(index)
                                    }
                                },
                                onDoubleTap = {
                                    coroutineScope.launch {
                                        zoomPanState.toggleZoom(it, size)
                                    }
                                },
                            )
                        }
                ) {
                    imageContent(image, zoomPanState)
                }
            }
        }
    }

    // TODO: Pager indicator? Might need to migrate back to Accompanist

    AnimatedVisibility(visible = showMenu, enter = fadeIn(), exit = fadeOut()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                .clickable { showMenu = false }
        ) {
            ElevatedCard(
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .padding(24.dp)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.entry_image_menu_option_open)) },
                    onClick = {
                        showMenu = false
                        onClickOpenImage(pagerState.currentPage)
                    },
                )

                Divider()

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.entry_image_menu_option_edit)) },
                    onClick = {
                        showMenu = false
                        try {
                            imageSelectSingleLauncher.launch(pagerState.currentPage)
                        } catch (e: Exception) {
                            imageState().onSelectError(e)
                        }
                    },
                )

                Divider()

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.entry_image_menu_option_crop)) },
                    onClick = {
                        showMenu = false
                        val currentPage = pagerState.currentPage
                        val cropState = cropState()
                        if (cropState.cropReadyIndex() == currentPage) {
                            imageCropLauncher.launch(currentPage)
                        } else {
                            cropState.onImageRequestCrop(currentPage)
                        }
                    },
                )
            }
        }
        BackHandler(showMenu) { showMenu = false }
    }
}

@Composable
private fun AddImagePagerPage(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clickable(onClick = onAddClick)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .fillMaxWidth()
            .height(400.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = stringResource(
                R.string.entry_add_image_content_description
            ),
            Modifier
                .size(48.dp)
                .align(Alignment.Center)
        )
    }
}

data class EntryImageState(
    val images: () -> List<EntryImage> = { emptyList() },
    val onSelected: (index: Int, Uri?) -> Unit = { _, _ -> },
    val onSelectError: (Exception?) -> Unit,
    val addAllowed: () -> Boolean = { false },
    val onAdded: (List<Uri>) -> Unit,
    val onSizeResult: (width: Int, height: Int) -> Unit = { _, _ -> },
) {
    val imageContentWithIndexChooser: ActivityResultContract<Int, Pair<Int, Uri?>> =
        GetImageContentWithIndexChooser()
}

private object GetMultipleContentsChooser : ActivityResultContracts.GetMultipleContents() {
    @CallSuper
    override fun createIntent(context: Context, input: String): Intent {
        return Intent.createChooser(super.createIntent(context, input), null)
    }
}

private class GetImageContentWithIndexChooser : ActivityResultContract<Int, Pair<Int, Uri?>>() {
    private var chosenIndex = 0

    @CallSuper
    override fun createIntent(context: Context, input: Int): Intent {
        chosenIndex = input
        return Intent.createChooser(
            Intent(Intent.ACTION_GET_CONTENT).addCategory(Intent.CATEGORY_OPENABLE)
                .setType("image/*"), null
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        chosenIndex to intent.takeIf { resultCode == Activity.RESULT_OK }?.data
}
