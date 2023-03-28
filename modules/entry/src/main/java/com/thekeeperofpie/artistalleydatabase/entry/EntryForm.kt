package com.thekeeperofpie.artistalleydatabase.entry

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageSearch
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.thekeeperofpie.artistalleydatabase.compose.AddBackPressInvokeTogether
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIcon
import com.thekeeperofpie.artistalleydatabase.compose.bottomBorder
import com.thekeeperofpie.artistalleydatabase.compose.dropdown.DropdownMenu
import com.thekeeperofpie.artistalleydatabase.compose.dropdown.DropdownMenuItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun EntryForm(
    areSectionsLoading: () -> Boolean = { false },
    sections: () -> List<EntrySection> = { emptyList() },
) {
    AnimatedContent(
        targetState = areSectionsLoading(),
        transitionSpec = {
            fadeIn(animationSpec = tween(durationMillis = 200, delayMillis = 150)) with
                    fadeOut(animationSpec = tween(100))
        },
        label = "Entry form section fade in",
    ) {
        if (it) {
            AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                Box(Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        } else {
            Column {
                sections().forEach {
                    when (it) {
                        is EntrySection.MultiText -> MultiTextSection(it)
                        is EntrySection.LongText -> LongTextSection(it)
                        is EntrySection.Dropdown -> DropdownSection(it)
                        is EntrySection.Custom<*> -> CustomSection(it)
                    }
                }

                Spacer(Modifier.height(24.dp))
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
                val listState = rememberLazyListState()
                DropdownMenu(
                    expanded = focused,
                    onDismissRequest = { focusRequester.freeFocus() },
                    properties = PopupProperties(focusable = false),
                    listState = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    items(section.predictions.size, key = { section.predictions[it].id }) {
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
                                var subtitleText: () -> String? = { null }
                                var image: () -> String? = { null }
                                var imageLink: () -> String? = { null }
                                var secondaryImage: (() -> String?)? = null
                                var secondaryImageLink: () -> String? = { null }
                                when (entry) {
                                    is EntrySection.MultiText.Entry.Custom -> {
                                        titleText = { entry.text }
                                    }
                                    is EntrySection.MultiText.Entry.Prefilled<*> -> {
                                        titleText = { entry.titleText }
                                        subtitleText = { entry.subtitleText }
                                        image = { entry.image }
                                        imageLink = { entry.imageLink }
                                        secondaryImage = entry.secondaryImage?.let { { it } }
                                        secondaryImageLink = { entry.secondaryImageLink }
                                    }
                                    EntrySection.MultiText.Entry.Different -> {
                                        titleText = { stringResource(R.string.different) }
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

                                    if (secondaryImage != null) {
                                        EntryImage(
                                            image = secondaryImage,
                                            link = secondaryImageLink,
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .heightIn(min = 54.dp)
                                                .width(42.dp)
                                        )
                                    }
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                // Scroll to the top each time the predictions change to show highest priority
                LaunchedEffect(section.predictions) {
                    listState.scrollToItem(0)
                }
            }
        }
    }
}

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

@OptIn(ExperimentalComposeUiApi::class)
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

                val secondaryImage = entry.secondaryImage
                if (secondaryImage != null) {
                    EntryImage(
                        image = { secondaryImage },
                        link = { entry.secondaryImageLink },
                        modifier = Modifier
                            .fillMaxHeight()
                            .heightIn(min = 72.dp)
                            .width(56.dp)
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

@OptIn(ExperimentalComposeUiApi::class)
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
                if (section.lockState?.editable != false) {
                    section.expanded = !section.expanded
                }
            },
            Modifier.fillMaxWidth()
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
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
                            contentDescription = section.arrowContentDescription,
                        )
                    }
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
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
    pagerState: PagerState = rememberPagerState(),
    imageState: () -> ImageState,
    cropState: () -> CropUtils.CropState,
    loading: () -> Boolean,
    imageContent: @Composable (image: EntryImage) -> Unit,
) {
    val imageSelectMultipleLauncher = rememberLauncherForActivityResult(
        GetMultipleContentsChooser,
        imageState().onMultipleSelected,
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

    // If the image ratio changes, reset maxHeight so it shrinks/grows properly
    val imageRatio = imageState().images().firstOrNull()?.widthToHeightRatio ?: 1f
    val screenWidthPx = LocalDensity.current.run {
        LocalConfiguration.current.screenWidthDp.dp.roundToPx()
    }
    val startingHeight = (screenWidthPx * imageRatio).roundToInt()
    val targetHeight = LocalDensity.current.run { 400.dp.roundToPx() }.coerceAtMost(startingHeight)
    val heightAnimation = remember { Animatable(1f) }

    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(expanded, loading()) {
        // This call will also implicitly kick the initial slide up
        // due to maxHeight causing a recompose and running this effect
        heightAnimation.animateTo(
            if (expanded || loading()) 1f else 0f,
            animationSpec = tween(EntryUtils.SLIDE_DURATION_MS),
        )
    }

    val animatedHeight = LocalDensity.current.run {
        ((startingHeight - targetHeight) * heightAnimation.value + targetHeight).toDp()
    }

    ImageSelectBoxInner(
        loading = loading,
        expanded = { expanded },
        setExpanded = { expanded = it },
        showExpandImage = startingHeight > (targetHeight + 10f),
        height = animatedHeight,
        onBackPress = { heightAnimation.animateTo(1f, tween(1000)) }
    ) {
        val images = imageState().images()
        val addAllowed = imageState().addAllowed()
        val size = if (addAllowed) images.size + 1 else images.size
        HorizontalPager(
            pageCount = size,
            state = pagerState,
            modifier = Modifier.heightIn(max = 10000.dp)
        ) { index ->
            if (index == images.size) {
                AddImagePagerPage(
                    onAddClick = { imageSelectMultipleLauncher.launch("image/*") },
                    modifier = Modifier
                        .height(animatedHeight)
                )
            } else {
                val image = images[index]
                val uri = image.croppedUri ?: image.uri
                if (uri == null) {
                    // TODO: Null image placeholder
                } else {
                    Box(
                        Modifier
                            .wrapContentHeight()
                            .verticalScroll(rememberScrollState())
                            .combinedClickable(onClick = {
                                try {
                                    imageSelectSingleLauncher.launch(index)
                                } catch (e: Exception) {
                                    imageState().onSelectError(e)
                                }
                            }, onLongClick = {
                                @Suppress("NAME_SHADOWING")
                                val cropState = cropState()
                                if (cropState.cropReadyIndex() == index) {
                                    imageCropLauncher.launch(index)
                                } else {
                                    cropState.onImageRequestCrop(index)
                                }
                            })
                    ) {
                        imageContent(image)
                    }
                }
            }
        }

        // TODO: Pager indicator? Might need to migrate back to Accompanist
    }
}

@Composable
private fun AddImagePagerPage(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clickable(onClick = onAddClick)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.ImageSearch,
            contentDescription = stringResource(
                R.string.entry_add_image_content_description
            ),
            Modifier
                .size(48.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun ImageSelectBoxInner(
    loading: () -> Boolean = { false },
    expanded: () -> Boolean,
    setExpanded: (Boolean) -> Unit,
    showExpandImage: Boolean,
    height: Dp,
    onBackPress: suspend () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    AddBackPressInvokeTogether(label = "ImageSelectBoxInner onBackPress") { onBackPress() }

    Box {
        Box(
            Modifier
                .height(height)
                .verticalScroll(rememberScrollState())
                .animateContentSize()
        ) {
            content()
        }

        AnimatedVisibility(
            visible = showExpandImage && !loading(),
            enter = fadeIn(animationSpec = tween(durationMillis = 200, delayMillis = 150)),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            TrailingDropdownIcon(
                expanded = expanded(),
                contentDescription = R.string.entry_image_expand_content_description,
                onClick = { setExpanded(!expanded()) },
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

data class ImageState(
    val images: () -> List<EntryImage> = { emptyList() },
    val onSelected: (index: Int, Uri?) -> Unit = { _, _ -> },
    val onSelectError: (Exception?) -> Unit,
    val addAllowed: () -> Boolean = { false },
    val onMultipleSelected: (List<Uri>) -> Unit,
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