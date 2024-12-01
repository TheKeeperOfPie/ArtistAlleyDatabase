package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_cover_image_long_press_preview
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_chapters_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_clear_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_confirm_close_text
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_confirm_close_title
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_confirm_delete_exit_button
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_confirm_delete_save_button
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_confirm_delete_title
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_created_at
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_date_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_decrement_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_episodes_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_hidden_from_status_lists_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_increment_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_notes_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_priority_decrement_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_priority_increment_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_priority_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_private_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_repeat_decrement_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_repeat_increment_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_repeat_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_score_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_score_point_3_dissatisfied
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_score_point_3_neutral
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_score_point_3_satisfied
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_score_point_5_start_1
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_score_point_5_start_2
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_score_point_5_start_3
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_score_point_5_start_4
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_score_point_5_start_5
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_status_dropdown_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_status_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_updated_at
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_volumes_label
import artistalleydatabase.modules.utils_compose.generated.resources.delete
import artistalleydatabase.modules.utils_compose.generated.resources.no
import artistalleydatabase.modules.utils_compose.generated.resources.save
import artistalleydatabase.modules.utils_compose.generated.resources.yes
import coil3.annotation.ExperimentalCoilApi
import com.anilist.data.fragment.MediaDetailsListEntry
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.anilist.data.type.ScoreFormat
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toStatusIcon
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.ui.MediaCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateDialog
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateRow
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.ItemDropdown
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class, ExperimentalCoilApi::class)
@Suppress("NAME_SHADOWING")
object AnimeMediaEditBottomSheet {

    private val DEFAULT_IMAGE_HEIGHT = 100.dp
    private val DEFAULT_IMAGE_WIDTH = 72.dp

    @Composable
    operator fun invoke(
        state: () -> MediaEditState,
        eventSink: (Event) -> Unit,
        onConfirmExit: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        var showDelete by remember { mutableStateOf(false) }

        HorizontalDivider()

        val state = state()
        Column(modifier = modifier) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f, fill = false)
            ) {
                Crossfade(
                    targetState = state.initialParams?.loading,
                    label = "Media edit sheet crossfade"
                ) {
                    if (it == true) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(32.dp)
                            )
                        }
                    } else {
                        Column {
                            SharedTransitionKeyScope("media_edit_bottom_sheet") {
                                Form(
                                    state = state,
                                    eventSink = eventSink,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            HorizontalDivider()

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.initialParams?.id != null) {
                    TextButton(onClick = { showDelete = true }) {
                        Crossfade(
                            targetState = state.deleting,
                            label = "Media edit delete indicator crossfade"
                        ) {
                            if (it) {
                                CircularProgressIndicator()
                            } else {
                                Text(
                                    text = stringResource(UtilsStrings.delete),
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 10.dp
                                    )
                                )
                            }
                        }
                    }
                }

                TextButton(onClick = { eventSink(Event.Save) }) {
                    Crossfade(
                        targetState = state.saving,
                        label = "Media edit save indicator crossfade"
                    ) {
                        if (it) {
                            CircularProgressIndicator()
                        } else {
                            Text(
                                text = stringResource(UtilsStrings.save),
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 10.dp
                                )
                            )
                        }
                    }
                }
            }
        }

        val scope = rememberCoroutineScope()

        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                title = { Text(stringResource(Res.string.anime_media_edit_confirm_delete_title)) },
                confirmButton = {
                    TextButton(onClick = {
                        showDelete = false
                        eventSink(Event.Delete)
                    }) {
                        Text(stringResource(UtilsStrings.yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDelete = false }) {
                        Text(stringResource(UtilsStrings.no))
                    }
                },
            )
        } else if (state.showConfirmClose) {
            AlertDialog(
                onDismissRequest = { state.showConfirmClose = false },
                title = { Text(stringResource(Res.string.anime_media_edit_confirm_close_title)) },
                text = { Text(stringResource(Res.string.anime_media_edit_confirm_close_text)) },
                confirmButton = {
                    TextButton(onClick = {
                        state.showConfirmClose = false
                        eventSink(Event.Save)
                    }) {
                        Text(stringResource(Res.string.anime_media_edit_confirm_delete_save_button))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        state.hasConfirmedClose = true
                        state.showConfirmClose = false
                        onConfirmExit()
                    }) {
                        Text(stringResource(Res.string.anime_media_edit_confirm_delete_exit_button))
                    }
                },
            )
        }
    }

    @Composable
    private fun ColumnScope.Form(
        state: MediaEditState,
        eventSink: (Event) -> Unit,
    ) {
        var startEndDateShown by remember { mutableStateOf<Boolean?>(null) }
        val initialParams = state.initialParams
        val mediaId = initialParams?.mediaId
        if (initialParams != null && mediaId != null) {
            val coverImage = initialParams.coverImage
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                val navigationCallback = LocalNavigationCallback.current
                val coverImageState = rememberCoilImageState(coverImage)
                val fullscreenImageHandler = LocalFullscreenImageHandler.current
                val sharedTransitionKey = SharedTransitionKey.makeKeyForId(mediaId)
                MediaCoverImage(
                    imageState = coverImageState,
                    image = coverImageState.request().build(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = DEFAULT_IMAGE_WIDTH, height = DEFAULT_IMAGE_HEIGHT)
//                        .sharedElement(sharedTransitionKey, "media_image")
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .combinedClickable(
                            onClick = {
                                navigationCallback.navigate(
                                    AnimeDestination.MediaDetails(
                                        mediaId = mediaId,
                                        title = initialParams.title,
                                        coverImage = coverImageState.toImageState(),
                                        sharedTransitionKey = sharedTransitionKey,
                                        headerParams = MediaHeaderParams(
                                            coverImage = coverImageState.toImageState(),
                                            title = initialParams.title,
                                            media = null,
                                        ),
                                    )
                                )
                            },
                            onLongClick = { coverImage?.let(fullscreenImageHandler::openImage) },
                            onLongClickLabel = stringResource(
                                Res.string.anime_media_cover_image_long_press_preview
                            ),
                        )
                )

                AutoSizeText(
                    text = initialParams.title.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .weight(1f)
                )
            }

            HorizontalDivider()
        }

        val isAnime = initialParams?.mediaType == MediaType.ANIME
        SectionHeader(Res.string.anime_media_edit_status_label)
        ItemDropdown(
            value = state.status,
            iconContentDescription = Res.string.anime_media_edit_status_dropdown_content_description,
            values = {
                listOf(
                    null,
                    MediaListStatus.CURRENT,
                    MediaListStatus.PLANNING,
                    MediaListStatus.COMPLETED,
                    MediaListStatus.DROPPED,
                    MediaListStatus.PAUSED,
                    MediaListStatus.REPEATING,
                )
            },
            iconForValue = {
                val (imageVector, contentDescriptionRes) =
                    it.toStatusIcon(mediaType = initialParams?.mediaType)
                Icon(
                    imageVector = imageVector,
                    contentDescription = stringResource(contentDescriptionRes),
                )
            },
            textForValue = { stringResource(it.toTextRes(isAnime)) },
            onSelectItem = {
                state.status = it
                eventSink(Event.StatusChange(it))
            },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        if (initialParams?.mediaType == MediaType.MANGA) {
            TwoColumn(
                labelOneRes = Res.string.anime_media_edit_volumes_label,
                columnOneContent = {
                    ProgressSection(
                        progress = state.progressVolumes,
                        progressMax = initialParams.maxProgressVolumes,
                        onProgressChange = { state.progressVolumes = it },
                    )
                },
                labelTwoRes = Res.string.anime_media_edit_chapters_label,
                columnTwoContent = {
                    ProgressSection(
                        progress = state.progress,
                        progressMax = initialParams.maxProgress,
                        onProgressChange = { state.progress = it },
                    )
                },
            )
        } else {
            TwoColumn(
                labelOneRes = Res.string.anime_media_edit_episodes_label,
                columnOneContent = {
                    ProgressSection(
                        progress = state.progress,
                        progressMax = initialParams?.maxProgress,
                        onProgressChange = { state.progress = it },
                    )
                },
            )
        }

        ScoreSection(
            format = { state.scoreFormat },
            score = { state.score },
            onScoreChange = { state.score = it },
        )

        SectionHeader(Res.string.anime_media_edit_date_label)
        StartEndDateRow(
            startDate = state.startDate,
            endDate = state.endDate,
            onRequestDatePicker = { startEndDateShown = it },
            onDateChange = { start, selectedMillisUtc ->
                eventSink(
                    Event.DateChange(
                        start,
                        selectedMillisUtc
                    )
                )
            },
        )

        if (startEndDateShown != null) {
            StartEndDateDialog(
                shownForStartDate = startEndDateShown,
                onShownForStartDateChange = { startEndDateShown = it },
                onDateChange = { start, selectedMillisUtc ->
                    eventSink(
                        Event.DateChange(
                            start,
                            selectedMillisUtc
                        )
                    )
                },
            )
        }

        TwoColumn(
            labelOneRes = Res.string.anime_media_edit_priority_label,
            columnOneContent = {
                val priority = state.priority
                TextField(
                    value = priority,
                    onValueChange = { state.priority = it },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        autoCorrectEnabled = KeyboardOptions.Default.autoCorrectEnabled,
                    ),
                    leadingIcon = {
                        val visible = priority.isNotBlank()
                                && priority.toIntOrNull()?.let { it > 0 } == true
                        val alpha by animateFloatAsState(
                            if (visible) 1f else 0.38f,
                            label = "Priority decrement alpha",
                        )
                        IconButton(
                            enabled = visible,
                            onClick = {
                                priority.toIntOrNull()?.let {
                                    state.priority = (it - 1).toString()
                                }
                            },
                            modifier = Modifier.alpha(alpha),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RemoveCircleOutline,
                                contentDescription = stringResource(
                                    Res.string.anime_media_edit_priority_decrement_content_description
                                ),
                            )
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (priority.isBlank()) {
                                    state.priority = "1"
                                } else {
                                    priority.toIntOrNull()?.let {
                                        state.priority = (it + 1).toString()
                                    }
                                }
                            }) {
                            Icon(
                                imageVector = Icons.Filled.AddCircleOutline,
                                contentDescription = stringResource(
                                    Res.string.anime_media_edit_priority_increment_content_description
                                ),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            labelTwoRes = Res.string.anime_media_edit_repeat_label,
            columnTwoContent = {
                val repeat = state.repeat
                TextField(
                    value = repeat,
                    onValueChange = { state.repeat = it },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        autoCorrectEnabled = KeyboardOptions.Default.autoCorrectEnabled,
                    ),
                    leadingIcon = {
                        val visible = repeat.isNotBlank()
                                && repeat.toIntOrNull()?.let { it > 0 } == true
                        val alpha by animateFloatAsState(
                            if (visible) 1f else 0.38f,
                            label = "Repeat decrement alpha",
                        )
                        IconButton(
                            enabled = visible,
                            onClick = {
                                repeat.toIntOrNull()?.let {
                                    state.repeat = (it - 1).toString()
                                }
                            },
                            modifier = Modifier.alpha(alpha),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RemoveCircleOutline,
                                contentDescription = stringResource(
                                    Res.string.anime_media_edit_repeat_decrement_content_description
                                ),
                            )
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (repeat.isBlank()) {
                                    state.repeat = "1"
                                } else {
                                    repeat.toIntOrNull()?.let {
                                        state.repeat = (it + 1).toString()
                                    }
                                }
                            }) {
                            Icon(
                                imageVector = Icons.Filled.AddCircleOutline,
                                contentDescription = stringResource(
                                    Res.string.anime_media_edit_repeat_increment_content_description
                                ),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        )


        SectionHeader(Res.string.anime_media_edit_notes_label)
        TextField(
            value = state.notes,
            onValueChange = { state.notes = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                val dateTimeFormatter = LocalDateTimeFormatter.current
                val createdAt = state.createdAt
                val createdAtShown = createdAt != null && createdAt > 0
                if (createdAtShown) {
                    Text(
                        text = stringResource(
                            Res.string.anime_media_edit_created_at,
                            dateTimeFormatter.formatEntryDateTime(createdAt!! * 1000),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                val updatedAt = state.updatedAt
                if (updatedAt != null && updatedAt > 0) {
                    Text(
                        text = stringResource(
                            Res.string.anime_media_edit_updated_at,
                            dateTimeFormatter.formatEntryDateTime(updatedAt * 1000),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = if (createdAtShown) 0.dp else 8.dp,
                            bottom = 8.dp
                        ),
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                val private = state.private
                Text(
                    text = stringResource(Res.string.anime_media_edit_private_label),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Checkbox(
                    checked = private,
                    onCheckedChange = { state.private = it },
                )
            }
        }


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.End)
        ) {
            val private = state.hiddenFromStatusLists
            Text(
                text = stringResource(Res.string.anime_media_edit_hidden_from_status_lists_label),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Checkbox(
                checked = private,
                onCheckedChange = { state.hiddenFromStatusLists = it },
            )
        }
    }

    @Composable
    private fun ProgressSection(
        progress: String,
        progressMax: Int?,
        onProgressChange: (String) -> Unit,
    ) {
        val placeholder: (@Composable () -> Unit)? = if (progressMax != null) {
            {
                Text(
                    text = progressMax.toString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else null

        TextField(
            value = progress,
            onValueChange = onProgressChange,
            singleLine = true,
            placeholder = placeholder,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                autoCorrectEnabled = KeyboardOptions.Default.autoCorrectEnabled,
            ),
            leadingIcon = {
                val visible = progress.isNotBlank()
                        && progress.toIntOrNull()?.let { it > 0 } == true
                val alpha by animateFloatAsState(
                    if (visible) 1f else 0.38f,
                    label = "Progress decrement alpha",
                )
                IconButton(
                    enabled = visible,
                    onClick = {
                        progress.toIntOrNull()?.let {
                            if (it == 1) {
                                onProgressChange("")
                            } else {
                                onProgressChange((it - 1).toString())
                            }
                        }
                    },
                    modifier = Modifier.alpha(alpha)
                ) {
                    Icon(
                        imageVector = Icons.Filled.RemoveCircleOutline,
                        contentDescription = stringResource(
                            Res.string.anime_media_edit_decrement_content_description
                        ),
                    )
                }
            },
            trailingIcon = {
                val progressAsInt = progress.toIntOrNull()
                val visible = if (progressAsInt == null) {
                    progress.isBlank()
                } else {
                    progressMax == null || progressAsInt < progressMax
                }
                val alpha by animateFloatAsState(
                    if (visible) 1f else 0.38f,
                    label = "Progress increment alpha",
                )
                IconButton(
                    enabled = visible,
                    onClick = {
                        if (progress.isBlank()) {
                            onProgressChange("1")
                        } else {
                            progress.toIntOrNull()?.let {
                                onProgressChange((it + 1).toString())
                            }
                        }
                    },
                    modifier = Modifier.alpha(alpha)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddCircleOutline,
                        contentDescription = stringResource(
                            Res.string.anime_media_edit_increment_content_description
                        ),
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }

    @Composable
    private fun TwoColumn(
        labelOneRes: StringResource,
        columnOneContent: @Composable () -> Unit,
        labelTwoRes: StringResource? = null,
        columnTwoContent: (@Composable () -> Unit)? = null,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(horizontal = 16.dp)
        ) {
            SectionHeader(labelOneRes, horizontalPadding = 0.dp, modifier = Modifier.weight(1f))

            if (labelTwoRes != null) {
                SectionHeader(labelTwoRes, horizontalPadding = 0.dp, modifier = Modifier.weight(1f))
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(horizontal = 16.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                columnOneContent()
            }

            Box(modifier = Modifier.weight(1f)) {
                if (columnTwoContent != null) {
                    columnTwoContent()
                }
            }
        }
    }

    @Composable
    private fun ColumnScope.ScoreSection(
        format: () -> ScoreFormat,
        score: () -> String,
        onScoreChange: (String) -> Unit,
    ) {
        SectionHeader(Res.string.anime_media_edit_score_label)

        val score = score()
        val scoreAsInt = score.toIntOrNull() ?: 0
        when (val format = format()) {
            ScoreFormat.POINT_3 -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .wrapContentWidth()
                        .align(Alignment.CenterHorizontally),
                ) {
                    val dissatisfied = scoreAsInt in (1..35)
                    IconButton(onClick = {
                        onScoreChange(if (dissatisfied) "" else "35")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.SentimentVeryDissatisfied,
                            contentDescription = stringResource(
                                Res.string.anime_media_edit_score_point_3_dissatisfied
                            ),
                            tint = if (scoreAsInt in (1..35)) Color.Red else LocalContentColor.current
                        )
                    }

                    val neutral = scoreAsInt in (36..60)
                    IconButton(onClick = {
                        onScoreChange(if (neutral) "" else "60")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.SentimentNeutral,
                            contentDescription = stringResource(
                                Res.string.anime_media_edit_score_point_3_neutral
                            ),
                            tint = if (neutral) Color.Yellow else LocalContentColor.current
                        )
                    }

                    val satisfied = scoreAsInt in (61..100)
                    IconButton(onClick = {
                        onScoreChange(if (satisfied) "" else "85")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.SentimentVerySatisfied,
                            contentDescription = stringResource(
                                Res.string.anime_media_edit_score_point_3_satisfied
                            ),
                            tint = if (satisfied) Color.Green else LocalContentColor.current
                        )
                    }
                }
            }
            ScoreFormat.POINT_5 -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .wrapContentWidth()
                        .align(Alignment.CenterHorizontally),
                ) {
                    listOf(
                        20 to Res.string.anime_media_edit_score_point_5_start_1,
                        40 to Res.string.anime_media_edit_score_point_5_start_2,
                        60 to Res.string.anime_media_edit_score_point_5_start_3,
                        80 to Res.string.anime_media_edit_score_point_5_start_4,
                        100 to Res.string.anime_media_edit_score_point_5_start_5,
                    ).forEach { (starScore, contentDescriptionTextRes) ->
                        IconButton(onClick = {
                            onScoreChange(if (scoreAsInt != starScore) starScore.toString() else "")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.StarRate,
                                contentDescription = stringResource(contentDescriptionTextRes),
                                tint = if (scoreAsInt >= starScore) {
                                    Color.Yellow
                                } else {
                                    LocalContentColor.current
                                }
                            )
                        }
                    }
                }
            }
            else -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    val value: Float
                    val maxRange: Float
                    val steps: Int
                    val onValueChange: (Float) -> Unit
                    @Suppress("KotlinConstantConditions") when (format) {
                        ScoreFormat.POINT_100 -> {
                            value = score().toIntOrNull()?.toFloat() ?: 0f
                            maxRange = 100f
                            steps = 100
                            onValueChange = { onScoreChange(it.toInt().toString()) }
                        }
                        ScoreFormat.POINT_10_DECIMAL -> {
                            value = score().toFloatOrNull() ?: 0f
                            maxRange = 10f
                            steps = 100
                            onValueChange = {
                                onScoreChange(
                                    BigDecimal.fromFloat(it)
                                        .roundToDigitPositionAfterDecimalPoint(
                                            1,
                                            RoundingMode.ROUND_HALF_AWAY_FROM_ZERO
                                        )
                                        .toStringExpanded()
                                )
                            }
                        }
                        ScoreFormat.POINT_10 -> {
                            value = score().toFloatOrNull() ?: 0f
                            maxRange = 10f
                            steps = 10
                            onValueChange = { onScoreChange(it.toInt().toString()) }
                        }
                        ScoreFormat.POINT_5, ScoreFormat.POINT_3 -> throw IllegalStateException("Impossible")
                        ScoreFormat.UNKNOWN__ -> return@Row
                    }

                    Slider(
                        value = value,
                        onValueChange = onValueChange,
                        valueRange = 0f..maxRange,
                        steps = steps,
                        modifier = Modifier.weight(1f),
                    )

                    val score = score()
                    TextField(
                        value = score,
                        onValueChange = onScoreChange,
                        trailingIcon = {
                            val alpha by animateFloatAsState(
                                if (score.isBlank()) 0f else 1f,
                                label = "Progress clear alpha",
                            )
                            IconButton(
                                onClick = { onScoreChange("") },
                                modifier = Modifier.alpha(alpha)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = stringResource(
                                        Res.string.anime_media_edit_clear_content_description
                                    ),
                                )
                            }
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            autoCorrectEnabled = KeyboardOptions.Default.autoCorrectEnabled,
                        ),
                        modifier = Modifier
                            .widthIn(min = 40.dp),
                    )
                }
            }
        }
    }

    @Composable
    private fun SectionHeader(
        titleRes: StringResource,
        modifier: Modifier = Modifier,
        horizontalPadding: Dp = 16.dp,
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier.padding(
                start = horizontalPadding,
                end = horizontalPadding,
                top = 12.dp,
                bottom = 4.dp,
            )
        )
    }

    sealed interface Event {
        data object Save : Event
        data object Delete : Event
        data class StatusChange(val status: MediaListStatus?) : Event
        data class DateChange(val start: Boolean, val selectedMillisUtc: Long?) : Event
        data class Open(
            val mediaId: String,
            val coverImage: String?,
            // TODO: Pass all translations so that UI can react to language changes down the line
            val title: String?,
            val mediaListEntry: MediaDetailsListEntry?,
            val mediaType: MediaType?,
            val status: MediaListStatus?,
            val maxProgress: Int?,
            val maxProgressVolumes: Int?,
            val loading: Boolean = false,
        ) : Event
    }
}
