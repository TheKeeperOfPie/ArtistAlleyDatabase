@file:OptIn(ExperimentalMaterial3Api::class, FlowPreview::class, ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.entry.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.entry.generated.resources.Res
import artistalleydatabase.modules.entry.generated.resources.delete
import artistalleydatabase.modules.entry.generated.resources.different
import artistalleydatabase.modules.entry.generated.resources.different_indicator_content_description
import artistalleydatabase.modules.entry.generated.resources.entry_open_more_content_description
import artistalleydatabase.modules.entry.generated.resources.move_down
import artistalleydatabase.modules.entry.generated.resources.move_up
import artistalleydatabase.modules.utils_compose.generated.resources.more_actions_content_description
import com.thekeeperofpie.artistalleydatabase.entry.EntryImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.bottomBorder
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.swap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds
import artistalleydatabase.modules.utils_compose.generated.resources.Res as UtilsComposeRes

@LayoutScopeMarker
@Immutable
interface EntryFormScope : ColumnScope

private class EntryFormScopeImpl(columnScope: ColumnScope) : EntryFormScope,
    ColumnScope by columnScope

object EntryForm2 {

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        content: @Composable EntryFormScope.() -> Unit,
    ) {
        Column(modifier = modifier) {
            EntryFormScopeImpl(this).content()
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun EntryFormScope.MultiTextSection(
    state: EntryFormSection.MultiText,
    headerText: @Composable () -> Unit,
    focusRequester: FocusRequester,
    trailingIcon: (EntryFormSection.MultiText.Entry) -> Pair<ImageVector, StringResource>?,
    entryPredictions: suspend (String) -> Flow<List<EntryFormSection.MultiText.Entry>>,
    onNavigate: (EntryFormSection.MultiText.Entry) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
) {
    val onNewEntry: (EntryFormSection.MultiText.Entry) -> Unit = {
        Snapshot.withMutableSnapshot {
            state.content += it
            state.pendingNewValue = TextFieldValue("")
        }
    }
    SectionHeader(
        text = headerText,
        lockState = state.lockState,
        onClick = {
            val newValue = state.pendingNewValue.text
            if (newValue.isNotBlank()) {
                // TODO: Unify trim logic somewhere
                onNewEntry(EntryFormSection.MultiText.Entry.Custom(newValue.trim()))
            }
            state.lockState = state.lockState.rotateLockState(
                wasEverDifferent = state.initialLockState == EntryFormSection.LockState.DIFFERENT,
            )
        }
    )

    val focusManager = LocalFocusManager.current
    state.content.forEachIndexed { index, value ->
        var showOverflow by remember { mutableStateOf(false) }
        Box {
            val iconPair = trailingIcon(value)
            SectionField(
                index = index,
                entry = value,
                onValueChange = {
                    if (value.text != it) {
                        state.content[index] = EntryFormSection.MultiText.Entry.Custom(it.trim())
                    }
                },
                onClickMore = { showOverflow = !showOverflow },
                onDone = { focusManager.moveFocus(FocusDirection.Next) },
                lockState = { state.lockState },
                trailingIcon = if (iconPair != null) {
                    {
                        Icon(
                            imageVector = iconPair.first,
                            contentDescription = stringResource(iconPair.second),
                        )
                    }
                } else null,
                onNavigate = { onNavigate(value) },
            )

            EntryItemDropdown(
                show = { showOverflow },
                onDismiss = { showOverflow = false },
                index = index,
                totalSize = { state.content.size },
                onDelete = { state.content.removeAt(index) },
                onMoveUp = { state.content.swap(index, index - 1) },
                onMoveDown = { state.content.swap(index, index + 1) },
                modifier = Modifier
                    .width(48.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp)
            )
        }
    }

    AnimatedVisibility(
        // TODO: Allow showing open field even when locked in search panel
        visible = state.lockState?.editable != false,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        val predictions by produceState(emptyList(), state, entryPredictions) {
            snapshotFlow { state.pendingNewValue.text }
                .debounce(1.seconds)
                .flatMapLatest(entryPredictions)
                .collectLatest { value = it }
        }
        EntryPrefilledAutocompleteDropdown(
            text = { state.pendingNewValue },
            predictions = { predictions },
            showPredictions = { state.lockState?.editable != false && state.pendingNewValue.text.isNotBlank() },
            onPredictionChosen = onNewEntry,
            trailingIcon = {
                val iconPair = trailingIcon(it)
                if (iconPair != null) {
                    Icon(
                        imageVector = iconPair.first,
                        contentDescription = stringResource(iconPair.second),
                    )
                }
            },
        ) { bringIntoViewRequester ->
            OpenSectionField(
                value = { state.pendingNewValue },
                onValueChange = { state.pendingNewValue = it },
                lockState = { state.lockState },
                totalSize = { state.content.size },
                onLock = { state.lockState = EntryFormSection.LockState.LOCKED },
                bringIntoViewRequester = bringIntoViewRequester,
                focusRequester = focusRequester,
                onFocusChanged = { onFocusChanged(it) },
                onRemove = {
                    Snapshot.withMutableSnapshot {
                        val removed = state.content.removeLastOrNull()
                        if (removed != null) {
                            state.pendingNewValue = TextFieldValue(
                                text = removed.text,
                                selection = TextRange(removed.text.length)
                            )
                        }
                    }
                },
                onDone = {
                    val newValue = state.pendingNewValue.text
                    if (newValue.isNotBlank()) {
                        onNewEntry(EntryFormSection.MultiText.Entry.Custom(newValue.trim()))
                    }
                },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
            )
        }
    }
}

@Composable
private fun EntryItemDropdown(
    show: () -> Boolean,
    onDismiss: () -> Unit,
    index: Int,
    totalSize: () -> Int,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        DropdownMenu(
            expanded = show(),
            onDismissRequest = onDismiss,
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.delete)) },
                onClick = {
                    onDelete()
                    onDismiss()
                }
            )
            if (index > 0) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.move_up)) },
                    onClick = {
                        onMoveUp()
                        onDismiss()
                    }
                )
            }
            if (index < totalSize() - 1) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.move_down)) },
                    onClick = {
                        onMoveDown()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    lockState: EntryFormSection.LockState?,
    onClick: () -> Unit = {},
) {
    Row(modifier.clickable(lockState != null, onClick = onClick)) {
        Box(
            modifier = Modifier.weight(1f, true)
                .padding(top = 12.dp, bottom = 10.dp, start = 16.dp, end = 16.dp)
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.labelLarge
            ) {
                text()
            }
        }

        if (lockState != null) {
            Icon(
                imageVector = lockState.icon,
                contentDescription = stringResource(lockState.contentDescription),
                modifier = Modifier.padding(top = 12.dp, bottom = 10.dp, start = 16.dp, end = 16.dp)
            )
        }
    }
}

@Composable
private fun OpenSectionField(
    value: () -> TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    lockState: () -> EntryFormSection.LockState?,
    totalSize: () -> Int,
    onLock: () -> Unit,
    bringIntoViewRequester: BringIntoViewRequester,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
    onRemove: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier,
) {
    // TODO: There must be a better way to intercept previous value
    var previousValue by remember { mutableStateOf(value()) }
    val focusManager = LocalFocusManager.current
    OpenSectionField(
        value = value,
        onValueChange = onValueChange,
        onNext = {
            if (totalSize() > 0) {
                onLock()
            }
            focusManager.moveFocus(FocusDirection.Next)
        },
        onBackspace = {
            if (totalSize() > 0) {
                if (previousValue.text.isBlank() && value().text.isBlank()) {
                    onRemove()
                } else {
                    // Set the next value to ensure empty eventually propagates to previous
                    previousValue = value()
                }
                true
            } else {
                focusManager.moveFocus(FocusDirection.Previous)
            }
        },
        onDone = onDone,
        lockState = lockState,
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { onFocusChanged(it.isFocused) }
            .bringIntoViewRequester(bringIntoViewRequester)
    )
}

@Composable
private fun OpenSectionField(
    value: () -> TextFieldValue,
    onValueChange: (value: TextFieldValue) -> Unit,
    lockState: () -> EntryFormSection.LockState?,
    modifier: Modifier = Modifier,
    onNext: KeyboardActionScope.() -> Unit,
    onBackspace: () -> Boolean,
    onDone: () -> Unit,
) {
    @Suppress("NAME_SHADOWING")
    val value = value()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = lockState()?.editable == false,
        keyboardOptions = KeyboardOptions(imeAction = if (value.text.isBlank()) ImeAction.Next else ImeAction.Done),
        keyboardActions = KeyboardActions(onNext = onNext, onDone = { onDone() }),
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .onKeyEvent {
                if (it.type == KeyEventType.KeyUp) {
                    return@onKeyEvent when (it.key) {
                        Key.Backspace -> onBackspace()
                        Key.Tab, Key.Enter -> {
                            onDone()
                            true
                        }
                        else -> false
                    }
                } else false
            }
    )
}


@Composable
private fun SectionField(
    index: Int,
    entry: EntryFormSection.MultiText.Entry,
    onValueChange: (value: String) -> Unit,
    onClickMore: () -> Unit,
    onDone: () -> Unit,
    lockState: () -> EntryFormSection.LockState?,
    trailingIcon: @Composable (() -> Unit)?,
    onNavigate: ((EntryFormSection.MultiText.Entry) -> Unit)?,
) {
    when (entry) {
        is EntryFormSection.MultiText.Entry.Custom -> CustomText(
            entry = entry,
            index = index,
            lockState = lockState,
            onValueChange = onValueChange,
            onDone = onDone,
            onClickMore = onClickMore,
            onNavigate = onNavigate,
        )
        is EntryFormSection.MultiText.Entry.Prefilled<*> -> PrefilledText(
            entry = entry,
            index = index,
            lockState = lockState(),
            trailingIcon = trailingIcon,
            onClickMore = onClickMore,
            onNavigate = onNavigate,
        )
        EntryFormSection.MultiText.Entry.Different ->
            DifferentText(lockState = lockState, index = index, onClickMore = onClickMore)
    }
}

@Composable
private fun CustomText(
    entry: EntryFormSection.MultiText.Entry.Custom,
    index: Int,
    lockState: () -> EntryFormSection.LockState?,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    onClickMore: () -> Unit,
    onNavigate: ((EntryFormSection.MultiText.Entry) -> Unit)? = null,
) {
    val shape =
        if (index == 0) RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp) else RectangleShape

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
                if (onNavigate != null) {
                    IconButton(onClick = { onNavigate(entry) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = stringResource(
                                Res.string.entry_open_more_content_description
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
                                UtilsComposeRes.string.more_actions_content_description
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
                shape = shape,
            )
    )
}

@Composable
private fun PrefilledText(
    entry: EntryFormSection.MultiText.Entry.Prefilled<*>,
    index: Int,
    lockState: EntryFormSection.LockState?,
    trailingIcon: @Composable (() -> Unit)?,
    onClickMore: () -> Unit,
    onNavigate: ((EntryFormSection.MultiText.Entry) -> Unit)?,
) {
    val shape =
        if (index == 0) RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp) else RectangleShape
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .height(IntrinsicSize.Min)
            .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = shape)
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

        trailingIcon?.invoke()

        if (onNavigate != null) {
            IconButton(onClick = { onNavigate(entry) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = stringResource(
                        Res.string.entry_open_more_content_description
                    ),
                )
            }
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

        val editable = lockState?.editable != false
        if (trailingIcon == null && secondaryImage == null) {
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
                        UtilsComposeRes.string.more_actions_content_description
                    ),
                )
            }
        } else {
            AnimatedVisibility(
                visible = editable,
                enter = expandHorizontally(expandFrom = Alignment.CenterHorizontally) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally) + fadeOut(),
            ) {
                IconButton(onClick = onClickMore) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(
                            UtilsComposeRes.string.more_actions_content_description
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

@Composable
private fun DifferentText(
    lockState: () -> EntryFormSection.LockState?,
    index: Int,
    onClickMore: () -> Unit,
) {
    val shape =
        if (index == 0) RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp) else RectangleShape
    TextField(
        value = stringResource(Res.string.different),
        onValueChange = {},
        readOnly = true,
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ViewKanban,
                    contentDescription = stringResource(Res.string.different_indicator_content_description),
                    modifier = Modifier.conditionally(
                        lockState()?.editable == false,
                        Modifier.padding(start = 16.dp)
                    ),
                )

                IconButton(onClick = onClickMore) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(
                            UtilsComposeRes.string.more_actions_content_description
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
                shape = shape,
            )
    )
}

@Composable
private fun EntryPrefilledAutocompleteDropdown(
    text: () -> TextFieldValue?,
    predictions: () -> List<EntryFormSection.MultiText.Entry>,
    showPredictions: () -> Boolean,
    onPredictionChosen: (EntryFormSection.MultiText.Entry) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable ((EntryFormSection.MultiText.Entry) -> Unit)?,
    textField: @Composable ExposedDropdownMenuBoxScope.(BringIntoViewRequester) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        val bringIntoViewRequester = remember { BringIntoViewRequester() }
        textField(bringIntoViewRequester)

        if (showPredictions()) {
            val predictions = predictions()
            if (predictions.isNotEmpty()) {
                LaunchedEffect(text(), predictions) {
                    expanded = true
                }
                // DropdownMenu overrides the LocalUriHandler, so save it here and pass it down
                val uriHandler = LocalUriHandler.current
                val coroutineScope = rememberCoroutineScope()
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.heightIn(max = 240.dp)
                ) {
                    predictions.forEachIndexed { index, entry ->
                        DropdownMenuItem(
                            onClick = {
                                onPredictionChosen(entry)
                                coroutineScope.launch {
                                    // TODO: Delay is necessary or column won't scroll
                                    delay(500)
                                    bringIntoViewRequester.bringIntoView()
                                }
                            },
                            text = {
                                val titleText: @Composable () -> String
                                var subtitleText: () -> String? = { null }
                                var image: () -> String? = { null }
                                var imageLink: () -> String? = { null }
                                var secondaryImage: (() -> String?)? = null
                                var secondaryImageLink: () -> String? = { null }
                                when (entry) {
                                    is EntryFormSection.MultiText.Entry.Custom -> {
                                        titleText = { entry.text }
                                    }
                                    is EntryFormSection.MultiText.Entry.Prefilled<*> -> {
                                        titleText = { entry.titleText }
                                        subtitleText = { entry.subtitleText }
                                        image = { entry.image }
                                        imageLink = { entry.imageLink }
                                        secondaryImage = entry.secondaryImage?.let { { it } }
                                        secondaryImageLink = { entry.secondaryImageLink }
                                    }
                                    EntryFormSection.MultiText.Entry.Different -> {
                                        titleText = { stringResource(Res.string.different) }
                                    }
                                }.run { /*exhaust*/ }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.animateContentSize(),
                                ) {
                                    EntryImage(
                                        image = image,
                                        link = imageLink,
                                        uriHandler = uriHandler,
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
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
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

                                    trailingIcon?.invoke(entry)

                                    if (secondaryImage != null) {
                                        EntryImage(
                                            image = secondaryImage,
                                            link = secondaryImageLink,
                                            uriHandler = uriHandler,
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .heightIn(min = 54.dp)
                                                .width(42.dp)
                                        )
                                    }
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
fun EntryFormScope.LongTextSection(
    state: EntryFormSection.LongText,
    headerText: @Composable () -> Unit,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
) {
    SectionHeader(
        text = headerText,
        lockState = state.lockState,
        onClick = {
            state.lockState = state.lockState.rotateLockState(
                wasEverDifferent = state.initialLockState == EntryFormSection.LockState.DIFFERENT,
            )
        }
    )

    val editable = state.lockState?.editable
    OutlinedTextField(
        value = state.value,
        onValueChange = { state.value = it },
        readOnly = editable == false,
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { onFocusChanged(it.isFocused) }
            .focusable(editable != false)
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}
