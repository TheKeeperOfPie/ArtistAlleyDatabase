@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun StartEndDateRow(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onRequestDatePicker: (forStart: Boolean) -> Unit,
    onDateChange: (start: Boolean, Long?) -> Unit,
) {
    val localDateUtcNow = LocalDate.now()
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        val startDateString =
            startDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                .orEmpty()
        val endDateString =
            endDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                .orEmpty()

        val startInteractionSource = remember { MutableInteractionSource() }
        LaunchedEffect(startInteractionSource) {
            startInteractionSource.interactions.collect {
                if (it is PressInteraction.Release) {
                    onRequestDatePicker(true)
                }
            }
        }

        val hasStartDate = startDate != null
        val isTodayBeforeEnd = endDate == null || localDateUtcNow.isBefore(endDate)
                || localDateUtcNow.isEqual(endDate)
        TextField(
            value = startDateString,
            onValueChange = {},
            readOnly = true,
            label = if (hasStartDate) {
                {
                    Text(
                        stringResource(R.string.anime_media_edit_date_start_date_label)
                    )
                }
            } else null,
            placeholder = { Text(stringResource(R.string.anime_media_edit_date_start_date_label)) },
            trailingIcon = {
                if (hasStartDate || isTodayBeforeEnd) {
                    IconButton(onClick = {
                        onDateChange(
                            true,
                            if (hasStartDate) {
                                null
                            } else {
                                LocalDate.of(
                                    localDateUtcNow.year,
                                    localDateUtcNow.month,
                                    localDateUtcNow.dayOfMonth
                                )
                                    .atStartOfDay(ZoneOffset.UTC)
                                    .toInstant()
                                    .toEpochMilli()
                            }
                        )
                    }) {
                        Icon(
                            imageVector = if (hasStartDate) {
                                Icons.Filled.Clear
                            } else {
                                Icons.Filled.CalendarToday
                            },
                            contentDescription = stringResource(
                                R.string.anime_media_edit_date_today_content_description
                            ),
                        )
                    }
                }
            },
            interactionSource = startInteractionSource,
            modifier = Modifier
                .weight(1f),
        )

        val endInteractionSource = remember { MutableInteractionSource() }
        LaunchedEffect(endInteractionSource) {
            endInteractionSource.interactions.collect {
                if (it is PressInteraction.Release) {
                    onRequestDatePicker(false)
                }
            }
        }

        val hasEndDate = endDate != null
        val isTodayAfterStart =
            startDate == null || localDateUtcNow.isAfter(startDate)
                    || localDateUtcNow.isEqual(startDate)
        TextField(
            value = endDateString,
            onValueChange = {},
            readOnly = true,
            label = if (hasEndDate) {
                {
                    Text(
                        stringResource(R.string.anime_media_edit_date_end_date_label)
                    )
                }
            } else null,
            placeholder = { Text(stringResource(R.string.anime_media_edit_date_end_date_label)) },
            trailingIcon = {
                if (hasEndDate || isTodayAfterStart) {
                    IconButton(onClick = {
                        onDateChange(
                            false,
                            if (hasEndDate) {
                                null
                            } else {
                                LocalDate.of(
                                    localDateUtcNow.year,
                                    localDateUtcNow.month,
                                    localDateUtcNow.dayOfMonth
                                )
                                    .atStartOfDay(ZoneOffset.UTC)
                                    .toInstant()
                                    .toEpochMilli()
                            }
                        )
                    }) {
                        Icon(
                            imageVector = if (hasEndDate) {
                                Icons.Filled.Clear
                            } else {
                                Icons.Filled.CalendarToday
                            },
                            contentDescription = stringResource(
                                R.string.anime_media_edit_date_today_content_description
                            ),
                        )
                    }
                }
            },
            interactionSource = endInteractionSource,
            modifier = Modifier
                .weight(1f),
        )
    }
}

@Composable
fun StartEndDateDialog(
    shownForStartDate: Boolean?,
    onShownForStartDateChange: (Boolean?) -> Unit,
    onDateChange: (start: Boolean, Long?) -> Unit,
) {
    if (shownForStartDate != null) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
        DatePickerDialog(
            onDismissRequest = { onShownForStartDateChange(null) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onShownForStartDateChange(null)
                        onDateChange(shownForStartDate, datePickerState.selectedDateMillis)
                    },
                    enabled = confirmEnabled,
                ) {
                    Text(text = stringResource(UtilsStringR.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onShownForStartDateChange(null) }) {
                    Text(text = stringResource(UtilsStringR.cancel))
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            DatePicker(datePickerState)
        }
    }
}

fun <T> LazyListScope.listSection(
    @StringRes titleRes: Int,
    values: Collection<T>,
    valueToId: (T) -> String?,
    aboveFold: Int,
    hasMoreValues: Boolean = false,
    expanded: () -> Boolean = { false },
    onExpandedChange: (Boolean) -> Unit = {},
    hidden: () -> Boolean = { false },
    hiddenContent: @Composable () -> Unit = {},
    onClickViewAll: (() -> Unit)? = null,
    @StringRes viewAllContentDescriptionTextRes: Int? = null,
    itemContent: @Composable LazyListScope.(T, paddingBottom: Dp, modifier: Modifier) -> Unit,
) {
    if (values.isNotEmpty()) {
        val hasMore = values.size > aboveFold
        item("$titleRes-header") {
            DetailsSectionHeader(
                text = stringResource(titleRes),
                modifier = Modifier.clickable(
                    enabled = hasMore || hasMoreValues,
                    onClick = {
                        if (hasMore) {
                            onExpandedChange(!expanded())
                        } else {
                            onClickViewAll?.invoke()
                        }
                    },
                ),
                onClickViewAll = onClickViewAll,
                viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes
            )
        }

        if (hidden()) {
            item("$titleRes-hidden") {
                hiddenContent()
            }
            return
        }

        itemsIndexed(
            values.take(aboveFold),
            { index, item -> "$titleRes-${valueToId(item) ?: index}" },
        ) { index, item ->
            val paddingBottom = if (index == values.size
                    .coerceAtMost(aboveFold) - 1
            ) {
                if (hasMore || hasMoreValues) 16.dp else 0.dp
            } else {
                16.dp
            }
            this@listSection.itemContent(item, paddingBottom, Modifier.animateItemPlacement())
        }

        fun showAllButton() {
            item("$titleRes-showAll") {
                ElevatedCard(
                    onClick = { onClickViewAll?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .animateItemPlacement()
                ) {
                    Text(
                        text = stringResource(UtilsStringR.view_all),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        if (hasMore) {
            if (expanded()) {
                itemsIndexed(
                    values.drop(aboveFold),
                    { index, item -> "$titleRes-${valueToId(item) ?: (index + aboveFold)}" },
                ) { _, item ->
                    this@listSection.itemContent(item, 16.dp, Modifier.animateItemPlacement())
                }

                if (hasMoreValues) {
                    showAllButton()
                } else {
                    item("$titleRes-showLess") {
                        ElevatedCard(
                            onClick = { onExpandedChange(false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .animateItemPlacement()
                        ) {
                            Text(
                                text = stringResource(UtilsStringR.show_less),
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            } else {
                item("$titleRes-showMore") {
                    ElevatedCard(
                        onClick = { onExpandedChange(true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .animateItemPlacement()
                    ) {
                        Text(
                            text = stringResource(UtilsStringR.show_more),
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        } else if (hasMoreValues) {
            showAllButton()
        }
    }
}
