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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
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
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
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
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.bottomBorder
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.swap
import com.thekeeperofpie.artistalleydatabase.utils_compose.text.isTabKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.text.isTabKeyDownOrTyped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds
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

    abstract class State {
        abstract var lockState: EntryLockState
        protected abstract val wasEverDifferent: Boolean

        fun rotateLockState() {
            lockState = lockState.rotateLockState(wasEverDifferent)
        }
    }

    // TODO: Should this be collapsed into SingleTextState?
    @Stable
    class PendingTextState(
        val pendingValue: TextFieldState = TextFieldState(),
        initialLockState: EntryLockState = EntryLockState.UNLOCKED,
        override var wasEverDifferent: Boolean = initialLockState == EntryLockState.DIFFERENT,
    ) : State() {
        override var lockState by mutableStateOf(initialLockState)
        val focusRequester = FocusRequester()
        var isFocused by mutableStateOf(false)

        object Saver : ComposeSaver<PendingTextState, Any> {
            override fun SaverScope.save(value: PendingTextState) = listOf(
                with(TextFieldState.Saver) { save(value.pendingValue) },
                value.lockState,
                value.wasEverDifferent,
            )

            override fun restore(value: Any): PendingTextState {
                val (pendingValue, pendingFocused, lockState, wasEverDifferent) = value as List<*>
                return PendingTextState(
                    pendingValue = with(TextFieldState.Saver) { restore(pendingValue!!) }!!,
                    initialLockState = lockState as EntryLockState,
                    wasEverDifferent = wasEverDifferent as Boolean,
                )
            }
        }
    }

    @Stable
    class SingleTextState(
        val value: TextFieldState = TextFieldState(),
        initialLockState: EntryLockState = EntryLockState.UNLOCKED,
    ) : State() {
        override var lockState by mutableStateOf(initialLockState)
        override val wasEverDifferent get() = false
        val focusRequester = FocusRequester()
        var isFocused by mutableStateOf(false)

        companion object {
            fun fromValue(value: String?) = SingleTextState(
                value = TextFieldState(value.orEmpty()),
                initialLockState = if (value.isNullOrBlank()) {
                    EntryLockState.UNLOCKED
                } else {
                    EntryLockState.LOCKED
                }
            )
        }

        object Saver : ComposeSaver<SingleTextState, Any> {
            override fun SaverScope.save(value: SingleTextState) = listOf(
                with(TextFieldState.Saver) { save(value.value) },
                value.lockState,
            )

            override fun restore(value: Any): SingleTextState {
                val (value, lockState) = value as List<*>
                return SingleTextState(
                    value = with(TextFieldState.Saver) { restore(value!!) }!!,
                    initialLockState = lockState as EntryLockState,
                )
            }
        }
    }

    object MultiTextState {

        @Serializable
        @Immutable
        sealed interface Entry {
            val id: String
            val text: String
            val serializedValue: String get() = text
            val searchableValue: String get() = text

            @Serializable
            data class Custom(override val text: String) : Entry {
                override val id = "custom_$text"
            }

            @Serializable
            data object Different : Entry {
                override val id = "different"
                override val text = ""
            }

            @Serializable
            data class Prefilled<T>(
                val value: T,
                override val id: String,
                override val text: String,
                val image: String? = null,
                val imageLink: String? = null,
                val secondaryImage: String? = null,
                val secondaryImageLink: String? = null,
                val titleText: String = text,
                val subtitleText: String? = null,
                override val serializedValue: String,
                override val searchableValue: String,
            ) : Entry
        }
    }

    @Stable
    class DropdownState(
        initialSelectedIndex: Int = 0,
        initialLockState: EntryLockState = EntryLockState.UNLOCKED,
        override var wasEverDifferent: Boolean = initialLockState == EntryLockState.DIFFERENT,
    ) : State() {
        var selectedIndex by mutableIntStateOf(initialSelectedIndex)
        var expanded by mutableStateOf(false)
        val focusRequester = FocusRequester()
        override var lockState by mutableStateOf(initialLockState)

        object Saver : ComposeSaver<DropdownState, Any> {
            override fun SaverScope.save(value: DropdownState): Any {
                return listOf(
                    value.selectedIndex,
                    value.lockState,
                    value.wasEverDifferent,
                )
            }

            override fun restore(value: Any): DropdownState {
                val (selectedIndex, lockState, wasEverDifferent) = value as List<*>
                return DropdownState(
                    initialSelectedIndex = selectedIndex as Int,
                    initialLockState = lockState as EntryLockState,
                    wasEverDifferent = wasEverDifferent as Boolean,
                )
            }
        }
    }
}

@Composable
fun EntryFormScope.SingleTextSection(
    state: EntryForm2.SingleTextState,
    title: StringResource,
    previousFocus: FocusRequester?,
    nextFocus: FocusRequester?,
    inputTransformation: InputTransformation? = null,
    errorText: (() -> String?)? = null,
) {
    SingleTextSection(
        state = state,
        headerText = { Text(stringResource(title)) },
        onTab = {
            val focusRequester = if (it) nextFocus else previousFocus
            focusRequester?.requestFocus()
        },
        inputTransformation = inputTransformation,
        errorText = errorText,
    )
}

@Suppress("UnusedReceiverParameter")
@Composable
fun EntryFormScope.SingleTextSection(
    state: EntryForm2.SingleTextState,
    headerText: @Composable () -> Unit,
    onTab: (next: Boolean) -> Unit = {},
    inputTransformation: InputTransformation? = null,
    errorText: (() -> String?)? = null,
) {
    Column {
        SectionHeader(
            text = headerText,
            lockState = state.lockState,
            onClick = state::rotateLockState,
        )

        val modifier = Modifier.fillMaxWidth()
            .onFocusChanged { state.isFocused = it.isFocused }
            .focusRequester(state.focusRequester)
            .padding(horizontal = 16.dp)
            .interceptTab(onTab)
        val errorText = errorText?.invoke()
        if (state.lockState == EntryLockState.UNLOCKED) {
            OutlinedTextField(
                state = state.value,
                supportingText = errorText?.let { { Text(it) } },
                isError = errorText != null,
                inputTransformation = inputTransformation,
                modifier = modifier
            )
        } else {
            TextField(
                state = state.value,
                readOnly = true,
                supportingText = errorText?.let { { Text(it) } },
                isError = errorText != null,
                inputTransformation = inputTransformation,
                modifier = modifier
            )
        }
    }
}

@Composable
fun EntryFormScope.MultiTextSection(
    state: EntryForm2.PendingTextState,
    headerText: @Composable () -> Unit,
    entryPredictions: suspend (String) -> Flow<List<EntryForm2.MultiTextState.Entry>> = { emptyFlow() },
    trailingIcon: (EntryForm2.MultiTextState.Entry) -> Pair<ImageVector, StringResource>? = { null },
    onNavigate: (EntryForm2.MultiTextState.Entry) -> Unit = {},
    items: SnapshotStateList<EntryForm2.MultiTextState.Entry>,
    onItemCommitted: (String) -> Unit,
    removeLastItem: () -> String?,
) {
    // DropdownMenu overrides the LocalUriHandler, so save it here and pass it down
    val uriHandler = LocalUriHandler.current
    MultiTextSection(
        state = state,
        headerText = headerText,
        entryPredictions = entryPredictions,
        items = items,
        onItemCommitted = {
            onItemCommitted(it)
            state.pendingValue.clearText()
        },
        removeLastItem = removeLastItem,
        item = { index, value ->
            var showOverflow by remember { mutableStateOf(false) }
            Box {
                val iconPair = trailingIcon(value)
                val focusManager = LocalFocusManager.current
                SectionField(
                    index = index,
                    entry = value,
                    onValueChange = {
                        if (value.text != it) {
                            items[index] = EntryForm2.MultiTextState.Entry.Custom(it.trim())
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
                    totalSize = { items.size },
                    onDelete = { items.removeAt(index) },
                    onMoveUp = { items.swap(index, index - 1) },
                    onMoveDown = { items.swap(index, index + 1) },
                    modifier = Modifier
                        .width(48.dp)
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp)
                )
            }
        },
        prediction = { _, entry ->
            val titleText: @Composable () -> String
            var subtitleText: () -> String? = { null }
            var image: () -> String? = { null }
            var imageLink: () -> String? = { null }
            var secondaryImage: (() -> String?)? = null
            var secondaryImageLink: () -> String? = { null }
            when (entry) {
                is EntryForm2.MultiTextState.Entry.Custom -> {
                    titleText = { entry.text }
                }
                is EntryForm2.MultiTextState.Entry.Prefilled<*> -> {
                    titleText = { entry.titleText }
                    subtitleText = { entry.subtitleText }
                    image = { entry.image }
                    imageLink = { entry.imageLink }
                    secondaryImage = entry.secondaryImage?.let { { it } }
                    secondaryImageLink = { entry.secondaryImageLink }
                }
                EntryForm2.MultiTextState.Entry.Different -> {
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
    )
}

@Composable
fun <T> EntryFormScope.MultiTextSection(
    state: EntryForm2.PendingTextState,
    headerText: @Composable () -> Unit,
    entryPredictions: suspend (String) -> Flow<List<T>> = { emptyFlow() },
    items: SnapshotStateList<T>,
    onItemCommitted: (String) -> Unit,
    removeLastItem: () -> String?,
    item: @Composable (index: Int, T) -> Unit,
    prediction: @Composable (index: Int, T) -> Unit = item,
    preferPrediction: Boolean = false,
    onTab: (next: Boolean) -> Unit = {},
) {
    SectionHeader(
        text = headerText,
        lockState = state.lockState,
        onClick = {
            val newValue = state.pendingValue.text
            if (newValue.isNotBlank()) {
                onItemCommitted(newValue.trim().toString())
            }
            state.rotateLockState()
        }
    )

    items.forEachIndexed { index, value ->
        var showOverflow by remember { mutableStateOf(false) }
        Box {
            val shape =
                if (index == 0) RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 4.dp
                ) else RectangleShape
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .height(IntrinsicSize.Min)
                    .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = shape)
                    .bottomBorder(MaterialTheme.colorScheme.onSurfaceVariant)
                    .animateContentSize()
            ) {
                item(index, value)
            }

            EntryItemDropdown(
                show = { showOverflow },
                onDismiss = { showOverflow = false },
                index = index,
                totalSize = { items.size },
                onDelete = { items.removeAt(index) },
                onMoveUp = { items.swap(index, index - 1) },
                onMoveDown = { items.swap(index, index + 1) },
                modifier = Modifier
                    .width(48.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp)
            )
        }
    }

    AnimatedVisibility(
        // TODO: Allow showing open field even when locked in search panel
        visible = state.lockState.editable,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        val predictions by produceState(emptyList(), state, entryPredictions) {
            // No debounce here because speed of autocomplete is critical for streamlined entry
            snapshotFlow { state.pendingValue.text.toString() }
                .flatMapLatest(entryPredictions)
                .flowOn(PlatformDispatchers.IO)
                .collectLatest { value = it }
        }
        val dropdownFocusRequester = remember { FocusRequester() }
        val showPredictions by remember {
            derivedStateOf {
                state.lockState.editable && state.pendingValue.text.isNotBlank()
            }
        }
        var dropdownExpanded by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        EntryAutocompleteDropdown(
            expanded = { dropdownExpanded },
            onExpandedChange = { dropdownExpanded = it },
            text = state.pendingValue,
            predictions = { predictions },
            showPredictions = { showPredictions },
            onPredictionChosen = {
                Snapshot.withMutableSnapshot {
                    items += it
                    state.pendingValue.clearText()
                }
            },
            item = prediction,
            fieldFocusRequester = state.focusRequester,
            focusRequester = dropdownFocusRequester,
            modifier = Modifier.onPreviewKeyEvent {
                if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                if (it.isTabKeyDownOrTyped) {
                    if (dropdownExpanded) {
                        scope.launch {
                            delay(100.milliseconds)
                            dropdownFocusRequester.requestFocus(FocusDirection.Next)
                        }
                    }
                    false
                } else {
                    when (it.key) {
                        // TODO: Re-add backspace to move up behavior on mobile
                        Key.DirectionUp -> if (dropdownExpanded) {
                            dropdownFocusRequester.requestFocus(FocusDirection.Next)
                            true
                        } else if (state.pendingValue.text.isBlank() && items.isNotEmpty()) {
                            Snapshot.withMutableSnapshot {
                                val removed = removeLastItem()
                                if (removed != null) {
                                    state.pendingValue.setTextAndPlaceCursorAtEnd(removed)
                                }
                            }
                            scope.launch {
                                delay(1.seconds)
                                state.focusRequester.requestFocus()
                            }
                            true
                        } else {
                            false
                        }
                        Key.DirectionDown -> if (dropdownExpanded) {
                            dropdownFocusRequester.requestFocus(FocusDirection.Next)
                            true
                        } else {
                            false
                        }
                        else -> false
                    }
                }
            }
        ) {
            OpenSectionField(
                state = state.pendingValue,
                lockState = { state.lockState },
                totalSize = { items.size },
                onLock = { state.lockState = EntryLockState.LOCKED },
                onDone = {
                    if (preferPrediction && predictions.isNotEmpty()) {
                        Snapshot.withMutableSnapshot {
                            items += predictions.first()
                            state.pendingValue.clearText()
                        }
                    } else {
                        val newValue = state.pendingValue.text
                        if (newValue.isNotBlank()) {
                            onItemCommitted(newValue.trim().toString())
                        }
                    }
                },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                    .onFocusChanged { state.isFocused = it.isFocused }
                    .focusRequester(state.focusRequester)
                    .onPreviewKeyEvent {
                        if (it.type != KeyEventType.KeyDown || !dropdownExpanded ||
                            it.key != Key.Escape
                        ) {
                            return@onPreviewKeyEvent false
                        }
                        dropdownExpanded = false
                        true
                    }
                    .interceptTab {
                        if (dropdownExpanded) {
                            dropdownFocusRequester.requestFocus()
                        } else {
                            onTab(it)
                        }
                    }
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
    lockState: EntryLockState?,
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
private fun SectionHeader(
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    state: EntryForm2.State,
) {
    SectionHeader(
        text = text, lockState = state.lockState,
        onClick = { state.rotateLockState() },
        modifier = modifier
    )
}

@Composable
private fun OpenSectionField(
    state: TextFieldState,
    lockState: () -> EntryLockState?,
    totalSize: () -> Int,
    onLock: () -> Unit,
    onDone: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    OpenSectionField(
        state = state,
        onNext = {
            if (totalSize() > 0) {
                onLock()
            }
            focusManager.moveFocus(FocusDirection.Next)
        },
        onDone = onDone,
        lockState = lockState,
        modifier = modifier,
    )
}

@Composable
private fun OpenSectionField(
    state: TextFieldState,
    lockState: () -> EntryLockState?,
    modifier: Modifier = Modifier,
    onNext: () -> Unit,
    onDone: () -> Unit,
) {
    val isBlank by remember { derivedStateOf { state.text.isBlank() } }
    OutlinedTextField(
        state = state,
        readOnly = lockState()?.editable == false,
        keyboardOptions = KeyboardOptions(imeAction = if (isBlank) ImeAction.Next else ImeAction.Done),
        onKeyboardAction = {
            if (isBlank) {
                onNext()
            } else {
                onDone()
            }
        },
        lineLimits = TextFieldLineLimits.SingleLine,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .onPreviewKeyEvent {
                if (it.type == KeyEventType.KeyDown) {
                    when (it.key) {
                        Key.NavigateNext -> {
                            onNext()
                            true
                        }
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

@Composable
private fun SectionField(
    index: Int,
    entry: EntryForm2.MultiTextState.Entry,
    onValueChange: (value: String) -> Unit,
    onClickMore: () -> Unit,
    onDone: () -> Unit,
    lockState: () -> EntryLockState?,
    trailingIcon: @Composable (() -> Unit)?,
    onNavigate: (() -> Unit)?,
) {
    when (entry) {
        is EntryForm2.MultiTextState.Entry.Custom -> CustomText(
            entry = entry,
            index = index,
            lockState = lockState,
            onValueChange = onValueChange,
            onDone = onDone,
            onClickMore = onClickMore,
            onNavigate = onNavigate,
        )
        is EntryForm2.MultiTextState.Entry.Prefilled<*> -> PrefilledText(
            entry = entry,
            index = index,
            lockState = lockState(),
            trailingIcon = trailingIcon,
            onClickMore = onClickMore,
            onNavigate = onNavigate,
        )
        EntryForm2.MultiTextState.Entry.Different ->
            DifferentText(lockState = lockState, index = index, onClickMore = onClickMore)
    }
}

@Composable
private fun CustomText(
    entry: EntryForm2.MultiTextState.Entry.Custom,
    index: Int,
    lockState: () -> EntryLockState?,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    onClickMore: () -> Unit,
    onNavigate: (() -> Unit)? = null,
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
                    IconButton(onClick = onNavigate) {
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
    entry: EntryForm2.MultiTextState.Entry.Prefilled<*>,
    index: Int,
    lockState: EntryLockState?,
    trailingIcon: @Composable (() -> Unit)?,
    onClickMore: () -> Unit,
    onNavigate: (() -> Unit)?,
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
            IconButton(onClick = onNavigate) {
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
    lockState: () -> EntryLockState?,
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
private fun <T> EntryAutocompleteDropdown(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    text: TextFieldState,
    predictions: () -> List<T>,
    showPredictions: () -> Boolean,
    onPredictionChosen: (T) -> Unit,
    fieldFocusRequester: FocusRequester,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    item: @Composable (index: Int, T) -> Unit,
    textField: @Composable ExposedDropdownMenuBoxScope.() -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded(),
        onExpandedChange = { onExpandedChange(!expanded()) },
        modifier = modifier.focusable(false)
            .onPreviewKeyEvent {
                if (expanded() && it.type == KeyEventType.KeyDown &&
                    (it.key == Key.DirectionDown || it.key == Key.DirectionUp)
                ) {
                    focusRequester.requestFocus(FocusDirection.Enter)
                    true
                } else {
                    false
                }
            }
    ) {
        textField()

        if (showPredictions()) {
            val predictions = predictions()
            if (predictions.isNotEmpty()) {
                LaunchedEffect(text.text, predictions) {
                    onExpandedChange(true)
                }
                val focusRequesters = remember(focusRequester, predictions.size) {
                    listOf(focusRequester) + (0 until predictions.size - 1).map { FocusRequester() }
                }
                ExposedDropdownMenu(
                    expanded = expanded(),
                    onDismissRequest = { onExpandedChange(false) },
                    modifier = Modifier.heightIn(max = 240.dp)
                        .focusable(false)
                        .onPreviewKeyEvent {
                            if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                            val char = it.utf16CodePoint.toChar()
                            if (char.isLetterOrDigit() ||
                                char.category == CharCategory.SPACE_SEPARATOR
                            ) {
                                onExpandedChange(false)
                                text.edit { insert(length, it.toString()) }
                                fieldFocusRequester.requestFocus()
                                true
                            } else if (it.key == Key.Backspace) {
                                text.edit {
                                    if (length > 0) {
                                        delete(length - 1, length)
                                    }
                                }
                                fieldFocusRequester.requestFocus()
                                true
                            } else {
                                false
                            }
                        }
                ) {
                    val focusManager = LocalFocusManager.current
                    predictions.forEachIndexed { index, entry ->
                        var focused by remember { mutableStateOf(false) }
                        DropdownMenuItem(
                            onClick = { onPredictionChosen(entry) },
                            text = { item(index, entry) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .background(
                                    if (focused) {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainer
                                    }
                                )
                                .onFocusChanged { focused = it.isFocused }
                                .focusRequester(focusRequesters[index])
                                .focusProperties {
                                    up = focusRequesters.getOrNull(index - 1)
                                        ?: fieldFocusRequester
                                    previous = focusRequesters.getOrNull(index - 1)
                                        ?: fieldFocusRequester
                                    down = focusRequesters.getOrNull(index + 1)
                                        ?: fieldFocusRequester
                                    next = focusRequesters.getOrNull(index + 1)
                                        ?: fieldFocusRequester
                                }
                                .fillMaxWidth()
                                .onPreviewKeyEvent { event ->
                                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                                    when (event.key) {
                                        Key.Escape -> {
                                            onExpandedChange(false)
                                            true
                                        }
                                        Key.DirectionDown -> {
                                            focusManager.moveFocus(FocusDirection.Next)
                                            true
                                        }
                                        Key.DirectionUp -> {
                                            focusManager.moveFocus(FocusDirection.Previous)
                                            true
                                        }
                                        Key.Enter -> {
                                            onPredictionChosen(entry)
                                            true
                                        }
                                        else -> false
                                    }
                                }
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
    state: EntryForm2.PendingTextState,
    headerText: @Composable () -> Unit,
) {
    SectionHeader(text = headerText, state = state)

    val editable = state.lockState.editable
    OutlinedTextField(
        state = state.pendingValue,
        readOnly = !editable,
        modifier = Modifier
            .onFocusChanged { state.isFocused = it.isFocused }
            .focusRequester(state.focusRequester)
            .focusable(editable)
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}


@Suppress("UnusedReceiverParameter")
@Composable
fun <T> EntryFormScope.DropdownSection(
    state: EntryForm2.DropdownState,
    headerText: @Composable () -> Unit,
    options: List<T>,
    optionToText: @Composable (T) -> String,
    leadingIcon: (@Composable (T) -> Unit)? = null,
    expandedItemText: @Composable (T) -> Unit = { Text(optionToText(it)) },
    errorText: (() -> String?)? = null,
    previousFocus: FocusRequester? = null,
    nextFocus: FocusRequester? = null,
) {
    SectionHeader(text = headerText, state = state)

    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        val editable = state.lockState.editable
        ExposedDropdownMenuBox(
            expanded = state.expanded && editable,
            onExpandedChange = {
                if (editable) {
                    state.expanded = it
                }
            },
            modifier = Modifier.fillMaxWidth()
                .focusRequester(state.focusRequester)
                .conditionallyNonNull(previousFocus) { requester ->
                    onPreviewKeyEvent {
                        if (it.isTabKeyDownOrTyped && it.isShiftPressed) {
                            requester.requestFocus(FocusDirection.Previous)
                            true
                        } else {
                            false
                        }
                    }
                }
                .conditionallyNonNull(nextFocus) { requester ->
                    onPreviewKeyEvent {
                        if (it.isTabKeyDownOrTyped) {
                            requester.requestFocus(FocusDirection.Next)
                            true
                        } else {
                            false
                        }
                    }
                }
                .onPreviewKeyEvent { it.isTabKey }
                .onPreviewKeyEvent {
                    if (it.type != KeyEventType.KeyDown || it.key != Key.DirectionDown) {
                        return@onPreviewKeyEvent false
                    }
                    if (editable) {
                        state.expanded = true
                        true
                    } else {
                        false
                    }
                }
        ) {
            val errorText = errorText?.invoke()
            val option = options.getOrNull(state.selectedIndex)
            TextField(
                readOnly = true,
                value = option?.let { optionToText(it) }.orEmpty(),
                onValueChange = {},
                leadingIcon = leadingIcon?.let { option?.let { { leadingIcon(it) } } },
                trailingIcon = {
                    @Suppress("RemoveRedundantQualifierName")
                    androidx.compose.animation.AnimatedVisibility(
                        visible = editable,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        ExposedDropdownMenuDefaults.TrailingIcon(state.expanded)
                    }
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                supportingText = if (errorText == null) {
                    null
                } else {
                    { Text(errorText) }
                },
                isError = errorText != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
            )
            val focusRequesters = remember(options) {
                (0 until options.size).map { FocusRequester() }
            }
            ExposedDropdownMenu(
                expanded = state.expanded,
                onDismissRequest = { state.expanded = false },
            ) {
                options.forEachIndexed { index, item ->
                    val focusRequester = focusRequesters[index]
                    LaunchedEffect(index, focusRequester) {
                        if (index == 0) {
                            focusRequester.requestFocus()
                        }
                    }
                    var focused by remember { mutableStateOf(false) }
                    DropdownMenuItem(
                        onClick = {
                            state.expanded = false
                            state.selectedIndex = index
                        },
                        leadingIcon = leadingIcon?.let { { leadingIcon(item) } },
                        text = { expandedItemText(item) },
                        modifier = Modifier
                            .background(
                                if (focused) {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                }
                            )
                            .onFocusChanged { focused = it.isFocused }
                            .focusRequester(focusRequester)
                            .onKeyEvent { event ->
                                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                                when (event.key) {
                                    Key.DirectionDown -> {
                                        focusRequesters.getOrNull(index + 1)?.requestFocus()
                                        true
                                    }
                                    Key.DirectionUp -> {
                                        focusRequesters.getOrNull(index - 1)?.requestFocus()
                                        true
                                    }
                                    else -> false
                                }
                            }
                    )
                }
            }
        }
    }
}

fun Modifier.interceptTab(onTab: (next: Boolean) -> Unit) = onPreviewKeyEvent {
    if (it.isTabKeyDownOrTyped) {
        onTab(!it.isShiftPressed)
        true
    } else if (it.isTabKey) {
        // Intercept all tabs to avoid default focus next/previous interaction
        true
    } else {
        false
    }
}
