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
import androidx.paging.compose.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.charactersSection
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.DescriptionSection
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.expandableListInfoText
import com.thekeeperofpie.artistalleydatabase.compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.twoColumnInfoText

object StaffOverviewScreen {

    @Composable
    operator fun invoke(
        viewModel: StaffDetailsViewModel,
        entry: StaffDetailsScreen.Entry,
        coverImageState: CoilImageState,
        expandedState: StaffDetailsScreen.ExpandedState,
    ) {
        val characters = viewModel.characters.collectAsLazyPagingItems()
        val staffName = entry.staff.name?.primaryName()
        val staffSubtitle = entry.staff.name?.subtitleName()
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (!entry.description?.value.isNullOrEmpty()) {
                item("descriptionSection", "descriptionSection") {
                    DescriptionSection(
                        markdownText = entry.description,
                        expanded = expandedState::description,
                        onExpandedChange = { expandedState.description = it },
                    )
                }
            }

            charactersSection(
                screenKey = AnimeNavDestinations.STAFF_DETAILS.id,
                titleRes = R.string.anime_staff_details_characters_label,
                characters = characters,
                onClickViewAll = {
                    it.navigate(
                        AnimeDestinations.StaffCharacters(
                            staffId = entry.staff.id.toString(),
                            sharedTransitionKey = null,
                            headerParams = StaffHeaderParams(
                                name = staffName,
                                subtitle = staffSubtitle,
                                coverImage = coverImageState.toImageState(),
                                favorite = viewModel.favoritesToggleHelper.favorite,
                            )
                        )
                    )
                },
                viewAllContentDescriptionTextRes = R.string.anime_staff_details_view_all_content_description,
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
                    .animateContentSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 2.dp)
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

                contentShown = twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_staff_details_years_active_label),
                    bodyOne = yearsActiveText,
                    labelTwo = stringResource(R.string.anime_staff_details_favorites_label),
                    bodyTwo = (entry.staff.favourites ?: 0).toString(),
                    showDividerAbove = contentShown,
                ) || contentShown

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
