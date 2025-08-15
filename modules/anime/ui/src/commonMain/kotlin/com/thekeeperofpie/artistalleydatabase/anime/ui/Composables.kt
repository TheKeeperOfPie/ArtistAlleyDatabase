@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.ui.generated.resources.Res
import artistalleydatabase.modules.anime.ui.generated.resources.anime_generic_favorite_is_favorite_icon_content_description
import artistalleydatabase.modules.anime.ui.generated.resources.anime_generic_favorite_is_not_favorite_icon_content_description
import artistalleydatabase.modules.anime.ui.generated.resources.anime_generic_favorites_icon_content_description
import artistalleydatabase.modules.anime.ui.generated.resources.anime_generic_view_all_content_description
import artistalleydatabase.modules.anime.ui.generated.resources.anime_media_edit_date_end_date_label
import artistalleydatabase.modules.anime.ui.generated.resources.anime_media_edit_date_start_date_label
import artistalleydatabase.modules.anime.ui.generated.resources.anime_media_edit_date_today_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.cancel
import artistalleydatabase.modules.utils_compose.generated.resources.confirm
import artistalleydatabase.modules.utils_compose.generated.resources.show_less
import artistalleydatabase.modules.utils_compose.generated.resources.show_more
import artistalleydatabase.modules.utils_compose.generated.resources.view_all
import coil3.request.crossfade
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.utils.DateTimeUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
fun StartEndDateRow(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onRequestDatePicker: (forStart: Boolean) -> Unit,
    onDateChange: (start: Boolean, Long?) -> Unit,
) {
    val localDateUtcNow = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        val startDateString = startDate?.format(DateTimeUtils.shortDateFormat).orEmpty()
        val endDateString = endDate?.format(DateTimeUtils.shortDateFormat).orEmpty()

        val startInteractionSource = remember { MutableInteractionSource() }
        LaunchedEffect(startInteractionSource) {
            startInteractionSource.interactions.collect {
                if (it is PressInteraction.Release) {
                    onRequestDatePicker(true)
                }
            }
        }

        val hasStartDate = startDate != null
        val isTodayBeforeEnd = endDate == null || localDateUtcNow < endDate
                || localDateUtcNow == endDate
        TextField(
            value = startDateString,
            onValueChange = {},
            readOnly = true,
            label = if (hasStartDate) {
                {
                    Text(
                        stringResource(Res.string.anime_media_edit_date_start_date_label)
                    )
                }
            } else null,
            placeholder = { Text(stringResource(Res.string.anime_media_edit_date_start_date_label)) },
            trailingIcon = {
                if (hasStartDate || isTodayBeforeEnd) {
                    IconButton(onClick = {
                        onDateChange(
                            true,
                            if (hasStartDate) {
                                null
                            } else {
                                LocalDate(
                                    localDateUtcNow.year,
                                    localDateUtcNow.month,
                                    localDateUtcNow.dayOfMonth
                                )
                                    .atStartOfDayIn(TimeZone.UTC)
                                    .toEpochMilliseconds()
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
                                Res.string.anime_media_edit_date_today_content_description
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
            startDate == null || localDateUtcNow > startDate || localDateUtcNow == startDate
        TextField(
            value = endDateString,
            onValueChange = {},
            readOnly = true,
            label = if (hasEndDate) {
                {
                    Text(
                        stringResource(Res.string.anime_media_edit_date_end_date_label)
                    )
                }
            } else null,
            placeholder = { Text(stringResource(Res.string.anime_media_edit_date_end_date_label)) },
            trailingIcon = {
                if (hasEndDate || isTodayAfterStart) {
                    IconButton(onClick = {
                        onDateChange(
                            false,
                            if (hasEndDate) {
                                null
                            } else {
                                LocalDate(
                                    localDateUtcNow.year,
                                    localDateUtcNow.month,
                                    localDateUtcNow.dayOfMonth
                                )
                                    .atStartOfDayIn(TimeZone.UTC)
                                    .toEpochMilliseconds()
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
                                Res.string.anime_media_edit_date_today_content_description
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
                    Text(text = stringResource(UtilsStrings.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onShownForStartDateChange(null) }) {
                    Text(text = stringResource(UtilsStrings.cancel))
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            DatePicker(datePickerState)
        }
    }
}

fun <T> LazyGridScope.listSection(
    titleRes: StringResource,
    values: List<T>?,
    valueToId: (T) -> String?,
    aboveFold: Int,
    hasMoreValues: Boolean = false,
    expanded: () -> Boolean = { false },
    onExpandedChange: (Boolean) -> Unit = {},
    hidden: () -> Boolean = { false },
    hiddenContent: @Composable () -> Unit = {},
    onClickViewAll: (() -> Unit)? = null,
    viewAllContentDescriptionTextRes: StringResource? = null,
    loading: Boolean = false,
    headerSideEffect: (() -> Unit)? = null,
    itemContent: @Composable LazyGridItemScope.(T, paddingBottom: Dp) -> Unit,
) {
    // TODO: There must be a better way to do this
    if (headerSideEffect != null) {
        item {
            SideEffect(headerSideEffect)
        }
    }
    if (values != null && values.isEmpty()) return
    item(
        key = "$titleRes-header",
        span = GridUtils.maxSpanFunction,
        contentType = "detailsSectionHeader",
    ) {
        DetailsSectionHeader(
            text = stringResource(titleRes),
            onClickViewAll = onClickViewAll,
            viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
        )
    }
    if (loading) {
        item(key = "$titleRes-loading", span = GridUtils.maxSpanFunction) {
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator()
            }
        }
    }

    if (values == null) return
    listSectionWithoutHeader(
        titleRes = titleRes,
        values = values,
        valueToId = valueToId,
        aboveFold = aboveFold,
        hasMoreValues = hasMoreValues,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        hidden = hidden,
        hiddenContent = hiddenContent,
        onClickViewAll = onClickViewAll,
        itemContent = itemContent,
    )
}

fun <T> LazyGridScope.listSectionWithoutHeader(
    titleRes: StringResource,
    values: List<T>?,
    valueToId: (T) -> String?,
    aboveFold: Int,
    hasMoreValues: Boolean = false,
    noResultsTextRes: StringResource? = null,
    expanded: () -> Boolean = { false },
    onExpandedChange: (Boolean) -> Unit = {},
    hidden: () -> Boolean = { false },
    hiddenContent: @Composable () -> Unit = {},
    onClickViewAll: (() -> Unit)? = null,
    itemContent: @Composable LazyGridItemScope.(T, paddingBottom: Dp) -> Unit,
) {
    if (values == null) return
    if (values.isEmpty()) {
        if (noResultsTextRes != null) {
            item(
                key = "$titleRes-noResults",
                span = GridUtils.maxSpanFunction,
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(noResultsTextRes),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }
        return
    }

    if (hidden()) {
        item(key = "$titleRes-hidden", span = GridUtils.maxSpanFunction) {
            hiddenContent()
        }
        return
    }

    val hasMore = values.size > aboveFold
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
        itemContent(item, paddingBottom)
    }

    fun showAllButton() {
        item("$titleRes-showAll") {
            val modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .animateItem()
            val content: @Composable ColumnScope.() -> Unit = {
                Text(
                    text = stringResource(UtilsStrings.view_all),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            // TODO: Abstract this?
            if (onClickViewAll == null) {
                ElevatedCard(modifier = modifier, content = content)
            } else {
                ElevatedCard(onClick = onClickViewAll, modifier = modifier, content = content)
            }
        }
    }

    if (hasMore) {
        if (expanded()) {
            itemsIndexed(
                values.drop(aboveFold),
                { index, item -> "$titleRes-${valueToId(item) ?: (index + aboveFold)}" },
            ) { _, item ->
                itemContent(item, 16.dp)
            }

            if (hasMoreValues) {
                showAllButton()
            } else {
                item(key = "$titleRes-showLess", span = GridUtils.maxSpanFunction) {
                    ElevatedCard(
                        onClick = { onExpandedChange(false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .animateItem()
                    ) {
                        Text(
                            text = stringResource(UtilsStrings.show_less),
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        } else {
            item(key = "$titleRes-showMore", span = GridUtils.maxSpanFunction) {
                ElevatedCard(
                    onClick = { onExpandedChange(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .animateItem()
                ) {
                    Text(
                        text = stringResource(UtilsStrings.show_more),
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

@Composable
fun GenericViewAllCard(
    onClick: () -> Unit,
    width: Dp = 120.dp,
    height: Dp = 180.dp,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .size(width = width, height = height)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = stringResource(
                    Res.string.anime_generic_view_all_content_description
                )
            )
        }
    }
}

// TODO: Placeholder background color
@Composable
fun ListRowSmallImage(
    ignored: Boolean,
    imageState: CoilImageState?,
    contentDescriptionTextRes: StringResource,
    modifier: Modifier = Modifier,
    width: Dp = 64.dp,
    height: Dp = 96.dp,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    CoilImage(
        state = imageState,
        model = imageState.request()
            .size(
                width = density.run { width.roundToPx() },
                height = density.run { height.roundToPx() },
            )
            .crossfade(true)
            .build(),
        contentScale = ContentScale.Crop,
        contentDescription = stringResource(contentDescriptionTextRes),
        modifier = Modifier
            .size(width = width, height = height)
            .then(modifier)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
            .clickable { onClick() }
            .placeholder(
                visible = imageState?.success != true,
                highlight = PlaceholderHighlight.shimmer(),
            )
            .alpha(if (ignored) 0.38f else 1f)
    )
}

@Composable
fun ListRowFavoritesSection(
    loading: Boolean,
    favorites: Int?,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = favorites?.toString() ?: "000",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.placeholder(
                    visible = loading,
                    highlight = PlaceholderHighlight.shimmer(),
                ),
            )

            Icon(
                imageVector = when {
                    favorites == null -> Icons.Outlined.PeopleAlt
                    favorites > 2000 -> Icons.Filled.PeopleAlt
                    favorites > 1000 -> Icons.Outlined.PeopleAlt
                    favorites > 100 -> Icons.Filled.Person
                    else -> Icons.Filled.PersonOutline
                },
                contentDescription = stringResource(
                    Res.string.anime_generic_favorites_icon_content_description
                ),
                modifier = Modifier.placeholder(
                    visible = loading,
                    highlight = PlaceholderHighlight.shimmer(),
                ),
            )
        }
    }
}

@Composable
fun FavoriteIconButton(
    favorite: Boolean?,
    onFavoriteChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = favorite != null, modifier = modifier) {
        if (favorite != null) {
            IconButton(onClick = { onFavoriteChanged(!favorite) }) {
                Icon(
                    imageVector = if (favorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Filled.FavoriteBorder
                    },
                    contentDescription = stringResource(
                        if (favorite) {
                            Res.string.anime_generic_favorite_is_favorite_icon_content_description
                        } else {
                            Res.string.anime_generic_favorite_is_not_favorite_icon_content_description
                        }
                    )
                )
            }
        }
    }
}
