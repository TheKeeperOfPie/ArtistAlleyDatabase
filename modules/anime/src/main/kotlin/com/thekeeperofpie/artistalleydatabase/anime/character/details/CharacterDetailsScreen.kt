package com.thekeeperofpie.artistalleydatabase.anime.character.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.CharacterDetailsQuery.Data.Character
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.mediaListSection
import com.thekeeperofpie.artistalleydatabase.anime.staff.DetailsStaff
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsLoadingOrError
import com.thekeeperofpie.artistalleydatabase.anime.ui.descriptionSection
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSubsectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.InfoText
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdgeBottom
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.twoColumnInfoText
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalMaterial3Api::class)
object CharacterDetailsScreen {

    private const val MEDIA_ABOVE_FOLD = 3

    @Composable
    operator fun invoke(
        viewModel: AnimeCharacterDetailsViewModel = hiltViewModel(),
        coverImage: @Composable () -> String? = { null },
        coverImageWidthToHeightRatio: Float = 1f,
        title: @Composable () -> String = { "First Last" },
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        Scaffold(
            topBar = {
                CollapsingToolbar(
                    maxHeight = 356.dp,
                    pinnedHeight = 120.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    Header(
                        characterId = viewModel.characterId,
                        progress = it,
                        color = { viewModel.colorMap[viewModel.characterId]?.first },
                        coverImage = coverImage,
                        coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
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
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { scaffoldPadding ->
            val expandedState = rememberExpandedState()
            val entry = viewModel.entry
            Crossfade(targetState = entry, label = "Character details crossfade") {
                if (it == null) {
                    DetailsLoadingOrError(
                        loading = viewModel.loading,
                        errorResource = { viewModel.errorResource },
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(scaffoldPadding)
                    ) {
                        content(
                            viewModel = viewModel,
                            entry = it,
                            expandedState = expandedState,
                            navigationCallback = navigationCallback,
                            colorCalculationState = colorCalculationState,
                        )
                    }
                }
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
        titleText: @Composable () -> String,
        subtitleText: @Composable () -> String?,
        colorCalculationState: ColorCalculationState,
    ) {
        CoverAndBannerHeader(
            screenKey = AnimeNavDestinations.CHARACTER_DETAILS.id,
            entryId = EntryId("anime_character", characterId),
            progress = progress,
            color = color,
            coverImage = coverImage,
            coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
            coverImageOnSuccess = {
                ComposeColorUtils.calculatePalette(characterId, it, colorCalculationState)
            }
        ) {
            AutoResizeHeightText(
                text = titleText(),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            )

            val subtitleText = subtitleText()
            AnimatedVisibility(subtitleText != null, label = "Character details subtitle text") {
                if (subtitleText != null) {
                    Text(
                        text = subtitleText,
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
    }

    private fun LazyListScope.content(
        viewModel: AnimeCharacterDetailsViewModel,
        entry: Entry,
        expandedState: ExpandedState,
        navigationCallback: AnimeNavigator.NavigationCallback,
        colorCalculationState: ColorCalculationState,
    ) {
        descriptionSection(
            htmlText = entry.character.description,
            expanded = expandedState::description,
            onExpandedChange = { expandedState.description = it },
        )

        staffSection(
            screenKey = AnimeNavDestinations.CHARACTER_DETAILS.id,
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
            onLongClick = viewModel::onMediaLongClick,
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
                ) || contentShown

                entry.character.favourites?.let {
                    InfoText(
                        label = stringResource(R.string.anime_character_details_favorites_label),
                        body = it.toString(),
                        showDividerAbove = contentShown,
                    )
                    contentShown = true
                }

                val alternativeNames = entry.character.name?.alternative?.filterNotNull().orEmpty()
                val alternativeNamesSpoiler =
                    entry.character.name?.alternativeSpoiler?.filterNotNull().orEmpty()
                if (alternativeNames.isNotEmpty() || alternativeNamesSpoiler.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    var hidden by remember { mutableStateOf(true) }

                    Box {
                        Column(
                            modifier = Modifier
                                .wrapContentHeight()
                                .heightIn(max = if (expanded) Dp.Unspecified else 120.dp)
                                .clickable { expanded = !expanded }
                                .fadingEdgeBottom(show = !expanded)
                        ) {
                            if (contentShown) {
                                Divider()
                            }

                            DetailsSubsectionHeader(
                                stringResource(R.string.anime_character_details_alternative_names_label)
                            )

                            alternativeNames.forEachIndexed { index, name ->
                                if (index != 0) {
                                    Divider(modifier = Modifier.padding(start = 16.dp))
                                }

                                val bottomPadding = if (index == alternativeNames.size - 1
                                    && alternativeNamesSpoiler.isEmpty()
                                ) {
                                    12.dp
                                } else {
                                    8.dp
                                }

                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 8.dp,
                                            bottom = bottomPadding,
                                        )
                                )
                            }

                            if (alternativeNamesSpoiler.isNotEmpty()) {

                                if (alternativeNames.isNotEmpty()) {
                                    Divider(modifier = Modifier.padding(start = 16.dp))
                                }
                                if (hidden) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clickable { hidden = false }
                                            .padding(
                                                start = 16.dp,
                                                end = 16.dp,
                                                top = 8.dp,
                                                bottom = 12.dp,
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Warning,
                                            contentDescription = stringResource(
                                                R.string.anime_character_details_alternative_names_spoiler_warning_content_description
                                            ),
                                        )

                                        Text(
                                            text = stringResource(R.string.anime_character_details_alternative_names_spoiler_warning),
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier
                                                .align(Alignment.CenterVertically)
                                                .weight(1f)
                                        )
                                    }
                                } else {
                                    alternativeNamesSpoiler.forEachIndexed { index, name ->
                                        val bottomPadding =
                                            if (index == alternativeNamesSpoiler.size - 1) {
                                                12.dp
                                            } else {
                                                8.dp
                                            }

                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    start = 16.dp,
                                                    end = 16.dp,
                                                    top = 8.dp,
                                                    bottom = bottomPadding,
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        TrailingDropdownIconButton(
                            expanded = expanded,
                            contentDescription = stringResource(R.string.anime_character_details_alternative_names_expand_content_description),
                            onClick = { expanded = !expanded },
                            modifier = Modifier.align(Alignment.TopEnd),
                        )
                    }
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
        onLongClick: (AnimeMediaListRow.Entry<*>) -> Unit,
        onTagLongClick: (String) -> Unit,
    ) {
        mediaListSection(
            screenKey = AnimeNavDestinations.CHARACTER_DETAILS.id,
            titleRes = R.string.anime_character_details_media_label,
            values = entry.media,
            valueToEntry = { it },
            aboveFold = MEDIA_ABOVE_FOLD,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            onLongClick = onLongClick,
            onTagLongClick = onTagLongClick,
        )
    }

    data class Entry(val character: Character, val media: List<AnimeMediaListRow.Entry<*>>) {
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
                            staff = it,
                        )
                    }
                    .orEmpty()
            }.orEmpty()
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
