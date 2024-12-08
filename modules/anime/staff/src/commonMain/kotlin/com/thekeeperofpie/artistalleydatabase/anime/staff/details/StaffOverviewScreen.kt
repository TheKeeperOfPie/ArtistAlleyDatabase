package com.thekeeperofpie.artistalleydatabase.anime.staff.details

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.staff.generated.resources.Res
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_age_label
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_blood_type_label
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_characters_label
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_date_of_birth_label
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_date_of_death_label
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_favorites_label
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_gender_label
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_home_town_label
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_information_label
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_primary_occupations_expand_content_description
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_primary_occupations_label
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_view_all_content_description
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_years_active_beginning
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_years_active_beginning_and_end
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_years_active_label
import com.anilist.data.StaffDetailsQuery
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDestinations
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.DescriptionSection
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.expandableListInfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.twoColumnInfoText
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object StaffOverviewScreen {

    @Composable
    operator fun invoke(
        entry: Entry,
        coverImageState: CoilImageState,
        expandedState: StaffExpandedState,
        favorite: () -> Boolean?,
        charactersSection: LazyGridScope.(
            titleRes: StringResource,
            viewAllRoute: () -> NavDestination,
            viewAllContentDescriptionTextRes: StringResource,
        ) -> Unit,
    ) {
        val staffName = entry.staff.name?.primaryName()
        val staffSubtitle = entry.staff.name?.subtitleName()
        LazyVerticalGrid(
            columns = GridUtils.standardWidthAdaptiveCells,
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (entry.description != null) {
                item(
                    key = "descriptionSection",
                    span = GridUtils.maxSpanFunction,
                    contentType = "descriptionSection",
                ) {
                    DescriptionSection(
                        markdownText = entry.description,
                        expanded = expandedState::description,
                        onExpandedChange = { expandedState.description = it },
                    )
                }
            }

            charactersSection(
                Res.string.anime_staff_details_characters_label,
                {
                    StaffDestinations.StaffCharacters(
                        staffId = entry.staff.id.toString(),
                        sharedTransitionKey = null,
                        headerParams = StaffHeaderParams(
                            name = staffName,
                            subtitle = staffSubtitle,
                            coverImage = coverImageState.toImageState(),
                            favorite = favorite(),
                        )
                    )
                },
                Res.string.anime_staff_details_view_all_content_description,
            )

            infoSection(entry = entry)
        }
    }

    private fun LazyGridScope.infoSection(entry: Entry) {
        item(
            key = "infoHeader",
            span = GridUtils.maxSpanFunction,
            contentType = "detailsSectionHeader",
        ) {
            DetailsSectionHeader(stringResource(Res.string.anime_staff_details_information_label))
        }

        item(key = "infoSection", span = GridUtils.maxSpanFunction, contentType = "infoSection") {
            ElevatedCard(
                modifier = Modifier
                    .animateContentSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 2.dp)
            ) {
                var contentShown = twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_staff_details_age_label),
                    bodyOne = entry.staff.age?.toString(),
                    labelTwo = stringResource(Res.string.anime_staff_details_gender_label),
                    bodyTwo = entry.staff.gender,
                    showDividerAbove = false,
                )

                val dateTimeFormatter = LocalDateTimeFormatter.current
                contentShown = twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_staff_details_date_of_birth_label),
                    bodyOne = entry.staff.dateOfBirth?.let {
                        remember(it) { dateTimeFormatter.formatDateTime(it.year, it.month, it.day) }
                    },
                    labelTwo = stringResource(Res.string.anime_staff_details_date_of_death_label),
                    bodyTwo = entry.staff.dateOfDeath?.let {
                        remember(it) { dateTimeFormatter.formatDateTime(it.year, it.month, it.day) }
                    },
                    showDividerAbove = contentShown,
                ) || contentShown

                contentShown = twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_staff_details_home_town_label),
                    bodyOne = entry.staff.homeTown,
                    labelTwo = stringResource(Res.string.anime_staff_details_blood_type_label),
                    bodyTwo = entry.staff.bloodType,
                    showDividerAbove = contentShown,
                ) || contentShown

                val yearsActive = entry.staff.yearsActive?.filterNotNull().orEmpty()
                val yearsActiveText = when (yearsActive.size) {
                    1 -> stringResource(
                        Res.string.anime_staff_details_years_active_beginning,
                        yearsActive[0]
                    )
                    2 -> stringResource(
                        Res.string.anime_staff_details_years_active_beginning_and_end,
                        yearsActive[0],
                        yearsActive[1]
                    )
                    else -> null
                }

                contentShown = twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_staff_details_years_active_label),
                    bodyOne = yearsActiveText,
                    labelTwo = stringResource(Res.string.anime_staff_details_favorites_label),
                    bodyTwo = (entry.staff.favourites ?: 0).toString(),
                    showDividerAbove = contentShown,
                ) || contentShown

                expandableListInfoText(
                    labelTextRes = Res.string.anime_staff_details_primary_occupations_label,
                    contentDescriptionTextRes = Res.string.anime_staff_details_primary_occupations_expand_content_description,
                    values = entry.staff.primaryOccupations?.filterNotNull().orEmpty(),
                    valueToText = { it },
                    showDividerAbove = contentShown,
                )
            }
        }
    }

    interface Entry {
        val staff: StaffDetailsQuery.Data.Staff
        val description: MarkdownText?
    }
}
