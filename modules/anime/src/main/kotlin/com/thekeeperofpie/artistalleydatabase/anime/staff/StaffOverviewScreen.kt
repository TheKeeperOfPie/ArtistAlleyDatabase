package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.charactersSection
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.ui.descriptionSection
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.InfoText
import com.thekeeperofpie.artistalleydatabase.compose.expandableListInfoText
import com.thekeeperofpie.artistalleydatabase.compose.twoColumnInfoText

object StaffOverviewScreen {

    @Composable
    operator fun invoke(
        entry: StaffDetailsScreen.Entry,
        staffImageWidthToHeightRatio: () -> Float,
        colorCalculationState: ColorCalculationState,
        expandedState: StaffDetailsScreen.ExpandedState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            descriptionSection(
                htmlText = entry.staff.description,
                expanded = expandedState::description,
                onExpandedChange = { expandedState.description = it },
            )

            charactersSection(
                screenKey = AnimeNavDestinations.STAFF_DETAILS.id,
                titleRes = R.string.anime_staff_details_characters_label,
                characters = entry.characters,
                onCharacterClick = navigationCallback::onCharacterClick,
                onCharacterLongClick = navigationCallback::onCharacterLongClick,
                onStaffClick = navigationCallback::onStaffClick,
                onClickViewAll = {
                    navigationCallback.onStaffCharactersClick(
                        entry.staff,
                        staffImageWidthToHeightRatio(),
                        colorCalculationState.getColors(entry.staff.id.toString()).first
                    )
                },
                viewAllContentDescriptionTextRes = R.string.anime_staff_details_view_all_content_description,
                colorCalculationState = colorCalculationState,
            )

            infoSection(entry = entry)
        }
    }

    private fun LazyListScope.infoSection(entry: StaffDetailsScreen.Entry) {
        item {
            DetailsSectionHeader(stringResource(R.string.anime_staff_details_information_label))
        }

        item {
            ElevatedCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .animateContentSize(),
            ) {
                var contentShown = twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_staff_details_age_label),
                    bodyOne = entry.staff.age?.toString(),
                    labelTwo = stringResource(R.string.anime_staff_details_gender_label),
                    bodyTwo = entry.staff.gender,
                    showDividerAbove = false,
                )

                contentShown = twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_staff_details_date_of_birth_label),
                    bodyOne = entry.staff.dateOfBirth?.let {
                        MediaUtils.formatDateTime(LocalContext.current, it.year, it.month, it.day)
                    },
                    labelTwo = stringResource(R.string.anime_staff_details_date_of_death_label),
                    bodyTwo = entry.staff.dateOfDeath?.let {
                        MediaUtils.formatDateTime(LocalContext.current, it.year, it.month, it.day)
                    },
                    showDividerAbove = contentShown,
                ) || contentShown

                contentShown = twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_staff_details_home_town_label),
                    bodyOne = entry.staff.homeTown,
                    labelTwo = stringResource(R.string.anime_staff_details_blood_type_label),
                    bodyTwo = entry.staff.bloodType,
                    showDividerAbove = contentShown,
                ) || contentShown

                val yearsActive = entry.staff.yearsActive?.filterNotNull().orEmpty()
                val yearsActiveText = when (yearsActive.size) {
                    1 -> stringResource(
                        R.string.anime_staff_details_years_active_beginning,
                        yearsActive[0]
                    )
                    2 -> stringResource(
                        R.string.anime_staff_details_years_active_beginning_and_end,
                        yearsActive[0],
                        yearsActive[1]
                    )
                    else -> null
                }
                if (yearsActiveText != null) {
                    InfoText(
                        label = stringResource(R.string.anime_staff_details_years_active_label),
                        body = yearsActiveText,
                        showDividerAbove = contentShown,
                    )
                    contentShown = true
                }

                expandableListInfoText(
                    labelTextRes = R.string.anime_staff_details_primary_occupations_label,
                    contentDescriptionTextRes = R.string.anime_staff_details_primary_occupations_expand_content_description,
                    values = entry.staff.primaryOccupations?.filterNotNull().orEmpty(),
                    valueToText = { it },
                    showDividerAbove = contentShown,
                )
            }
        }
    }
}
