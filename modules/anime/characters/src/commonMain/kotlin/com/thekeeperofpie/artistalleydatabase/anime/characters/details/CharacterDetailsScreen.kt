package com.thekeeperofpie.artistalleydatabase.anime.characters.details

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import artistalleydatabase.modules.anime.characters.generated.resources.Res
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_age_label
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_alternative_names_expand_content_description
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_alternative_names_label
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_alternative_names_spoiler_warning
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_alternative_names_spoiler_warning_content_description
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_blood_type_label
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_date_of_birth_label
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_favorites_label
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_gender_label
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_information_label
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_media_label
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_view_all_content_description
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_voice_actors_label
import com.anilist.data.CharacterDetailsQuery.Data.Character
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterDestinations
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterHeader
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffDetails
import com.thekeeperofpie.artistalleydatabase.anime.ui.DescriptionSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSection
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSubsectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.InfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeBottom
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import com.thekeeperofpie.artistalleydatabase.utils_compose.twoColumnInfoText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
object CharacterDetailsScreen {

    private const val MEDIA_ABOVE_FOLD = 3

    @Composable
    operator fun <MediaEntry> invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        characterId: String,
        headerValues: CharacterHeaderValues,
        viewer: () -> AniListViewer?,
        upIconOption: UpIconOption?,
        onRefresh: () -> Unit,
        entry: () -> LoadingResult<Entry<MediaEntry>>,
        onFavoriteChanged: (Boolean) -> Unit,
        voiceActorsDeferred: () -> MutableStateFlow<PagingData<StaffDetails>>,
        staffSection: LazyGridScope.(
            titleRes: StringResource?,
            LazyPagingItems<StaffDetails>,
            roleLines: Int,
        ) -> Unit,
        mediaRow: @Composable (
            AnimeCharacterDetailsViewModel.Entry<MediaEntry>,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
        )

        val coverImageState = rememberCoilImageState(headerValues.coverImage)

        val entry = entry()
        var loadingThresholdPassed by remember { mutableStateOf(false) }
        val refreshing = entry.loading && loadingThresholdPassed
        val pullRefreshState = rememberPullRefreshState(
            refreshing = refreshing,
            onRefresh = onRefresh,
        )
        LaunchedEffect(pullRefreshState) {
            delay(1.seconds)
            loadingThresholdPassed = true
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(state = pullRefreshState)
        ) {
            mediaEditBottomSheetScaffold { padding, onClickListEdit ->
                Scaffold(
                    modifier = Modifier.padding(padding)
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        CollapsingToolbar(
                            maxHeight = 356.dp,
                            pinnedHeight = 120.dp,
                            scrollBehavior = scrollBehavior,
                        ) {
                            CharacterHeader(
                                viewer = viewer(),
                                upIconOption = upIconOption,
                                characterId = characterId,
                                progress = it,
                                headerValues = headerValues,
                                sharedTransitionKey = SharedTransitionKey.makeKeyForId(characterId),
                                coverImageState = coverImageState,
                                onFavoriteChanged = onFavoriteChanged,
                            )
                        }
                    }
                ) { scaffoldPadding ->
                    val expandedState = rememberExpandedState()
                    val finalError = entry.result == null && !entry.loading
                    Crossfade(targetState = finalError, label = "Character details crossfade") {
                        if (it) {
                            VerticalList.ErrorContent(
                                errorText = entry.error?.messageText().orEmpty(),
                                exception = entry.error?.throwable,
                            )
                        } else if (entry.result != null) {
                            val voiceActorsInitial = (entry.result?.voiceActorsInitial
                                ?: MutableStateFlow(PagingData.empty())).collectAsLazyPagingItems()
                            val voiceActorsDeferred =
                                voiceActorsDeferred().collectAsLazyPagingItems()
                            val voiceActors =
                                voiceActorsDeferred.takeIf { it.itemCount > 0 }
                                    ?: voiceActorsInitial
                            val character = entry.result?.character
                            val characterName = character?.name?.primaryName()
                            val characterSubtitle = character?.name?.subtitleName()
                            val navigationController = LocalNavigationController.current
                            val headerSharedTransitionKey =
                                SharedTransitionKey.makeKeyForId(characterId)
                            LazyVerticalGrid(
                                columns = GridUtils.standardWidthAdaptiveCells,
                                contentPadding = PaddingValues(bottom = 16.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(scaffoldPadding)
                            ) {
                                content(
                                    headerValues = headerValues,
                                    headerSharedTransitionKey = headerSharedTransitionKey,
                                    entry = entry.result!!,
                                    characterName = characterName,
                                    characterSubtitle = characterSubtitle,
                                    voiceActors = voiceActors,
                                    coverImageState = coverImageState,
                                    expandedState = expandedState,
                                    navigationController = navigationController,
                                    staffSection = staffSection,
                                    mediaRow = { item, modifier ->
                                        mediaRow(item, onClickListEdit, modifier)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    private fun <MediaEntry> LazyGridScope.content(
        headerValues: CharacterHeaderValues,
        headerSharedTransitionKey: SharedTransitionKey?,
        entry: Entry<MediaEntry>,
        characterName: String?,
        characterSubtitle: String?,
        voiceActors: LazyPagingItems<StaffDetails>,
        coverImageState: CoilImageState,
        expandedState: ExpandedState,
        navigationController: NavigationController,
        staffSection: LazyGridScope.(
            titleRes: StringResource?,
            LazyPagingItems<StaffDetails>,
            roleLines: Int,
        ) -> Unit,
        mediaRow: @Composable (AnimeCharacterDetailsViewModel.Entry<MediaEntry>, Modifier) -> Unit,
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

        staffSection(
            Res.string.anime_character_details_voice_actors_label,
            voiceActors,
            1,
        )

        mediaSection(
            entry = entry,
            characterName = characterName,
            characterSubtitle = characterSubtitle,
            headerValues = headerValues,
            headerSharedTransitionKey = headerSharedTransitionKey,
            coverImageState = coverImageState,
            expanded = expandedState::media,
            onExpandedChange = { expandedState.media = it },
            navigationController = navigationController,
            mediaRow = mediaRow,
        )

        infoSection(entry = entry)
    }

    private fun <MediaEntry> LazyGridScope.infoSection(entry: Entry<MediaEntry>) {
        item(
            key = "infoHeader",
            span = GridUtils.maxSpanFunction,
            contentType = "detailsSectionHeader",
        ) {
            DetailsSectionHeader(stringResource(Res.string.anime_character_details_information_label))
        }

        item(key = "infoSection", span = GridUtils.maxSpanFunction, contentType = "infoSection") {
            ElevatedCard(
                modifier = Modifier
                    .animateContentSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 2.dp)
            ) {
                val dateTimeFormatter = LocalDateTimeFormatter.current
                var contentShown = twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_character_details_age_label),
                    bodyOne = entry.character.age,
                    labelTwo = stringResource(Res.string.anime_character_details_date_of_birth_label),
                    bodyTwo = entry.character.dateOfBirth?.let {
                        dateTimeFormatter.formatDateTime(it.year, it.month, it.day)
                    },
                    showDividerAbove = false,
                )

                contentShown = twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_character_details_gender_label),
                    bodyOne = entry.character.gender,
                    labelTwo = stringResource(Res.string.anime_character_details_blood_type_label),
                    bodyTwo = entry.character.bloodType,
                    showDividerAbove = contentShown,
                ) || contentShown

                entry.character.favourites?.let {
                    InfoText(
                        label = stringResource(Res.string.anime_character_details_favorites_label),
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
                                HorizontalDivider()
                            }

                            DetailsSubsectionHeader(
                                stringResource(Res.string.anime_character_details_alternative_names_label)
                            )

                            alternativeNames.forEachIndexed { index, name ->
                                if (index != 0) {
                                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
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
                                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
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
                                                Res.string.anime_character_details_alternative_names_spoiler_warning_content_description
                                            ),
                                        )

                                        Text(
                                            text = stringResource(Res.string.anime_character_details_alternative_names_spoiler_warning),
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
                            contentDescription = stringResource(Res.string.anime_character_details_alternative_names_expand_content_description),
                            onClick = { expanded = !expanded },
                            modifier = Modifier.align(Alignment.TopEnd),
                        )
                    }
                }
            }
        }
    }

    private fun <MediaEntry> LazyGridScope.mediaSection(
        entry: Entry<MediaEntry>,
        characterName: String?,
        characterSubtitle: String?,
        headerValues: CharacterHeaderValues,
        headerSharedTransitionKey: SharedTransitionKey?,
        coverImageState: CoilImageState,
        expanded: () -> Boolean,
        onExpandedChange: (Boolean) -> Unit,
        navigationController: NavigationController,
        mediaRow: @Composable (AnimeCharacterDetailsViewModel.Entry<MediaEntry>, Modifier) -> Unit,
    ) {
        listSection(
            titleRes = Res.string.anime_character_details_media_label,
            values = entry.media,
            valueToId = { "anime_media_${it.mediaId}" },
            aboveFold = MEDIA_ABOVE_FOLD,
            hasMoreValues = entry.mediaHasMore,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            onClickViewAll = {
                navigationController.navigate(
                    CharacterDestinations.CharacterMedias(
                        characterId = entry.character.id.toString(),
                        sharedTransitionKey = headerSharedTransitionKey,
                        headerParams = CharacterHeaderParams(
                            name = characterName,
                            subtitle = characterSubtitle,
                            favorite = headerValues.favorite,
                            coverImage = coverImageState.toImageState(),
                        )
                    )
                )
            },
            viewAllContentDescriptionTextRes =
                Res.string.anime_character_details_view_all_content_description
        ) { item, paddingBottom ->
            mediaRow(
                item,
                Modifier
                    .animateItem()
                    .padding(start = 16.dp, end = 16.dp, bottom = paddingBottom),
            )
        }
    }

    data class Entry<MediaEntry>(
        val character: Character,
        val media: List<AnimeCharacterDetailsViewModel.Entry<MediaEntry>>,
        val description: MarkdownText?,
    ) {
        val voiceActorsInitial = MutableStateFlow(
            PagingData.from(
                character.media?.edges?.filterNotNull()?.flatMap {
                    it.voiceActorRoles?.filterNotNull()
                        ?.mapNotNull { it.voiceActor }
                        ?.map {
                            StaffDetails(
                                id = it.id.toString(),
                                name = it.name,
                                image = it.image?.large,
                                role = it.languageV2,
                                staff = it,
                            )
                        }
                        .orEmpty()
                }.orEmpty()
                    .distinctBy { it.idWithRole }
            )
        )

        val mediaHasMore = character.media?.pageInfo?.hasNextPage == true
    }

    @Composable
    private fun rememberExpandedState() = rememberSaveable(
        saver = listSaver(
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
