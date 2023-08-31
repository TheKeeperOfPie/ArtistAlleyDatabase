@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.PeopleAlt
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
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
    values: Collection<T>?,
    valueToId: (T) -> String?,
    aboveFold: Int,
    hasMoreValues: Boolean = false,
    expanded: () -> Boolean = { false },
    onExpandedChange: (Boolean) -> Unit = {},
    hidden: () -> Boolean = { false },
    hiddenContent: @Composable () -> Unit = {},
    onClickViewAll: ((AnimeNavigator.NavigationCallback) -> Unit)? = null,
    @StringRes viewAllContentDescriptionTextRes: Int? = null,
    itemContent: @Composable LazyListScope.(T, paddingBottom: Dp, modifier: Modifier) -> Unit,
) {
    if (values != null && values.isEmpty()) return
    item("$titleRes-header") {
        val navigationCallback = LocalNavigationCallback.current
        DetailsSectionHeader(
            text = stringResource(titleRes),
            modifier = Modifier.clickable(
                enabled = onClickViewAll != null,
                onClick = { onClickViewAll?.invoke(navigationCallback) },
            ),
            onClickViewAll = onClickViewAll?.let { { it(navigationCallback) } },
            viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes
        )
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

fun <T> LazyListScope.listSectionWithoutHeader(
    @StringRes titleRes: Int,
    values: Collection<T>?,
    valueToId: (T) -> String?,
    aboveFold: Int,
    hasMoreValues: Boolean = false,
    noResultsTextRes: Int? = null,
    expanded: () -> Boolean = { false },
    onExpandedChange: (Boolean) -> Unit = {},
    hidden: () -> Boolean = { false },
    hiddenContent: @Composable () -> Unit = {},
    onClickViewAll: ((AnimeNavigator.NavigationCallback) -> Unit)? = null,
    itemContent: @Composable LazyListScope.(T, paddingBottom: Dp, modifier: Modifier) -> Unit,
) {
    if (values == null) return
    if (values.isEmpty()) {
        if (noResultsTextRes != null) {
            item("$titleRes-noResults") {
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
        item("$titleRes-hidden") {
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
        this@listSectionWithoutHeader.itemContent(
            item,
            paddingBottom,
            Modifier.animateItemPlacement()
        )
    }

    fun showAllButton() {
        item("$titleRes-showAll") {
            val navigationCallback = LocalNavigationCallback.current
            ElevatedCard(
                onClick = { onClickViewAll?.invoke(navigationCallback) },
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
                this@listSectionWithoutHeader
                    .itemContent(item, 16.dp, Modifier.animateItemPlacement())
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
                imageVector = Icons.Filled.OpenInNew,
                contentDescription = stringResource(
                    R.string.anime_generic_view_all_content_description
                )
            )
        }
    }
}

// TODO: Placeholder background color
@Composable
fun ListRowSmallImage(
    context: Context,
    density: Density,
    ignored: Boolean,
    image: String?,
    @StringRes contentDescriptionTextRes: Int,
    width: Dp = 64.dp,
    height: Dp = 96.dp,
    onClick: (imageWidthToHeightRatio: Float) -> Unit,
) {
    var imageWidthToHeightRatio by remember { mutableStateOf<Float?>(null) }
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(image)
            .size(
                width = density.run { width.roundToPx() },
                height = density.run { height.roundToPx() },
            )
            .crossfade(true)
            .build(),
        contentScale = ContentScale.Crop,
        contentDescription = stringResource(contentDescriptionTextRes),
        onSuccess = { imageWidthToHeightRatio = it.widthToHeightRatio() },
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
            .clickable { onClick(imageWidthToHeightRatio ?: 1f) }
            .placeholder(
                visible = imageWidthToHeightRatio == null,
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
                    R.string.anime_generic_favorites_icon_content_description
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
                            R.string.anime_generic_favorite_is_favorite_icon_content_description
                        } else {
                            R.string.anime_generic_favorite_is_not_favorite_icon_content_description
                        }
                    )
                )
            }
        }
    }
}


@Composable
fun NavigationHeader(
    @StringRes titleRes: Int,
    viewAllRoute: String?,
    @StringRes viewAllContentDescriptionTextRes: Int? = null,
    modifier: Modifier = Modifier,
) {
    val navigationCallback = LocalNavigationCallback.current
    DetailsSectionHeader(
        text = stringResource(titleRes),
        onClickViewAll = viewAllRoute?.let {
            {
                navigationCallback.navigate(viewAllRoute)
            }
        },
        viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
        modifier = Modifier
            .clickable(enabled = viewAllRoute != null) {
                navigationCallback.navigate(viewAllRoute!!)
            }
            .then(modifier)
    )
}
