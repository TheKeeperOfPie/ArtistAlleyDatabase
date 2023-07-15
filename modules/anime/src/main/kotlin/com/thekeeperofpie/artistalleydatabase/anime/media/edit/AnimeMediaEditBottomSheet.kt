package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.fragment.MediaNavigationData
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.anilist.type.ScoreFormat
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toStatusIcon
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateDialog
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateRow
import com.thekeeperofpie.artistalleydatabase.compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.ItemDropdown
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(ExperimentalFoundationApi::class)
@Suppress("NAME_SHADOWING")
object AnimeMediaEditBottomSheet {

    private val DEFAULT_IMAGE_HEIGHT = 100.dp
    private val DEFAULT_IMAGE_WIDTH = 72.dp

    @Composable
    operator fun invoke(
        screenKey: String,
        viewModel: MediaEditViewModel,
        modifier: Modifier = Modifier,
        onLongPressImage: (MediaNavigationData) -> Unit = { /* TODO */ },
        onDismiss: () -> Unit,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        var showDelete by remember { mutableStateOf(false) }

        Divider()

        val initialParams by viewModel.initialParams.collectAsState()
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .wrapContentHeight()
        ) {
            Crossfade(targetState = initialParams?.loading, label = "Media edit sheet crossfade") {
                if (it == true) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Column {
                        Form(
                            screenKey = screenKey,
                            viewModel = viewModel,
                            initialParams = initialParams,
                            onLongPressImage = onLongPressImage,
                            colorCalculationState = colorCalculationState,
                            navigationCallback = navigationCallback,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Divider()

            Row(modifier = Modifier.align(Alignment.End)) {
                if (initialParams?.id != null) {
                    TextButton(onClick = { showDelete = true }) {
                        Crossfade(
                            targetState = viewModel.editData.deleting,
                            label = "Media edit delete indicator crossfade"
                        ) {
                            if (it) {
                                CircularProgressIndicator()
                            } else {
                                Text(
                                    text = stringResource(UtilsStringR.delete),
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 10.dp
                                    )
                                )
                            }
                        }
                    }
                }

                TextButton(onClick = viewModel::onClickSave) {
                    Crossfade(
                        targetState = viewModel.editData.saving,
                        label = "Media edit save indicator crossfade"
                    ) {
                        if (it) {
                            CircularProgressIndicator()
                        } else {
                            Text(
                                text = stringResource(UtilsStringR.save),
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

        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                title = { Text(stringResource(R.string.anime_media_edit_confirm_delete_title)) },
                confirmButton = {
                    TextButton(onClick = {
                        showDelete = false
                        viewModel.onClickDelete()
                    }) {
                        Text(stringResource(UtilsStringR.yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDelete = false }) {
                        Text(stringResource(UtilsStringR.no))
                    }
                },
            )
        } else if (viewModel.editData.showConfirmClose) {
            AlertDialog(
                onDismissRequest = { viewModel.editData.showConfirmClose = false },
                title = { Text(stringResource(R.string.anime_media_edit_confirm_close_title)) },
                text = { Text(stringResource(R.string.anime_media_edit_confirm_close_text)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.editData.showConfirmClose = false
                        viewModel.onClickSave()
                    }) {
                        Text(stringResource(UtilsStringR.yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.editData.showConfirmClose = false
                        viewModel.editData.showing = false
                        viewModel.dismissRequests.tryEmit(System.currentTimeMillis())
                        onDismiss()
                    }) {
                        Text(stringResource(UtilsStringR.no))
                    }
                },
            )
        }
    }

    @Composable
    private fun ColumnScope.Form(
        screenKey: String,
        viewModel: MediaEditViewModel,
        initialParams: MediaEditData.InitialParams?,
        onLongPressImage: (MediaNavigationData) -> Unit = { /* TODO */ },
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        var startEndDateShown by remember { mutableStateOf<Boolean?>(null) }
        val media = initialParams?.media
        if (media != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                SharedElement(
                    key = "anime_media_${media.id}_image",
                    screenKey = screenKey,
                ) {
                    var imageWidthToHeightRatio by remember { mutableFloatStateOf(1f) }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(media.coverImage?.extraLarge)
                            .crossfade(true)
                            .allowHardware(colorCalculationState.hasColor(media.id.toString()))
                            .size(
                                width = Dimension.Pixels(
                                    LocalDensity.current.run { DEFAULT_IMAGE_WIDTH.roundToPx() }
                                ),
                                height = Dimension.Undefined
                            )
                            .build(),
                        contentScale = ContentScale.Crop,
                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                        onSuccess = {
                            imageWidthToHeightRatio = it.widthToHeightRatio()
                            ComposeColorUtils.calculatePalette(
                                media.id.toString(),
                                it,
                                colorCalculationState,
                            )
                        },
                        contentDescription = stringResource(R.string.anime_media_cover_image_content_description),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .size(width = DEFAULT_IMAGE_WIDTH, height = DEFAULT_IMAGE_HEIGHT)
                            .combinedClickable(
                                onClick = {
                                    navigationCallback.onMediaClick(
                                        media,
                                        imageWidthToHeightRatio,
                                    )
                                },
                                onLongClick = { onLongPressImage(media) },
                                onLongClickLabel = stringResource(
                                    R.string.anime_media_cover_image_long_press_preview
                                ),
                            )
                    )
                }

                AutoSizeText(
                    text = media.title?.userPreferred.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .weight(1f)
                )
            }

            Divider()
        }

        val isAnime = initialParams?.mediaType == MediaType.ANIME
        SectionHeader(R.string.anime_media_edit_status_label)
        ItemDropdown(
            value = viewModel.editData.status,
            iconContentDescription = R.string.anime_media_edit_status_dropdown_content_description,
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
            onSelectItem = viewModel::onStatusChange,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        TwoColumn(
            labelOneRes = R.string.anime_media_edit_progress_label,
            columnOneContent = {
                val progress = viewModel.editData.progress
                TextField(
                    value = progress,
                    onValueChange = { viewModel.editData.progress = it },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        val visible = progress.isNotBlank()
                                && progress.toIntOrNull()?.let { it > 0 } == true
                        val alpha by animateFloatAsState(
                            if (visible) 1f else 0f,
                            label = "Progress decrement alpha",
                        )
                        IconButton(
                            enabled = visible,
                            onClick = {
                                progress.toIntOrNull()?.let {
                                    viewModel.editData.progress = (it - 1).toString()
                                }
                            },
                            modifier = Modifier.alpha(alpha)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RemoveCircleOutline,
                                contentDescription = stringResource(
                                    R.string.anime_media_edit_progress_decrement_content_description
                                ),
                            )
                        }
                    },
                    trailingIcon = {
                        val visible = progress.isBlank()
                                || progress.toIntOrNull()
                            ?.let { it < (initialParams?.maxProgress ?: 1) } == true
                        val alpha by animateFloatAsState(
                            if (visible) 1f else 0f,
                            label = "Progress increment alpha",
                        )
                        IconButton(
                            enabled = visible,
                            onClick = {
                                if (progress.isBlank()) {
                                    viewModel.editData.progress = "1"
                                } else {
                                    progress.toIntOrNull()?.let {
                                        viewModel.editData.progress = (it + 1).toString()
                                    }
                                }
                            },
                            modifier = Modifier.alpha(alpha)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddCircleOutline,
                                contentDescription = stringResource(
                                    R.string.anime_media_edit_progress_increment_content_description
                                ),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            },
            labelTwoRes = R.string.anime_media_edit_repeat_label,
            columnTwoContent = {
                val repeat = viewModel.editData.repeat
                TextField(
                    value = repeat,
                    onValueChange = { viewModel.editData.repeat = it },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        val visible = repeat.isNotBlank()
                                && repeat.toIntOrNull()?.let { it > 0 } == true
                        val alpha by animateFloatAsState(
                            if (visible) 1f else 0f,
                            label = "Repeat decrement alpha",
                        )
                        IconButton(
                            enabled = visible,
                            onClick = {
                                repeat.toIntOrNull()?.let {
                                    viewModel.editData.repeat = (it - 1).toString()
                                }
                            },
                            modifier = Modifier.alpha(alpha),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RemoveCircleOutline,
                                contentDescription = stringResource(
                                    R.string.anime_media_edit_repeat_decrement_content_description
                                ),
                            )
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (repeat.isBlank()) {
                                    viewModel.editData.repeat = "1"
                                } else {
                                    repeat.toIntOrNull()?.let {
                                        viewModel.editData.repeat = (it + 1).toString()
                                    }
                                }
                            }) {
                            Icon(
                                imageVector = Icons.Filled.AddCircleOutline,
                                contentDescription = stringResource(
                                    R.string.anime_media_edit_repeat_increment_content_description
                                ),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        )

        ScoreSection(
            format = { viewModel.scoreFormat.collectAsState().value },
            score = { viewModel.editData.score },
            onScoreChange = { viewModel.editData.score = it },
        )

        SectionHeader(R.string.anime_media_edit_date_label)
        StartEndDateRow(
            startDate = viewModel.editData.startDate,
            endDate = viewModel.editData.endDate,
            onRequestDatePicker = { startEndDateShown = it },
            onDateChange = viewModel::onDateChange,
        )

        if (startEndDateShown != null) {
            StartEndDateDialog(
                shownForStartDate = startEndDateShown,
                onShownForStartDateChange = { startEndDateShown = it },
                onDateChange = viewModel::onDateChange,
            )
        }

        TwoColumn(
            labelOneRes = R.string.anime_media_edit_priority_label,
            columnOneContent = {
                val priority = viewModel.editData.priority
                TextField(
                    value = priority,
                    onValueChange = { viewModel.editData.priority = it },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        val visible = priority.isNotBlank()
                                && priority.toIntOrNull()?.let { it > 0 } == true
                        val alpha by animateFloatAsState(
                            if (visible) 1f else 0f,
                            label = "Priority decrement alpha",
                        )
                        IconButton(
                            enabled = visible,
                            onClick = {
                                priority.toIntOrNull()?.let {
                                    viewModel.editData.priority = (it - 1).toString()
                                }
                            },
                            modifier = Modifier.alpha(alpha),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RemoveCircleOutline,
                                contentDescription = stringResource(
                                    R.string.anime_media_edit_priority_decrement_content_description
                                ),
                            )
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (priority.isBlank()) {
                                    viewModel.editData.priority = "1"
                                } else {
                                    priority.toIntOrNull()?.let {
                                        viewModel.editData.priority = (it + 1).toString()
                                    }
                                }
                            }) {
                            Icon(
                                imageVector = Icons.Filled.AddCircleOutline,
                                contentDescription = stringResource(
                                    R.string.anime_media_edit_priority_increment_content_description
                                ),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            labelTwoRes = R.string.anime_media_edit_private_label,
            columnTwoContent = {
                @Composable
                fun Boolean.toPrivateText() = when (this) {
                    true -> R.string.anime_media_edit_private_true
                    false -> R.string.anime_media_edit_private_false
                }.let { stringResource(it) }
                ItemDropdown(
                    value = viewModel.editData.private,
                    iconContentDescription = R.string.anime_media_edit_private_dropdown_content_description,
                    values = {
                        listOf(
                            true,
                            false,
                        )
                    },
                    textForValue = { it.toPrivateText() },
                    onSelectItem = { viewModel.editData.private = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        )

        val createdAt = viewModel.editData.createdAt
        val createdAtShown = createdAt != null && createdAt > 0
        if (createdAtShown) {
            Text(
                text = stringResource(
                    R.string.anime_media_edit_created_at,
                    MediaUtils.formatEntryDateTime(
                        LocalContext.current,
                        createdAt!! * 1000,
                    ),
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        val updatedAt = viewModel.editData.updatedAt
        if (updatedAt != null && updatedAt > 0) {
            Text(
                text = stringResource(
                    R.string.anime_media_edit_updated_at,
                    MediaUtils.formatEntryDateTime(
                        LocalContext.current,
                        updatedAt * 1000,
                    ),
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = if (createdAtShown) 0.dp else 8.dp,
                    bottom = 8.dp
                ),
            )
        }
    }

    @Composable
    private fun TwoColumn(
        @StringRes labelOneRes: Int,
        columnOneContent: @Composable () -> Unit,
        @StringRes labelTwoRes: Int,
        columnTwoContent: @Composable () -> Unit,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionHeader(labelOneRes, horizontalPadding = 0.dp)
                columnOneContent()
            }
            Column(modifier = Modifier.weight(1f)) {
                SectionHeader(labelTwoRes, horizontalPadding = 0.dp)
                columnTwoContent()
            }
        }
    }

    @Composable
    private fun ColumnScope.ScoreSection(
        format: @Composable () -> ScoreFormat,
        score: () -> String,
        onScoreChange: (String) -> Unit,
    ) {
        SectionHeader(R.string.anime_media_edit_score_label)

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
                                R.string.anime_media_edit_score_point_3_dissatisfied
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
                                R.string.anime_media_edit_score_point_3_neutral
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
                                R.string.anime_media_edit_score_point_3_satisfied
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
                        20 to R.string.anime_media_edit_score_point_5_start_1,
                        40 to R.string.anime_media_edit_score_point_5_start_2,
                        60 to R.string.anime_media_edit_score_point_5_start_3,
                        80 to R.string.anime_media_edit_score_point_5_start_4,
                        100 to R.string.anime_media_edit_score_point_5_start_5,
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
                            onValueChange = { onScoreChange(String.format("%.1f", it)) }
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

                    // TODO: Add button to clear score
                    TextField(
                        value = score(),
                        onValueChange = onScoreChange,
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .widthIn(min = 40.dp),
                    )
                }
            }
        }
    }

    @Composable
    private fun SectionHeader(@StringRes titleRes: Int, horizontalPadding: Dp = 16.dp) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = 12.dp,
                    bottom = 4.dp,
                )
        )
    }
}
