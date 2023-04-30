package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.anilist.type.ScoreFormat
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateDialog
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateRow
import com.thekeeperofpie.artistalleydatabase.compose.ItemDropdown
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import java.time.LocalDate

@Suppress("NAME_SHADOWING")
object AnimeMediaEditScreen {

    @Composable
    operator fun invoke(
        id: () -> String?,
        type: () -> MediaType?,
        updatedAt: () -> Long?,
        createdAt: () -> Long?,
        progressMax: () -> Int,
        status: () -> MediaListStatus?,
        onStatusChange: (MediaListStatus?) -> Unit,
        scoreFormat: @Composable () -> ScoreFormat,
        score: () -> String,
        onScoreChange: (String) -> Unit,
        progress: () -> String,
        onProgressChange: (String) -> Unit,
        repeat: () -> String,
        onRepeatChange: (String) -> Unit,
        priority: () -> String,
        onPriorityChange: (String) -> Unit,
        private: () -> Boolean,
        onPrivateChange: (Boolean) -> Unit,
        startDate: () -> LocalDate?,
        endDate: () -> LocalDate?,
        onDateChange: (start: Boolean, Long?) -> Unit,
        deleting: () -> Boolean,
        onClickDelete: () -> Unit,
        saving: () -> Boolean,
        onClickSave: () -> Unit,
        errorRes: () -> Pair<Int, Exception?>?,
        onErrorDismiss: () -> Unit,
    ) {
        var startEndDateShown by remember { mutableStateOf<Boolean?>(null) }
        Scaffold(
            snackbarHost = {
                SnackbarErrorText(
                    errorRes()?.first,
                    errorRes()?.second,
                    onErrorDismiss = onErrorDismiss,
                )
            },
        ) {
            Column(modifier = Modifier.padding(it)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    val isAnime = type() == MediaType.ANIME
                    SectionHeader(R.string.anime_media_edit_status_label)
                    ItemDropdown(
                        value = stringResource(status().toTextRes(isAnime)),
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
                        textForValue = { stringResource(it.toTextRes(isAnime)) },
                        onSelectItem = onStatusChange,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    TwoColumn(
                        labelOneRes = R.string.anime_media_edit_progress_label,
                        columnOneContent = {
                            val progress = progress()
                            TextField(
                                value = progress,
                                onValueChange = onProgressChange,
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
                                                onProgressChange((it - 1).toString())
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
                                        ?.let { it < progressMax() } == true
                                    val alpha by animateFloatAsState(
                                        if (visible) 1f else 0f,
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
                            val repeat = repeat()
                            TextField(
                                value = repeat,
                                onValueChange = onRepeatChange,
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
                                                onRepeatChange((it - 1).toString())
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
                                                onRepeatChange("1")
                                            } else {
                                                repeat.toIntOrNull()?.let {
                                                    onRepeatChange((it + 1).toString())
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
                        format = scoreFormat,
                        score = score,
                        onScoreChange = onScoreChange,
                    )

                    SectionHeader(R.string.anime_media_edit_date_label)
                    StartEndDateRow(
                        startDate = startDate(),
                        endDate = endDate(),
                        onRequestDatePicker = { startEndDateShown = it },
                        onDateChange = onDateChange,
                    )

                    if (startEndDateShown != null) {
                        StartEndDateDialog(
                            shownForStartDate = startEndDateShown,
                            onShownForStartDateToggled = { startEndDateShown = it },
                            onDateChange = onDateChange,
                        )
                    }

                    TwoColumn(
                        labelOneRes = R.string.anime_media_edit_priority_label,
                        columnOneContent = {
                            val priority = priority()
                            TextField(
                                value = priority,
                                onValueChange = onPriorityChange,
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
                                                onPriorityChange((it - 1).toString())
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
                                                onPriorityChange("1")
                                            } else {
                                                priority.toIntOrNull()?.let {
                                                    onPriorityChange((it + 1).toString())
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
                                value = private().toPrivateText(),
                                iconContentDescription = R.string.anime_media_edit_private_dropdown_content_description,
                                values = {
                                    listOf(
                                        true,
                                        false,
                                    )
                                },
                                textForValue = { it.toPrivateText() },
                                onSelectItem = onPrivateChange,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    )

                    val createdAt = createdAt()
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
                            modifier = Modifier.padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 4.dp
                            ),
                        )
                    }

                    val updatedAt = updatedAt()
                    if (updatedAt != null) {
                        Text(
                            text = stringResource(
                                R.string.anime_media_edit_updated_at,
                                MediaUtils.formatEntryDateTime(
                                    LocalContext.current,
                                    updatedAt * 1000,
                                ),
                            ),
                            modifier = Modifier.padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = if (createdAtShown) 4.dp else 16.dp,
                                bottom = 10.dp
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                Divider()

                Row(modifier = Modifier.align(Alignment.End)) {
                    if (id() != null) {
                        TextButton(onClick = onClickDelete) {
                            Crossfade(
                                targetState = deleting(),
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

                    TextButton(onClick = onClickSave) {
                        Crossfade(
                            targetState = saving(),
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
                    bottom = 8.dp,
                )
        )
    }
}
