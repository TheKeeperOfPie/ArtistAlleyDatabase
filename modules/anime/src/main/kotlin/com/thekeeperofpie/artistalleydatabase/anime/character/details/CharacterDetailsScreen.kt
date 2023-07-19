package com.thekeeperofpie.artistalleydatabase.anime.character.details

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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.AuthedUserQuery
import com.anilist.CharacterDetailsQuery.Data.Character
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeader
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.mediaListSection
import com.thekeeperofpie.artistalleydatabase.anime.staff.DetailsStaff
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsLoadingOrError
import com.thekeeperofpie.artistalleydatabase.anime.ui.descriptionSection
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSubsectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.InfoText
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdgeBottom
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.twoColumnInfoText

@OptIn(ExperimentalMaterial3Api::class)
object CharacterDetailsScreen {

    private val SCREEN_KEY = AnimeNavDestinations.CHARACTER_DETAILS.id
    private const val MEDIA_ABOVE_FOLD = 3

    @Composable
    operator fun invoke(
        viewModel: AnimeCharacterDetailsViewModel = hiltViewModel(),
        upIconOption: UpIconOption?,
        headerValues: CharacterHeaderValues,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)

        var characterImageWidthToHeightRatio by remember {
            mutableFloatStateOf(headerValues.imageWidthToHeightRatio)
        }

        val viewer by viewModel.viewer.collectAsState()
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            screenKey = AnimeNavDestinations.SEARCH.id,
            viewModel = editViewModel,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            topBar = {
                CollapsingToolbar(
                    maxHeight = 356.dp,
                    pinnedHeight = 120.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    CharacterHeader(
                        screenKey = SCREEN_KEY,
                        upIconOption = upIconOption,
                        characterId = viewModel.characterId,
                        progress = it,
                        headerValues = headerValues,
                        colorCalculationState = colorCalculationState,
                        onImageWidthToHeightRatioAvailable = {
                            characterImageWidthToHeightRatio = it
                        },
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
                            editViewModel = editViewModel,
                            headerValues = headerValues,
                            entry = it,
                            viewer = viewer,
                            characterImageWidthToHeightRatio = { characterImageWidthToHeightRatio },
                            expandedState = expandedState,
                            navigationCallback = navigationCallback,
                            colorCalculationState = colorCalculationState,
                        )
                    }
                }
            }
        }
    }

    private fun LazyListScope.content(
        viewModel: AnimeCharacterDetailsViewModel,
        editViewModel: MediaEditViewModel,
        headerValues: CharacterHeaderValues,
        entry: Entry,
        viewer: AuthedUserQuery.Data.Viewer?,
        characterImageWidthToHeightRatio: () -> Float,
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
            viewer = viewer,
            entry = entry,
            headerValues = headerValues,
            characterImageWidthToHeightRatio = characterImageWidthToHeightRatio,
            expanded = expandedState::media,
            onExpandedChange = { expandedState.media = it },
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            onClickListEdit = { editViewModel.initialize(it.media) },
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
        viewer: AuthedUserQuery.Data.Viewer?,
        entry: Entry,
        headerValues: CharacterHeaderValues,
        characterImageWidthToHeightRatio: () -> Float,
        expanded: () -> Boolean,
        onExpandedChange: (Boolean) -> Unit,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
        onClickListEdit: (AnimeMediaListRow.Entry<*>) -> Unit,
        onLongClick: (AnimeMediaListRow.Entry<*>) -> Unit,
        onTagLongClick: (String) -> Unit,
    ) {
        mediaListSection(
            screenKey = AnimeNavDestinations.CHARACTER_DETAILS.id,
            viewer = viewer,
            titleRes = R.string.anime_character_details_media_label,
            values = entry.media,
            valueToEntry = { it },
            aboveFold = MEDIA_ABOVE_FOLD,
            hasMoreValues = entry.mediaHasMore,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            onClickListEdit = onClickListEdit,
            onLongClick = onLongClick,
            onTagLongClick = onTagLongClick,
            onClickViewAll = {
                entry.let {
                    navigationCallback.onCharacterMediasClick(
                        character = it.character,
                        imageWidthToHeightRatio = characterImageWidthToHeightRatio(),
                        color = headerValues.color(colorCalculationState),
                    )
                }
            },
            viewAllContentDescriptionTextRes = R.string.anime_character_details_view_all_content_description,
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

        val mediaHasMore = character.media?.pageInfo?.hasNextPage == true
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
