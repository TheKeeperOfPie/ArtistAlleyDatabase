package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.CharacterDetailsQuery.Data.Character
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.mediaListSection
import com.thekeeperofpie.artistalleydatabase.anime.staff.DetailsStaff
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.InfoText
import com.thekeeperofpie.artistalleydatabase.anime.ui.descriptionSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.twoColumnInfoText
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

@OptIn(ExperimentalMaterial3Api::class)
object CharacterDetailsScreen {

    private const val MEDIA_ABOVE_FOLD = 3

    @Composable
    operator fun invoke(
        viewModel: AnimeCharacterDetailsViewModel = hiltViewModel(),
        coverImage: @Composable () -> String? = { null },
        coverImageWidthToHeightRatio: Float = 1f,
        title: @Composable () -> String = { "First Last" },
        bannerImage: @Composable () -> String? = { null },
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        Scaffold(
            topBar = {
                CollapsingToolbar(
                    maxHeight = 356.dp,
                    pinnedHeight = 180.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    Header(
                        characterId = viewModel.characterId,
                        progress = it,
                        color = { viewModel.colorMap[viewModel.characterId]?.first },
                        coverImage = coverImage,
                        coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
                        bannerImage = bannerImage,
                        titleText = title,
                        subtitleText = {
                            viewModel.entry?.character?.name?.run {
                                if (native != userPreferred) {
                                    native
                                } else if (full != userPreferred) {
                                    full
                                } else {
                                    null
                                }
                            }
                        },
                        colorCalculationState = colorCalculationState,
                    )
                }
            },
            snackbarHost = {
                val errorRes = viewModel.errorResource
                if (errorRes != null) {
                    SnackbarErrorText(
                        errorRes.first,
                        errorRes.second,
                        onErrorDismiss = { viewModel.errorResource = null },
                    )
                }
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            val expandedState = rememberExpandedState()
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                content(
                    viewModel = viewModel,
                    expandedState = expandedState,
                    navigationCallback = navigationCallback,
                    colorCalculationState = colorCalculationState,
                )
            }
        }
    }

    @Composable
    private fun Header(
        characterId: String,
        progress: Float,
        color: () -> Color?,
        coverImage: @Composable () -> String?,
        coverImageWidthToHeightRatio: Float,
        bannerImage: @Composable () -> String?,
        titleText: @Composable () -> String,
        subtitleText: @Composable () -> String?,
        colorCalculationState: ColorCalculationState,
    ) {
        CoverAndBannerHeader(
            pinnedHeight = 180.dp,
            progress = progress,
            color = color,
            coverImage = coverImage,
            coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
            bannerImage = bannerImage,
            coverImageOnSuccess = {
                ComposeColorUtils.calculatePalette(characterId, it, colorCalculationState)
            }
        ) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AutoResizeHeightText(
                    text = titleText(),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                )
            }

            subtitleText()?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(Alignment.Bottom)
                )
            }
        }
    }

    private fun LazyListScope.content(
        viewModel: AnimeCharacterDetailsViewModel,
        expandedState: ExpandedState,
        navigationCallback: AnimeNavigator.NavigationCallback,
        colorCalculationState: ColorCalculationState,
    ) {
        val entry = viewModel.entry
        if (entry == null) {
            if (viewModel.loading) {
                item {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp)
                        )
                    }
                }
            } else {
                item {
                    val errorRes = viewModel.errorResource
                    AnimeMediaListScreen.Error(
                        errorTextRes = errorRes?.first,
                        exception = errorRes?.second,
                    )
                }
            }
            return
        }

        descriptionSection(
            titleTextRes = R.string.anime_character_details_description_label,
            htmlText = entry.character.description,
            expanded = expandedState::description,
            onExpandedChanged = { expandedState.description = it },
        )

        staffSection(
            titleRes = R.string.anime_character_details_voice_actors_label,
            staff = entry.voiceActors,
            onStaffClick = navigationCallback::onStaffClick,
            onStaffLongClick = navigationCallback::onStaffLongClick,
            colorCalculationState = colorCalculationState,
            roleLines = 1,
        )

        mediaSection(
            entry = entry,
            expanded = expandedState::media,
            onExpandedChange = { expandedState.media = it },
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            onTagLongClick = { /* TODO */ },
        )

        infoSection(entry = entry)
    }

    private fun LazyListScope.infoSection(entry: Entry) {
        item {
            DetailsSectionHeader(stringResource(R.string.anime_media_details_information_label))
        }

        item {
            ElevatedCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .animateContentSize(),
            ) {
                var contentShown = twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_character_details_age_label),
                    bodyOne = entry.character.age,
                    labelTwo = stringResource(R.string.anime_character_details_date_of_birth_label),
                    bodyTwo = entry.character.dateOfBirth?.let {
                        MediaUtils.formatDateTime(LocalContext.current, it.year, it.month, it.day)
                    },
                    showDividerAbove = false,
                )

                contentShown = twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_character_details_gender_label),
                    bodyOne = entry.character.gender,
                    labelTwo = stringResource(R.string.anime_character_details_blood_type_label),
                    bodyTwo = entry.character.bloodType,
                    showDividerAbove = contentShown,
                )

                entry.character.favourites?.let {
                    InfoText(
                        label = stringResource(R.string.anime_character_details_favorites_label),
                        body = it.toString(),
                        showDividerAbove = contentShown,
                    )
                }
            }
        }
    }

    private fun LazyListScope.mediaSection(
        entry: Entry,
        expanded: () -> Boolean,
        onExpandedChange: (Boolean) -> Unit,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
        onTagLongClick: (String) -> Unit,
    ) {
        mediaListSection(
            titleRes = R.string.anime_character_details_media_label,
            values = entry.media,
            valueToEntry = { it },
            aboveFold = MEDIA_ABOVE_FOLD,
            expanded = expanded,
            onExpandedToggled = onExpandedChange,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            onTagLongClick = onTagLongClick,
        )
    }

    data class Entry(val character: Character) {
        val voiceActors = character.media?.edges?.filterNotNull()
            ?.flatMap {
                it.voiceActorRoles?.filterNotNull()
                    ?.mapNotNull { it.voiceActor }
                    ?.map {
                        DetailsStaff(
                            id = it.id.toString(),
                            name = it.name?.userPreferred,
                            image = it.image?.large,
                            role = it.languageV2,
                        )
                    }
                    .orEmpty()
            }.orEmpty()
            .distinctBy { it.id }

        val media = character.media?.edges
            ?.mapNotNull { it?.node?.let { AnimeMediaListRow.MediaEntry(it) } }
            .orEmpty()
            .distinctBy { it.id }
    }

    @Composable
    private fun rememberExpandedState() = rememberSaveable(saver = listSaver(
        save = {
            listOf(
                it.description,
                it.media,
            )
        },
        restore = {
            ExpandedState(
                description = it[0],
                media = it[1],
            )
        }
    )) {
        ExpandedState()
    }

    private class ExpandedState(
        description: Boolean = false,
        media: Boolean = false,
    ) {
        var description by mutableStateOf(description)
        var media by mutableStateOf(media)
    }
}
