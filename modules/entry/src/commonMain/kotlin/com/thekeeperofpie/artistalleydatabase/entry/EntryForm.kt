@file:Suppress("NAME_SHADOWING")
@file:OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class, ExperimentalComposeUiApi::class
)

package com.thekeeperofpie.artistalleydatabase.entry

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.entry.generated.resources.Res
import artistalleydatabase.modules.entry.generated.resources.delete
import artistalleydatabase.modules.entry.generated.resources.different
import artistalleydatabase.modules.entry.generated.resources.entry_add_image_content_description
import artistalleydatabase.modules.entry.generated.resources.entry_image_content_description
import artistalleydatabase.modules.entry.generated.resources.entry_image_menu_option_crop
import artistalleydatabase.modules.entry.generated.resources.entry_image_menu_option_edit
import artistalleydatabase.modules.entry.generated.resources.entry_image_menu_option_open
import artistalleydatabase.modules.entry.generated.resources.entry_image_menu_option_share
import artistalleydatabase.modules.entry.generated.resources.entry_open_more_content_description
import artistalleydatabase.modules.entry.generated.resources.label_open_entry_link
import artistalleydatabase.modules.entry.generated.resources.lock_state_different_content_description
import artistalleydatabase.modules.entry.generated.resources.lock_state_locked_content_description
import artistalleydatabase.modules.entry.generated.resources.lock_state_unlocked_content_description
import artistalleydatabase.modules.entry.generated.resources.move_down
import artistalleydatabase.modules.entry.generated.resources.move_up
import artistalleydatabase.modules.utils_compose.generated.resources.more_actions_content_description
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.image.rememberImageSelectController
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIcon
import com.thekeeperofpie.artistalleydatabase.utils_compose.ZoomPanState
import com.thekeeperofpie.artistalleydatabase.utils_compose.bottomBorder
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.optionalClickable
import com.thekeeperofpie.artistalleydatabase.utils_compose.rememberZoomPanState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.utils_compose.generated.resources.Res as UtilsRes

@Composable
fun EntryForm(
    modifier: Modifier = Modifier,
    areSectionsLoading: () -> Boolean = { false },
    sections: () -> List<EntrySection> = { emptyList() },
    onNavigate: (String) -> Unit,
    onAnySectionFocused: () -> Unit,
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2000.dp)
                ) {
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
                                    onFocusChanged = { if (it) onAnySectionFocused() },
                                    onFocusPrevious = { onFocusPrevious(index) },
                                    onFocusNext = { onFocusNext(index) },
                                )
                            }
                            is EntrySection.LongText -> LongTextSection(
                                section = section,
                                focusRequester = focusRequester,
                                onFocusChanged = { if (it) onAnySectionFocused() },
                            )
                            is EntrySection.Dropdown -> DropdownSection(
                                section = section,
                                focusRequester = focusRequester,
                                onFocusChanged = { if (it) onAnySectionFocused() },
                            )
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
                    EntrySection.LockState.LOCKED -> Res.string.lock_state_locked_content_description
                    EntrySection.LockState.UNLOCKED -> Res.string.lock_state_unlocked_content_description
                    EntrySection.LockState.DIFFERENT -> Res.string.lock_state_different_content_description
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
    onFocusChanged: (Boolean) -> Unit,
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
                        text = { Text(stringResource(Res.string.delete)) },
                        onClick = {
                            section.removeContentAt(index)
                            showOverflow = false
                        }
                    )
                    if (index > 0) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.move_up)) },
                            onClick = {
                                section.swapContent(index, index - 1)
                                showOverflow = false
                            }
                        )
                    }
                    if (index < section.contentSize() - 1) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.move_down)) },
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
                onFocusChanged = {
                    focused = it
                    onFocusChanged(it)
                },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
            )
        }
    }
}

@Composable
private fun LongTextSection(
    section: EntrySection.LongText,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
) {
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
            .onFocusChanged { onFocusChanged(it.isFocused) }
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
                                        UtilsRes.string.more_actions_content_description
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
                                UtilsRes.string.more_actions_content_description
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
                                    UtilsRes.string.more_actions_content_description
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
                value = stringResource(Res.string.different),
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
                                    UtilsRes.string.more_actions_content_description
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
                onClickLabel = stringResource(Res.string.label_open_entry_link),
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
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(image)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(Res.string.entry_image_content_description),
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
    // TODO: There must be a better way to intercept previous value
    var previousValue by remember { mutableStateOf(section.pendingValue) }
    OpenSectionField(
        value = { section.pendingValue },
        onValueChange = {
            previousValue = section.pendingValue
            section.pendingValue = it
        },
        onNext = {
            if (section.contentSize() > 0) {
                section.rotateLockState()
            }
            onFocusNext()
        },
        onBackspace = {
            if (section.contentSize() > 0) {
                if (previousValue.text.isEmpty() && section.pendingValue.text.isEmpty()) {
                    val removed = section.removeContentAt(section.contentSize() - 1)
                    section.pendingValue = TextFieldValue(
                        text = removed.text,
                        selection = TextRange(removed.text.length),
                    )
                } else {
                    // Set the next value to ensure empty eventually propagates to previous
                    previousValue = section.pendingValue
                }
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
private fun DropdownSection(
    section: EntrySection.Dropdown,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
) {
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
                    .onFocusChanged { onFocusChanged(it.isFocused) }
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

@Composable
fun MultiImageSelectBox(
    pagerState: PagerState,
    imageState: () -> EntryImageState,
    onClickOpenImage: (index: Int) -> Unit,
    onClickCropImage: (index: Int) -> Unit,
    onClickShareImage: ((index: Int) -> Unit)?,
    imageContent: @Composable (image: EntryImage, zoomPanState: ZoomPanState) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    val zoomPanStates = remember { mutableMapOf<Int, ZoomPanState>() }
    val userScrollEnabled by remember {
        derivedStateOf {
            zoomPanStates[pagerState.currentPage]?.canPanExternal() != false
        }
    }

    val imageSelectController = rememberImageSelectController(
        onSelection = imageState().onSelected,
        onAddition = imageState().onAdded,
    )

    val images = imageState().images()
    HorizontalPager(
        state = pagerState,
        userScrollEnabled = userScrollEnabled,
        modifier = Modifier.heightIn(max = 10000.dp)
    ) { index ->
        if (index == images.size) {
            AddImagePagerPage(onAddClick = imageSelectController::requestNewImages)
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
                                onLongPress = { onClickCropImage(index) },
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
                    text = { Text(stringResource(Res.string.entry_image_menu_option_open)) },
                    onClick = {
                        showMenu = false
                        onClickOpenImage(pagerState.currentPage)
                    },
                )

                HorizontalDivider()

                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.entry_image_menu_option_edit)) },
                    onClick = {
                        showMenu = false
                        try {
                            imageSelectController.requestNewImage(pagerState.currentPage)
                        } catch (e: Exception) {
                            imageState().onSelectError(e)
                        }
                    },
                )

                HorizontalDivider()

                if (onClickShareImage != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.entry_image_menu_option_share)) },
                        onClick = {
                            showMenu = false
                            onClickShareImage(pagerState.currentPage)
                        },
                    )
                }

                HorizontalDivider()

                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.entry_image_menu_option_crop)) },
                    onClick = {
                        showMenu = false
                        onClickCropImage(pagerState.currentPage)
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
                Res.string.entry_add_image_content_description
            ),
            Modifier
                .size(48.dp)
                .align(Alignment.Center)
        )
    }
}

// TODO: Remove
data class EntryImageState(
    val images: () -> List<EntryImage> = { emptyList() },
    val onSelected: (index: Int, Uri?) -> Unit = { _, _ -> },
    val onSelectError: (Exception?) -> Unit,
    val addAllowed: () -> Boolean = { false },
    val onAdded: (List<Uri>) -> Unit,
    val onSizeResult: (width: Int, height: Int) -> Unit = { _, _ -> },
)
