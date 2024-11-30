@file:OptIn(ExperimentalLayoutApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.media.data.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.media.data.generated.resources.Res
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_advanced
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_basic
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_decrement_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_dropdown_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_increment_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_year
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_year_clear_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_year_decrement_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_year_increment_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_year_today_content_description
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateRow
import com.thekeeperofpie.artistalleydatabase.utils_compose.ItemDropdown
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.MinWidthTextField
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.CustomFilterSection
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

// TODO: This should probably live somewhere else

@Composable
fun AiringDateSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    data: @Composable () -> AiringDate,
    onSeasonChange: (AiringDate.SeasonOption?) -> Unit,
    onSeasonYearChange: (String) -> Unit,
    onIsAdvancedToggle: (Boolean) -> Unit,
    onRequestDatePicker: (Boolean) -> Unit,
    onDateChange: (start: Boolean, Long?) -> Unit,
    showDivider: Boolean,
) {
    @Suppress("NAME_SHADOWING")
    val expanded = expanded()

    @Suppress("NAME_SHADOWING")
    val data = data()
    CustomFilterSection(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        titleRes = Res.string.anime_media_filter_airing_date,
        titleDropdownContentDescriptionRes = Res.string.anime_media_filter_airing_date_content_description,
        summaryText = {
            when (data) {
                is AiringDate.Basic -> {
                    val season = data.season
                    val seasonYear = data.seasonYear.toIntOrNull()
                    when {
                        season != null && seasonYear != null ->
                            "${season.text()} - $seasonYear"
                        season != null -> season.text()
                        seasonYear != null -> seasonYear.toString()
                        else -> null
                    }
                }
                is AiringDate.Advanced -> data.summaryText()
            }
        },
        onSummaryClick = {
            when (data) {
                is AiringDate.Basic -> {
                    onSeasonChange(null)
                    onSeasonYearChange("")
                }
                is AiringDate.Advanced -> {
                    onDateChange(true, null)
                    onDateChange(false, null)
                }
            }
        },
        showDivider = showDivider,
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column {
                @Suppress("NAME_SHADOWING")
                val data = data()
                val tabIndex = if (data is AiringDate.Basic) 0 else 1
                TabRow(
                    selectedTabIndex = tabIndex,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Tab(
                        selected = tabIndex == 0,
                        text = { Text(stringResource(Res.string.anime_media_filter_airing_date_season_basic)) },
                        onClick = { onIsAdvancedToggle(false) },
                    )

                    Tab(
                        selected = tabIndex == 1,
                        text = { Text(stringResource(Res.string.anime_media_filter_airing_date_season_advanced)) },
                        onClick = { onIsAdvancedToggle(true) },
                    )
                }

                when (data) {
                    is AiringDate.Basic ->
                        AiringDateBasicSection(
                            data = data,
                            onSeasonChange = onSeasonChange,
                            onSeasonYearChange = onSeasonYearChange,
                        )
                    is AiringDate.Advanced -> AiringDateAdvancedSection(
                        data = data,
                        onRequestDatePicker = onRequestDatePicker,
                        onDateChange = onDateChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun AiringDateBasicSection(
    data: AiringDate.Basic,
    onSeasonChange: (AiringDate.SeasonOption?) -> Unit,
    onSeasonYearChange: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
    ) {
        IconButton(
            onClick = {
                val previousSeason = data.previousSeason()
                onSeasonChange(previousSeason.first)
                onSeasonYearChange(previousSeason.second.toString())
            },
            modifier = Modifier.fillMaxHeight()
        ) {
            Icon(
                imageVector = Icons.Default.RemoveCircleOutline,
                contentDescription = stringResource(
                    Res.string.anime_media_filter_airing_date_season_decrement_content_description
                )
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            ItemDropdown(
                label = Res.string.anime_media_filter_airing_date_season,
                value = data.season,
                iconContentDescription = Res.string.anime_media_filter_airing_date_season_dropdown_content_description,
                values = { listOf(null) + AiringDate.SeasonOption.entries },
                textForValue = { it?.text().orEmpty() },
                onSelectItem = onSeasonChange,
                maxLines = 1,
                wrapWidth = true,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            )

            val screenWidth = LocalWindowConfiguration.current.screenWidthDp
            val minWidth = screenWidth / 2 - 32.dp
            val leadingIcon = @Composable {
                IconButton(onClick = {
                    data.seasonYear.toIntOrNull()?.let {
                        onSeasonYearChange((it - 1).toString())
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.RemoveCircleOutline,
                        contentDescription = stringResource(
                            Res.string.anime_media_filter_airing_date_season_year_decrement_content_description
                        ),
                    )
                }
            }

            MinWidthTextField(
                value = data.seasonYear,
                onValueChange = onSeasonYearChange,
                label = { Text(stringResource(Res.string.anime_media_filter_airing_date_season_year)) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    autoCorrectEnabled = KeyboardOptions.Default.autoCorrectEnabled,
                ),
                leadingIcon = leadingIcon.takeIf { data.seasonYear.isNotBlank() },
                trailingIcon = {
                    Row {
                        val isYearBlank = data.seasonYear.isBlank()
                        if (!isYearBlank) {
                            IconButton(onClick = {
                                data.seasonYear.toIntOrNull()?.let {
                                    onSeasonYearChange((it + 1).toString())
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.AddCircleOutline,
                                    contentDescription = stringResource(
                                        Res.string.anime_media_filter_airing_date_season_year_increment_content_description
                                    ),
                                )
                            }
                        }

                        IconButton(onClick = {
                            if (isYearBlank) {
                                onSeasonYearChange(
                                    Clock.System.now()
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .year.toString()
                                )
                            } else {
                                onSeasonYearChange("")
                            }
                        }) {
                            Icon(
                                imageVector = if (isYearBlank) {
                                    Icons.Filled.CalendarToday
                                } else {
                                    Icons.Filled.Clear
                                },
                                contentDescription = stringResource(
                                    if (isYearBlank) {
                                        Res.string.anime_media_filter_airing_date_season_year_today_content_description
                                    } else {
                                        Res.string.anime_media_filter_airing_date_season_year_clear_content_description
                                    },
                                ),
                            )
                        }
                    }
                },
                minWidth = minWidth,
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .widthIn(max = (screenWidth - 160.dp).coerceAtLeast(minWidth))
                    .padding(horizontal = 8.dp)
            )
        }

        IconButton(
            onClick = {
                val nextSeason = data.nextSeason()
                onSeasonChange(nextSeason.first)
                onSeasonYearChange(nextSeason.second.toString())
            },
            modifier = Modifier.fillMaxHeight()
        ) {
            Icon(
                imageVector = Icons.Default.AddCircleOutline,
                contentDescription = stringResource(
                    Res.string.anime_media_filter_airing_date_season_increment_content_description
                )
            )
        }
    }
}

@Preview
@Composable
fun AiringDateBasicSectionPreview() {
    AiringDateBasicSection(data = AiringDate.Basic(), onSeasonChange = {}, onSeasonYearChange = {})
}

@Composable
fun AiringDateAdvancedSection(
    data: AiringDate.Advanced,
    onRequestDatePicker: (forStart: Boolean) -> Unit,
    onDateChange: (start: Boolean, Long?) -> Unit,
) {
    StartEndDateRow(
        startDate = data.startDate,
        endDate = data.endDate,
        onRequestDatePicker = onRequestDatePicker,
        onDateChange = onDateChange,
    )
}
