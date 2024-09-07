package com.thekeeperofpie.artistalleydatabase.anime.media.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_activities_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_average_score_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_cds_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_chapters_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_characters_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_country_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_duration_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_duration_minutes
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_end_date_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_episodes_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_error_loading_secondary_data
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_error_loading_secondary_data_retry_button
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_favorites_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_format_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_forum_threads_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_hashtags_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_information_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_licensed_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_link_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_links_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_mean_score_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_more_actions_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_open_external
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_open_external_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_other_links_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_popularity_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_ranking_popular_all_time
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_ranking_popular_season_year
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_ranking_popular_year
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_ranking_rated_all_time
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_ranking_rated_season_year
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_ranking_rated_year
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_ranking_unknown
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_rankings_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_rankings_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_recommendations_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_relations_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_reviews_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_score_distribution_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_season_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_social_links_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_songs_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_source_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_staff_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_start_date_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_stats_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_status_distribution_completed
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_status_distribution_current_anime
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_status_distribution_current_manga
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_status_distribution_dropped
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_status_distribution_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_status_distribution_paused
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_status_distribution_planning
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_status_distribution_repeating
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_status_distribution_unknown
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_status_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_streaming_episode_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_streaming_episode_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_streaming_episode_spoiler
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_streaming_episode_spoiler_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_streaming_episodes_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_streaming_links_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_studios_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_studios_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_synonyms_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_synonyms_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_tag_with_rank_format
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_tags_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_trailer_dailymotion_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_trailer_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_trending_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_volumes_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_tag_long_click_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.no
import artistalleydatabase.modules.utils_compose.generated.resources.yes
import coil3.compose.AsyncImage
import com.anilist.MediaDetails2Query
import com.anilist.MediaDetailsQuery.Data.Media
import com.anilist.fragment.MediaNavigationData
import com.anilist.type.ExternalLinkType
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaRankType
import com.anilist.type.MediaRelation
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toColor
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toStatusIcon
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toStatusText
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.mediaListSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.DescriptionSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSection
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.media.YouTubePlayer
import com.thekeeperofpie.artistalleydatabase.media.rememberYouTubePlayerState
import com.thekeeperofpie.artistalleydatabase.utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.AssistChip
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeResourceUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSubsectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.InfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.assistChipColors
import com.thekeeperofpie.artistalleydatabase.utils_compose.charts.BarChart
import com.thekeeperofpie.artistalleydatabase.utils_compose.charts.PieChart
import com.thekeeperofpie.artistalleydatabase.utils_compose.expandableListInfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.colorsOrDefault
import com.thekeeperofpie.artistalleydatabase.utils_compose.multiplyCoerceSaturation
import com.thekeeperofpie.artistalleydatabase.utils_compose.optionalClickable
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import com.thekeeperofpie.artistalleydatabase.utils_compose.recomposeHighlighter
import com.thekeeperofpie.artistalleydatabase.utils_compose.showFloatingActionButtonOnVerticalScroll
import com.thekeeperofpie.artistalleydatabase.utils_compose.twoColumnInfoText
import io.fluidsonic.country.Country
import io.fluidsonic.i18n.name
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class,
)
object AnimeMediaDetailsScreen {

    private const val RELATIONS_ABOVE_FOLD = 3
    private const val STREAMING_EPISODES_ABOVE_FOLD = 3

    // Sorted by most relevant for an anime-first viewer
    val RELATION_SORT_ORDER = listOf(
        MediaRelation.PARENT,
        MediaRelation.PREQUEL,
        MediaRelation.SEQUEL,
        MediaRelation.SIDE_STORY,
        MediaRelation.SUMMARY,
        MediaRelation.ALTERNATIVE,
        MediaRelation.SPIN_OFF,
        MediaRelation.SOURCE,
        MediaRelation.ADAPTATION,
        MediaRelation.CHARACTER,
        MediaRelation.OTHER,
        MediaRelation.COMPILATION,
        MediaRelation.CONTAINS,
        MediaRelation.UNKNOWN__,
    )

    @Composable
    operator fun invoke(
        animeComponent: AnimeComponent = LocalAnimeComponent.current,
        viewModel: AnimeMediaDetailsViewModel = viewModel {
            animeComponent.animeMediaDetailsViewModel(createSavedStateHandle())
        },
        upIconOption: UpIconOption,
        headerValues: MediaHeaderValues,
        sharedTransitionKey: SharedTransitionKey?,
        coverImageState: CoilImageState?,
        charactersCount: (Entry?) -> Int,
        charactersSection: LazyListScope.(entry: Entry) -> Unit,
        staffCount: () -> Int,
        staffSection: LazyListScope.() -> Unit,
        songsSectionMetadata: SectionIndexInfo.SectionMetadata?,
        songsSection: LazyListScope.(
            expanded: () -> Boolean,
            onExpandedChange: (Boolean) -> Unit,
        ) -> Unit,
        cdsSectionMetadata: SectionIndexInfo.SectionMetadata?,
        cdsSection: LazyListScope.() -> Unit,
        requestLoadMedia2: () -> Unit,
        recommendationsSectionMetadata: SectionIndexInfo.SectionMetadata,
        recommendationsSection: LazyListScope.(
            expanded: () -> Boolean,
            onExpandedChange: (Boolean) -> Unit,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        activitiesSectionMetadata: SectionIndexInfo.SectionMetadata,
        activitiesSection: LazyListScope.(
            expanded: () -> Boolean,
            onExpandedChange: (Boolean) -> Unit,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        forumThreadsSectionMetadata: SectionIndexInfo.SectionMetadata,
        forumThreadsSection: LazyListScope.(
            expanded: () -> Boolean,
            onExpandedChange: (Boolean) -> Unit,
        ) -> Unit,
        reviewsSectionMetadata: SectionIndexInfo.SectionMetadata,
        reviewsSection: LazyListScope.(
            expanded: () -> Boolean,
            onExpandedChange: (Boolean) -> Unit,
        ) -> Unit,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        val entry = viewModel.entry
        val entry2 = viewModel.entry2
        val expandedState = rememberExpandedState()

        val viewer by viewModel.viewer.collectAsState()
        val sectionIndexInfo = buildSectionIndexInfo(
            entry = entry.result,
            entry2 = entry2.result,
            charactersCount = charactersCount(entry.result),
            staffCount = staffCount(),
            expandedState = expandedState,
            songsSection = songsSectionMetadata,
            cdsSection = cdsSectionMetadata,
            viewer = viewer,
            activitiesSection = activitiesSectionMetadata,
            recommendationsSection = recommendationsSectionMetadata,
            forumThreadsSection = forumThreadsSectionMetadata,
            reviewsSection = reviewsSectionMetadata,
        )

        var loadingThresholdPassed by remember { mutableStateOf(false) }
        val refreshing = (entry.loading || entry2.loading) && loadingThresholdPassed
        val pullRefreshState = rememberPullRefreshState(
            refreshing = refreshing,
            onRefresh = viewModel::refresh,
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
            val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
            // TODO: Pass media type if known so that open external works even if entry can't be loaded?
            MediaEditBottomSheetScaffold(
                viewModel = editViewModel,
                topBar = {
                    CollapsingToolbar(
                        maxHeight = 356.dp,
                        pinnedHeight = 120.dp,
                        scrollBehavior = scrollBehavior,
                    ) {
                        MediaHeader(
                            upIconOption = upIconOption,
                            mediaId = viewModel.mediaId,
                            mediaType = viewModel.entry.result?.media?.type,
                            titles = entry.result?.titlesUnique,
                            episodes = entry.result?.media?.episodes,
                            format = entry.result?.media?.format,
                            averageScore = entry.result?.media?.averageScore,
                            popularity = entry.result?.media?.popularity,
                            progress = it,
                            headerValues = headerValues,
                            onFavoriteChanged = {
                                viewModel.favoritesToggleHelper.set(
                                    headerValues.type.toFavoriteType(),
                                    viewModel.mediaId,
                                    it,
                                )
                            },
                            sharedTransitionKey = sharedTransitionKey,
                            coverImageState = coverImageState,
                            menuContent = {
                                Box {
                                    var showMenu by remember { mutableStateOf(false) }
                                    IconButton(onClick = {
                                        requestLoadMedia2()
                                        showMenu = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.MoreVert,
                                            contentDescription = stringResource(
                                                Res.string.anime_media_details_more_actions_content_description,
                                            ),
                                        )
                                    }

                                    val uriHandler = LocalUriHandler.current
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(Res.string.anime_media_details_open_external)) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Filled.OpenInBrowser,
                                                    contentDescription = stringResource(
                                                        Res.string.anime_media_details_open_external_icon_content_description
                                                    )
                                                )
                                            },
                                            onClick = {
                                                showMenu = false
                                                uriHandler.openUri(
                                                    AniListUtils.mediaUrl(
                                                        // TODO: Pass media type if known so that open external works even if entry can't be loaded?
                                                        entry.result?.media?.type
                                                            ?: MediaType.ANIME,
                                                        viewModel.mediaId,
                                                    ) + "?${UriUtils.FORCE_EXTERNAL_URI_PARAM}=true"
                                                )
                                            }
                                        )

                                        sectionIndexInfo.sections.forEach { (section, index) ->
                                            DropdownMenuItem(
                                                text = { Text(stringResource(section.titleRes)) },
                                                onClick = {
                                                    showMenu = false
                                                    scope.launch {
                                                        lazyListState.animateScrollToItem(index, 0)
                                                    }
                                                }
                                            )
                                        }

                                        if (entry2.loading) {
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                CircularProgressIndicator(
                                                    strokeWidth = 2.dp,
                                                    modifier = Modifier
                                                        .size(24.dp, 24.dp)
                                                        .padding(4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                        )
                    }
                },
            ) {
                Scaffold(
                    floatingActionButton = {
                        val media = entry.result?.media
                        if (media != null) {
                            val expanded by remember {
                                derivedStateOf { scrollBehavior.state.collapsedFraction == 0f }
                            }

                            val showFloatingActionButton =
                                viewModel.hasAuth.collectAsState(initial = false).value &&
                                        lazyListState.showFloatingActionButtonOnVerticalScroll()
                            AnimatedVisibility(
                                visible = showFloatingActionButton,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                val containerColor = headerValues.defaultColor
                                    .takeOrElse { FloatingActionButtonDefaults.containerColor }
                                val contentColor = ComposeColorUtils.bestTextColor(containerColor)
                                    ?: contentColorFor(containerColor)

                                val listStatus = viewModel.listStatus
                                val status = listStatus?.entry?.status
                                val mediaTitle = media.title?.primaryTitle()
                                ExtendedFloatingActionButton(
                                    text = {
                                        val progress = if (media.type == MediaType.ANIME) {
                                            listStatus?.entry?.progress
                                        } else {
                                            listStatus?.entry?.progressVolumes
                                        } ?: 0

                                        val progressMax = if (media.type == MediaType.ANIME) {
                                            media.episodes
                                        } else {
                                            media.volumes
                                        }

                                        Text(
                                            status.toStatusText(
                                                mediaType = media.type,
                                                progress = progress,
                                                progressMax = progressMax,
                                                score = listStatus?.entry?.score,
                                                scoreFormat = editViewModel.scoreFormat
                                                    .collectAsState().value,
                                            )
                                        )
                                    },
                                    icon = {
                                        val (vector, contentDescription) = status
                                            .toStatusIcon(mediaType = media.type)
                                        Icon(
                                            imageVector = vector,
                                            contentDescription = stringResource(contentDescription),
                                        )
                                    },
                                    expanded = status
                                        ?.takeUnless { it == MediaListStatus.UNKNOWN__ }
                                        ?.takeIf { expanded } != null,
                                    containerColor = containerColor,
                                    contentColor = contentColor,
                                    onClick = {
                                        if (showFloatingActionButton) {
                                            editViewModel.initialize(
                                                mediaId = media.id.toString(),
                                                coverImage = media.coverImage?.extraLarge,
                                                title = mediaTitle,
                                                mediaListEntry = listStatus?.entry,
                                                mediaType = media.type,
                                                status = listStatus?.entry?.status,
                                                maxProgress = MediaUtils.maxProgress(media),
                                                maxProgressVolumes = media.volumes,
                                            )
                                            editViewModel.editData.showing = true
                                        }
                                    },
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .fillMaxSize()
                ) { scaffoldPadding ->
                    val finalError = entry.result == null && !entry.loading
                    Crossfade(targetState = finalError, label = "Media details crossfade") {
                        if (it) {
                            AnimeMediaListScreen.Error(
                                errorTextRes = entry.error?.first,
                                exception = entry.error?.second,
                            )
                        } else if (entry.result != null) {
                            val hasReachedLinks by remember {
                                derivedStateOf {
                                    val threshold = sectionIndexInfo.linksSectionIndex
                                        ?: return@derivedStateOf false
                                    val lastVisibleIndex = lazyListState.layoutInfo
                                        .visibleItemsInfo.lastOrNull()?.index
                                        ?: return@derivedStateOf false
                                    lastVisibleIndex >= threshold
                                }
                            }
                            LaunchedEffect(hasReachedLinks) {
                                if (hasReachedLinks) {
                                    requestLoadMedia2()
                                }
                            }

                            LazyColumn(
                                state = lazyListState,
                                contentPadding = PaddingValues(bottom = 16.dp),
                                modifier = Modifier
                                    .padding(scaffoldPadding)
                                    .testTag("rootColumn")
                            ) {
                                content(
                                    viewModel = viewModel,
                                    viewer = viewer,
                                    entry = entry.result!!,
                                    entry2Result = entry2,
                                    coverImageState = coverImageState,
                                    onClickListEdit = editViewModel::initialize,
                                    expandedState = expandedState,
                                    charactersSection = charactersSection,
                                    staffSection = staffSection,
                                    songsSection = songsSection,
                                    cdsSection = cdsSection,
                                    recommendationsSection = recommendationsSection,
                                    activitiesSection = activitiesSection,
                                    forumThreadsSection = forumThreadsSection,
                                    reviewsSection = reviewsSection,
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

    private fun LazyListScope.content(
        viewModel: AnimeMediaDetailsViewModel,
        viewer: AniListViewer?,
        entry: Entry,
        entry2Result: LoadingResult<Entry2>,
        coverImageState: CoilImageState?,
        onClickListEdit: (MediaNavigationData) -> Unit,
        expandedState: ExpandedState,
        charactersSection: LazyListScope.(entry: Entry) -> Unit,
        staffSection: LazyListScope.() -> Unit,
        songsSection: LazyListScope.(
            expanded: () -> Boolean,
            onExpandedChange: (Boolean) -> Unit,
        ) -> Unit,
        cdsSection: LazyListScope.() -> Unit,
        recommendationsSection: LazyListScope.(
            expanded: () -> Boolean,
            onExpandedChange: (Boolean) -> Unit,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        activitiesSection: LazyListScope.(
            expanded: () -> Boolean,
            onExpandedChange: (Boolean) -> Unit,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        forumThreadsSection: LazyListScope.(
            expanded: () -> Boolean,
            onExpandedChange: (Boolean) -> Unit,
        ) -> Unit,
        reviewsSection: LazyListScope.(
            expanded: () -> Boolean,
            onExpandedChange: (Boolean) -> Unit,
        ) -> Unit,
    ) {
        if (entry.genres.isNotEmpty()) {
            item("genreSection", "genreSection") {
                GenreSection(genres = entry.genres, mediaType = entry.media.type)
            }
        }

        if (entry.description != null) {
            item("descriptionSection", "descriptionSection") {
                DescriptionSection(
                    markdownText = entry.description,
                    expanded = { expandedState.description },
                    onExpandedChange = { expandedState.description = it },
                )
            }
        }

        charactersSection(entry)

        relationsSection(
            onClickListEdit = onClickListEdit,
            viewer = viewer,
            entry = entry,
            relationsExpanded = expandedState::relations,
            onRelationsExpandedChange = { expandedState.relations = it },
        )

        infoSection(entry)

        songsSection(expandedState::songs) { expandedState.songs = it }

        cdsSection()

        staffSection()

        val entry2 = entry2Result.result
        if (entry2 != null) {
            statsSection(entry2)
            tagsSection(entry2, coverImageState)

            trailerSection(entry = entry2)

            streamingEpisodesSection(
                entry = entry2,
                expanded = expandedState::streamingEpisodes,
                onExpandedChange = { expandedState.streamingEpisodes = it },
                hidden = expandedState::streamingEpisodesHidden,
                onHiddenChange = { expandedState.streamingEpisodesHidden = it },
            )

            socialLinksSection(entry = entry2)
            streamingLinksSection(entry = entry2)
            otherLinksSection(entry = entry2)
        } else if (entry2Result.loading) {
            item("secondaryDataError") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            item("secondaryDataError") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.anime_media_details_error_loading_secondary_data),
                        textAlign = TextAlign.Center,
                    )

                    Button(onClick = viewModel::refreshSecondary) {
                        Text(stringResource(Res.string.anime_media_details_error_loading_secondary_data_retry_button))
                    }
                }
            }
        }

        recommendationsSection(
            expandedState::recommendations,
            { expandedState.recommendations = it },
            onClickListEdit,
        )

        activitiesSection(
            expandedState::activities,
            { expandedState.activities = it },
            onClickListEdit,
        )

        forumThreadsSection(expandedState::forumThreads) { expandedState.forumThreads = it }

        reviewsSection(
            expandedState::reviews,
            { expandedState.reviews = it },
        )
    }

    @Composable
    private fun LazyItemScope.GenreSection(
        genres: List<Entry.Genre>,
        mediaType: MediaType?,
    ) {
        val navigationCallback = LocalNavigationCallback.current
        val mediaGenreDialogController = LocalMediaGenreDialogController.current
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .animateContentSize()
                .animateItem()
                .recomposeHighlighter()
        ) {
            genres.forEach {
                AssistChip(
                    onClick = {
                        navigationCallback.navigate(
                            AnimeDestination.SearchMedia(
                                title = AnimeDestination.SearchMedia.Title.Custom(it.name),
                                genre = it.name,
                                mediaType = mediaType ?: MediaType.ANIME,
                            )
                        )
                    },
                    onLongClickLabel = stringResource(
                        Res.string.anime_media_tag_long_click_content_description
                    ),
                    onLongClick = { mediaGenreDialogController.onLongClickGenre(it.name) },
                    label = { AutoHeightText(it.name) },
                    colors = assistChipColors(
                        containerColor = it.color,
                        labelColor = it.textColor ?: MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }
    }

    private fun LazyListScope.relationsSection(
        viewer: AniListViewer?,
        entry: Entry,
        relationsExpanded: () -> Boolean,
        onRelationsExpandedChange: (Boolean) -> Unit,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        mediaListSection(
            onClickListEdit = onClickListEdit,
            viewer = viewer,
            titleRes = Res.string.anime_media_details_relations_label,
            values = entry.relations,
            valueToEntry = { it.entry },
            aboveFold = RELATIONS_ABOVE_FOLD,
            hasMoreValues = entry.relationsHasMore,
            expanded = relationsExpanded,
            onExpandedChange = onRelationsExpandedChange,
            label = { RelationLabel(it.relation) },
        )
    }

    private fun LazyListScope.infoSection(entry: Entry) {
        item("infoHeader") {
            DetailsSectionHeader(
                stringResource(Res.string.anime_media_details_information_label),
                modifier = Modifier.animateItem()
            )
        }

        val media = entry.media

        item("infoSectionOne") {
            ElevatedCard(
                modifier = Modifier
                    .animateContentSize()
                    .animateItem()
                    .padding(start = 16.dp, end = 16.dp, bottom = 2.dp)
            ) {
                twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_media_details_format_label),
                    bodyOne = stringResource(entry.formatTextRes),
                    labelTwo = stringResource(Res.string.anime_media_details_status_label),
                    bodyTwo = stringResource(entry.statusTextRes),
                    showDividerAbove = false,
                )

                twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_media_details_episodes_label),
                    bodyOne = media.episodes?.toString(),
                    labelTwo = stringResource(Res.string.anime_media_details_duration_label),
                    bodyTwo = media.duration?.let {
                        stringResource(Res.string.anime_media_details_duration_minutes, it)
                    },
                )

                twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_media_details_volumes_label),
                    bodyOne = media.volumes?.toString(),
                    labelTwo = stringResource(Res.string.anime_media_details_chapters_label),
                    bodyTwo = media.chapters?.toString(),
                )

                twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_media_details_source_label),
                    bodyOne = stringResource(media.source.toTextRes()),
                    labelTwo = stringResource(Res.string.anime_media_details_season_label),
                    bodyTwo = MediaUtils.formatSeasonYear(media.season, media.seasonYear),
                )

                val dateTimeFormatter = LocalDateTimeFormatter.current
                val startDateFormatted = media.startDate?.let {
                    remember(it) { dateTimeFormatter.formatDateTime(it.year, it.month, it.day) }
                }
                val endDateFormatted = media.endDate?.let {
                    remember(it) { dateTimeFormatter.formatDateTime(it.year, it.month, it.day) }
                }

                twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_media_details_start_date_label),
                    bodyOne = startDateFormatted,
                    labelTwo = stringResource(Res.string.anime_media_details_end_date_label),
                    bodyTwo = endDateFormatted,
                )
            }
        }

        item("infoSectionTwo") {
            ElevatedCard(
                modifier = Modifier
                    .animateContentSize()
                    .animateItem()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 2.dp)
            ) {
                var shown = twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_media_details_average_score_label),
                    bodyOne = media.averageScore?.toString(),
                    labelTwo = stringResource(Res.string.anime_media_details_mean_score_label),
                    bodyTwo = media.meanScore?.toString(),
                    showDividerAbove = false,
                )

                shown = twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_media_details_popularity_label),
                    bodyOne = media.popularity?.toString(),
                    labelTwo = stringResource(Res.string.anime_media_details_favorites_label),
                    bodyTwo = media.favourites?.toString(),
                    showDividerAbove = shown,
                ) || shown

                twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_media_details_trending_label),
                    bodyOne = media.trending?.toString(),
                    labelTwo = "",
                    bodyTwo = null,
                    showDividerAbove = shown,
                )
            }
        }

        item("infoSectionThree") {
            val showTopPadding = media.averageScore != null
                    || media.meanScore != null
                    || media.popularity != null
                    || media.favourites != null
                    || media.trending != null

            ElevatedCard(
                modifier = Modifier
                    .animateContentSize()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = if (showTopPadding) 16.dp else 0.dp,
                        bottom = 2.dp,
                    )
                    .animateItem(),
            ) {
                var shown = twoColumnInfoText(
                    labelOne = stringResource(Res.string.anime_media_details_licensed_label),
                    bodyOne = entry.licensedTextRes?.let { ComposeResourceUtils.stringResource(it) },
                    labelTwo = stringResource(Res.string.anime_media_details_country_label),
                    bodyTwo = io.fluidsonic.locale.Locale.forLanguageTagOrNull(
                        androidx.compose.ui.text.intl.Locale.current.toLanguageTag()
                    )?.let { entry.country?.name(it) },
                    showDividerAbove = false,
                )

                val uriHandler = LocalUriHandler.current
                if (media.hashtag != null) {
                    Column(modifier = Modifier
                        .optionalClickable(
                            onClick = if (entry.hashtags == null) null else {
                                {
                                    uriHandler.openUri(
                                        MediaUtils.twitterHashtagsLink(entry.hashtags)
                                    )
                                }
                            }
                        )
                    ) {
                        InfoText(
                            label = stringResource(Res.string.anime_media_details_hashtags_label),
                            body = media.hashtag!!,
                            showDividerAbove = shown,
                        )
                    }

                    shown = true
                }

                // TODO: isFavorite, isAdult, airingSchedule, reviews, trends

                shown = expandableListInfoText(
                    labelTextRes = Res.string.anime_media_details_studios_label,
                    contentDescriptionTextRes = Res.string.anime_media_details_studios_expand_content_description,
                    values = entry.studios,
                    valueToText = { it.name },
                    onClick = { uriHandler.openUri(AniListUtils.studioUrl(it.id)) },
                    showDividerAbove = shown,
                ) || shown

                expandableListInfoText(
                    labelTextRes = Res.string.anime_media_details_synonyms_label,
                    contentDescriptionTextRes = Res.string.anime_media_details_synonyms_expand_content_description,
                    values = entry.allSynonyms,
                    valueToText = { it },
                    showDividerAbove = shown,
                )
            }
        }
    }

    @Composable
    fun RelationLabel(relation: MediaRelation) {
        Text(
            text = stringResource(relation.toTextRes()),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.surfaceTint,
            modifier = Modifier
                .wrapContentHeight()
                .padding(
                    start = 12.dp,
                    top = 10.dp,
                    end = 16.dp,
                )
        )
    }

    private fun LazyListScope.statsSection(entry: Entry2) {
        item("statsHeader") {
            DetailsSectionHeader(
                stringResource(Res.string.anime_media_details_stats_label),
                modifier = Modifier.animateItem()
            )
        }

        item("statsSection") {
            ElevatedCard(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 2.dp)
                    .animateItem()
            ) {
                expandableListInfoText(
                    labelTextRes = Res.string.anime_media_details_rankings_label,
                    contentDescriptionTextRes = Res.string.anime_media_details_rankings_expand_content_description,
                    values = entry.rankings,
                    valueToText = {
                        when (it.type) {
                            MediaRankType.RATED -> MediaUtils.formatRanking(
                                ranking = it,
                                seasonYearTextRes = Res.string.anime_media_details_ranking_rated_season_year,
                                yearTextRes = Res.string.anime_media_details_ranking_rated_year,
                                allTimeTextRes = Res.string.anime_media_details_ranking_rated_all_time,
                            )
                            MediaRankType.POPULAR -> MediaUtils.formatRanking(
                                ranking = it,
                                seasonYearTextRes = Res.string.anime_media_details_ranking_popular_season_year,
                                yearTextRes = Res.string.anime_media_details_ranking_popular_year,
                                allTimeTextRes = Res.string.anime_media_details_ranking_popular_all_time,
                            )
                            MediaRankType.UNKNOWN__ -> stringResource(
                                Res.string.anime_media_details_ranking_unknown,
                                it.rank,
                                it.context,
                            )
                        }
                    },
                    showDividerAbove = false,
                )

                val statusDistribution = entry.media.stats?.statusDistribution
                    ?.filterNotNull()?.takeIf { it.isNotEmpty() }
                if (statusDistribution != null) {

                    HorizontalDivider()
                    DetailsSubsectionHeader(
                        stringResource(Res.string.anime_media_details_status_distribution_label)
                    )

                    PieChart(
                        slices = statusDistribution,
                        sliceToKey = { it.status },
                        sliceToAmount = { it.amount ?: 0 },
                        sliceToColor = { it.status.toColor() },
                        sliceToText = { slice ->
                            when (slice.status) {
                                MediaListStatus.CURRENT -> if (entry.media.type == MediaType.ANIME) {
                                    Res.string.anime_media_details_status_distribution_current_anime
                                } else {
                                    Res.string.anime_media_details_status_distribution_current_manga
                                }
                                MediaListStatus.PLANNING -> Res.string.anime_media_details_status_distribution_planning
                                MediaListStatus.COMPLETED -> Res.string.anime_media_details_status_distribution_completed
                                MediaListStatus.DROPPED -> Res.string.anime_media_details_status_distribution_dropped
                                MediaListStatus.PAUSED -> Res.string.anime_media_details_status_distribution_paused
                                MediaListStatus.REPEATING -> Res.string.anime_media_details_status_distribution_repeating
                                MediaListStatus.UNKNOWN__, null -> Res.string.anime_media_details_status_distribution_unknown
                            }.let { stringResource(it, slice.amount ?: 0) }
                        },
                        keySave = { it?.rawValue.orEmpty() },
                        keyRestore = { key ->
                            MediaListStatus.values().find { it.rawValue == key }
                                ?: MediaListStatus.UNKNOWN__
                        },
                    )
                }

                val scoreDistribution = entry.scoreDistribution
                if (scoreDistribution.isNotEmpty()) {
                    HorizontalDivider()
                    DetailsSubsectionHeader(
                        stringResource(Res.string.anime_media_details_score_distribution_label)
                    )

                    val firstColorIndex =
                        scoreDistribution.size - MediaUtils.scoreDistributionColors.size
                    BarChart(
                        slices = scoreDistribution,
                        sliceToAmount = { it.amount ?: 0 },
                        sliceToColor = { index, _ ->
                            if (index >= firstColorIndex) {
                                MediaUtils.scoreDistributionColors[index - firstColorIndex]
                            } else {
                                MediaUtils.scoreDistributionColors.first()
                            }
                        },
                        sliceToText = { it.score.toString() },
                        showBarPadding = false,
                    )
                }
            }
        }
    }

    private fun LazyListScope.tagsSection(
        entry: Entry2,
        coverImageState: CoilImageState?,
    ) {
        if (entry.tags.isNotEmpty()) {
            item("tagsHeader") {
                DetailsSectionHeader(
                    stringResource(Res.string.anime_media_details_tags_label),
                    modifier = Modifier.animateItem()
                )
            }

            item("tagsSection") {
                val navigationCallback = LocalNavigationCallback.current
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateItem()
                ) {
                    entry.tags.forEach {
                        val (containerColor, textColor) = coverImageState.colorsOrDefault()
                        AnimeMediaTagEntry.Chip(
                            tag = it,
                            title = {
                                if (it.rank == null) {
                                    it.name
                                } else {
                                    stringResource(
                                        Res.string.anime_media_details_tag_with_rank_format,
                                        it.name,
                                        it.rank
                                    )
                                }
                            },
                            onTagClick = { id, name ->
                                navigationCallback.navigate(
                                    AnimeDestination.SearchMedia(
                                        title = AnimeDestination.SearchMedia.Title.Custom(name),
                                        tagId = id,
                                        mediaType = entry.media.type ?: MediaType.ANIME,
                                    )
                                )
                            },
                            containerColor = containerColor,
                            textColor = textColor,
                            autoResize = false,
                        )
                    }
                }
            }
        }
    }

    private fun LazyListScope.trailerSection(
        entry: Entry2,
    ) {
        val trailer = entry.media.trailer ?: return
        if (trailer.site != "youtube" && trailer.site != "dailymotion") return

        val videoId = trailer.id ?: return

        item("trailerHeader") {
            DetailsSectionHeader(
                stringResource(Res.string.anime_media_details_trailer_label),
                modifier = Modifier.animateItem()
            )
        }

        if (trailer.site == "youtube") {
            item("trailerSection") {
                ElevatedCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateItem()
                ) {
                    YouTubePlayer(state = rememberYouTubePlayerState(videoId))
                }
            }
        } else {
            item("trailerSection") {
                val uriHandler = LocalUriHandler.current
                ElevatedCard(
                    onClick = { uriHandler.openUri(MediaUtils.dailymotionUrl(videoId)) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateItem()
                ) {
                    AsyncImage(
                        model = trailer.thumbnail,
                        contentDescription = stringResource(
                            Res.string.anime_media_details_trailer_dailymotion_content_description
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f)
                    )
                }
            }
        }
    }

    private fun LazyListScope.streamingEpisodesSection(
        entry: Entry2,
        expanded: () -> Boolean,
        onExpandedChange: (Boolean) -> Unit,
        hidden: () -> Boolean,
        onHiddenChange: (Boolean) -> Unit,
    ) {
        val streamingEpisodes = entry.streamingEpisodes
        listSection(
            titleRes = Res.string.anime_media_details_streaming_episodes_label,
            values = streamingEpisodes,
            valueToId = { it.url },
            aboveFold = STREAMING_EPISODES_ABOVE_FOLD,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            hidden = hidden,
            hiddenContent = {
                ElevatedCard(
                    onClick = { onHiddenChange(false) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = stringResource(
                                Res.string.anime_media_details_streaming_episode_spoiler_content_description
                            ),
                            modifier = Modifier
                                .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                        )

                        Text(
                            text = stringResource(
                                Res.string.anime_media_details_streaming_episode_spoiler
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .weight(1f)
                        )
                    }
                }
            }
        ) { item, paddingBottom ->
            val uriHandler = LocalUriHandler.current
            ElevatedCard(
                modifier = Modifier
                    .animateItem()
                    .padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
                    .optionalClickable(onClick = item.url?.let { { uriHandler.openUri(it) } }),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.animateContentSize()
                ) {

                    val fullscreenImageHandler = LocalFullscreenImageHandler.current
                    AsyncImage(
                        model = item.thumbnail,
                        contentScale = ContentScale.FillHeight,
                        contentDescription = stringResource(
                            Res.string.anime_media_details_streaming_episode_content_description
                        ),
                        modifier = Modifier
                            .widthIn(max = 200.dp)
                            .combinedClickable(
                                onClick = { item.url?.let(uriHandler::openUri) },
                                onLongClick = {
                                    item.thumbnail?.let(fullscreenImageHandler::openImage)
                                }
                            )
                    )

                    Text(
                        text = item.title.orEmpty(),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .weight(1f),
                    )

                    val site = item.site
                    if (site != null) {
                        val icon =
                            entry.media.externalLinks?.find { it?.site == site }?.icon
                        if (icon != null) {
                            AsyncImage(
                                model = icon,
                                contentDescription = stringResource(
                                    Res.string.anime_media_details_streaming_episode_icon_content_description
                                ),
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun LazyListScope.socialLinksSection(entry: Entry2) {
        linksSection(Res.string.anime_media_details_social_links_label, entry.socialLinks)
    }

    private fun LazyListScope.streamingLinksSection(entry: Entry2) {
        linksSection(Res.string.anime_media_details_streaming_links_label, entry.streamingLinks)
    }

    private fun LazyListScope.otherLinksSection(entry: Entry2) {
        linksSection(Res.string.anime_media_details_other_links_label, entry.otherLinks)
    }

    private fun LazyListScope.linksSection(headerRes: StringResource, links: List<Entry2.Link>) {
        if (links.isEmpty()) return

        item("linksHeader-$headerRes") {
            DetailsSectionHeader(
                stringResource(headerRes),
                modifier = Modifier.animateItem()
            )
        }

        item("linksSection-$headerRes") {
            val uriHandler = LocalUriHandler.current
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .animateItem()
            ) {
                links.forEach {
                    androidx.compose.material3.AssistChip(
                        leadingIcon = if (it.icon == null) null else {
                            {
                                AsyncImage(
                                    model = it.icon,
                                    contentDescription = stringResource(
                                        Res.string.anime_media_details_link_content_description,
                                        it.site,
                                    ),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(2.dp),
                                )
                            }
                        },
                        label = {
                            AutoHeightText(
                                text = it.site,
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = it.color ?: Color.Transparent,
                            labelColor = it.textColor ?: MaterialTheme.colorScheme.onSurface,
                        ),
                        onClick = { uriHandler.openUri(it.url) },
                    )
                }
            }
        }
    }

    @Immutable
    data class Entry(
        val mediaId: String,
        val media: Media,
        val relations: List<Relation>,
        val description: MarkdownText?,
    ) {
        val id = EntryId("media", mediaId)
        val titlesUnique = media.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()

        val formatTextRes = media.format.toTextRes()
        val statusTextRes = media.status.toTextRes()
        val licensedTextRes = media.isLicensed
            ?.let { if (it) UtilsStrings.yes else UtilsStrings.no }
        val country = media.countryOfOrigin?.toString()?.let(Country.Companion::forCodeOrNull)
        val hashtags = media.hashtag?.split("#")
            ?.filter { it.isNotEmpty() }
            ?.map { "#${it.trim()}" }

        val allSynonyms = listOfNotNull(
            media.title?.userPreferred,
            media.title?.romaji,
            media.title?.english,
            media.title?.native,
        ).distinct() + media.synonyms?.filterNotNull().orEmpty()

        val genres = media.genres?.filterNotNull().orEmpty().map(::Genre)

        val studios = media.studios?.edges?.filterNotNull()?.map {
            Studio(
                id = it.node?.id.toString(),
                name = it.node?.name.orEmpty(),
                main = it.isMain,
            )
        }.orEmpty()
            .sortedByDescending { it.main }

        val relationsHasMore = media.relations?.pageInfo?.hasNextPage ?: true

        data class Genre(
            val name: String,
            val color: Color = MediaUtils.genreColor(name),
            val textColor: Color? = ComposeColorUtils.bestTextColor(color),
        )

        data class Relation(
            val id: String,
            val relation: MediaRelation,
            val entry: MediaPreviewEntry,
        )

        data class Studio(
            val id: String,
            val name: String,
            val main: Boolean,
        )
    }

    data class Entry2(
        val mediaId: String,
        val media: MediaDetails2Query.Data.Media,
    ) {
        val tags = media.tags?.filterNotNull()?.map(::AnimeMediaTagEntry).orEmpty()

        private val links = media.externalLinks?.filterNotNull()?.mapNotNull {
            Link(
                id = it.id.toString(),
                type = it.type,
                url = it.url ?: return@mapNotNull null,
                icon = it.icon,
                site = it.site,
                color = it.color
                    ?.let(ComposeColorUtils::hexToColor)
                    ?.multiplyCoerceSaturation(0.75f, 0.75f)
            )
        }.orEmpty()

        val socialLinks = links.filter { it.type == ExternalLinkType.SOCIAL }
        val streamingLinks = links.filter { it.type == ExternalLinkType.STREAMING }
        val otherLinks = links.filter {
            it.type != ExternalLinkType.SOCIAL && it.type != ExternalLinkType.STREAMING
        } + listOfNotNull(
            media.type?.let {
                Link(
                    id = "AniList",
                    type = ExternalLinkType.INFO,
                    url = AniListUtils.mediaUrl(it, mediaId) +
                            "?${UriUtils.FORCE_EXTERNAL_URI_PARAM}=true",
                    site = "AniList",
                )
            }
        )

        val rankings = media.rankings?.filterNotNull().orEmpty()

        val scoreDistribution = media.stats?.scoreDistribution
            ?.filterNotNull()
            ?.let {
                // TODO: This is a bad hack. Use a real graphing library.
                // Fill any missing scores so the graph scale is correct
                if (it.size < 10 && it.all { (it.score ?: 0) % 10 == 0 }) {
                    val list = it.toMutableList()
                    repeat(10) {
                        val value = (it + 1) * 10
                        if (list.none { it.score == value }) {
                            list += MediaDetails2Query.Data.Media.Stats.ScoreDistribution(
                                score = value,
                                amount = 0
                            )
                        }
                    }
                    list
                } else {
                    it
                }
            }
            ?.sortedBy { it.score }
            .orEmpty()

        val streamingEpisodes = media.streamingEpisodes?.filterNotNull().orEmpty()

        data class Link(
            val id: String,
            val type: ExternalLinkType?,
            val url: String,
            val site: String,
            val icon: String? = null,
            val color: Color? = null,
            val textColor: Color? = color
                ?.let(ComposeColorUtils::bestTextColor),
        )
    }

    @Composable
    private fun rememberExpandedState() = rememberSaveable(saver = listSaver(
        save = {
            listOf(
                it.description,
                it.relations,
                it.recommendations,
                it.songs,
                it.streamingEpisodes,
                it.streamingEpisodesHidden,
                it.reviews,
                it.activities,
                it.forumThreads,
            )
        },
        restore = {
            ExpandedState(
                description = it[0],
                relations = it[1],
                recommendations = it[2],
                songs = it[3],
                streamingEpisodes = it[4],
                streamingEpisodesHidden = it[5],
                reviews = it[6],
                activities = it[7],
                forumThreads = it[8],
            )
        }
    )) {
        ExpandedState()
    }

    private class ExpandedState(
        description: Boolean = false,
        relations: Boolean = false,
        recommendations: Boolean = false,
        songs: Boolean = false,
        streamingEpisodes: Boolean = false,
        streamingEpisodesHidden: Boolean = true,
        reviews: Boolean = false,
        activities: Boolean = false,
        forumThreads: Boolean = false,
    ) {
        var description by mutableStateOf(description)
        var relations by mutableStateOf(relations)
        var recommendations by mutableStateOf(recommendations)
        var songs by mutableStateOf(songs)
        var streamingEpisodes by mutableStateOf(streamingEpisodes)
        var streamingEpisodesHidden by mutableStateOf(streamingEpisodesHidden)
        var reviews by mutableStateOf(reviews)
        var activities by mutableStateOf(activities)
        var forumThreads by mutableStateOf(forumThreads)

        fun allValues() = listOf(
            description,
            relations,
            recommendations,
            songs,
            streamingEpisodes,
            streamingEpisodesHidden,
            activities,
            forumThreads,
        )
    }

    // TODO: Fix a better mechanism; currently manually synced with screen logic
    @Composable
    private fun buildSectionIndexInfo(
        entry: Entry?,
        entry2: Entry2?,
        charactersCount: Int,
        staffCount: Int,
        expandedState: ExpandedState,
        songsSection: SectionIndexInfo.SectionMetadata?,
        cdsSection: SectionIndexInfo.SectionMetadata?,
        viewer: AniListViewer?,
        activitiesSection: SectionIndexInfo.SectionMetadata?,
        recommendationsSection: SectionIndexInfo.SectionMetadata?,
        forumThreadsSection: SectionIndexInfo.SectionMetadata?,
        reviewsSection: SectionIndexInfo.SectionMetadata?,
    ) = remember(
        entry,
        entry2,
        charactersCount,
        staffCount,
        expandedState.allValues(),
        songsSection,
        cdsSection,
        viewer,
        activitiesSection,
        forumThreadsSection,
        reviewsSection,
    ) {
        if (entry == null) return@remember SectionIndexInfo(emptyList(), null)
        val list = mutableListOf<Pair<SectionIndexInfo.Section, Int>>()
        var currentIndex = 0
        if (entry.genres.isNotEmpty()) currentIndex += 1
        if (!entry.media.description.isNullOrEmpty()) currentIndex += 1
        if (charactersCount > 0) {
            list += SectionIndexInfo.Section.CHARACTERS to currentIndex
            currentIndex += 2
        }
        fun runListSection(size: Int, aboveFold: Int, expanded: Boolean, hasMore: Boolean) {
            currentIndex += 1
            currentIndex += size.coerceAtMost(aboveFold)

            if (size > aboveFold) {
                if (expanded) {
                    currentIndex += size - aboveFold
                }
                currentIndex += 1
            } else if (hasMore) {
                currentIndex += 1
            }
        }

        if (entry.relations.isNotEmpty()) {
            list += SectionIndexInfo.Section.RELATIONS to currentIndex
            runListSection(
                size = entry.relations.size,
                aboveFold = RELATIONS_ABOVE_FOLD,
                expanded = expandedState.relations,
                hasMore = entry.relationsHasMore,
            )
        }

        list += SectionIndexInfo.Section.INFO to currentIndex
        currentIndex += 4

        val songsCount = songsSection?.count(viewer, expandedState.songs) ?: 0
        if (songsCount > 0) {
            list += SectionIndexInfo.Section.SONGS to currentIndex
            currentIndex += songsCount
        }

        val cdsCount = cdsSection?.count(viewer, false) ?: 0
        if (cdsCount > 0) {
            list += SectionIndexInfo.Section.CDS to currentIndex
            currentIndex += cdsCount
        }

        if (staffCount > 0) {
            list += SectionIndexInfo.Section.STAFF to currentIndex
            currentIndex += 2
        }

        list += SectionIndexInfo.Section.STATS to currentIndex
        currentIndex += 2

        val linksSectionIndex = currentIndex

        if (entry2 == null) {
            return@remember SectionIndexInfo(list, linksSectionIndex = linksSectionIndex)
        }

        // TODO: If entry2 values are null, show/mock header
        if (entry2.tags.isNotEmpty()) {
            list += SectionIndexInfo.Section.TAGS to currentIndex
            currentIndex += 2
        }

        val trailer = entry2.media.trailer
        if (trailer != null && (trailer.site == "youtube" || trailer.site == "dailymotion")) {
            list += SectionIndexInfo.Section.TRAILER to currentIndex
            currentIndex += 2
        }

        val streamingEpisodes = entry2.media.streamingEpisodes?.filterNotNull().orEmpty()
        if (streamingEpisodes.isNotEmpty()) {
            list += SectionIndexInfo.Section.EPISODES to currentIndex
            if (expandedState.streamingEpisodesHidden) {
                currentIndex += 2
            } else {
                runListSection(
                    size = streamingEpisodes.size,
                    aboveFold = STREAMING_EPISODES_ABOVE_FOLD,
                    expanded = expandedState.streamingEpisodes,
                    hasMore = false,
                )
            }
        }

        if (entry2.socialLinks.isNotEmpty()
            || entry2.streamingLinks.isNotEmpty()
            || entry2.otherLinks.isNotEmpty()
        ) {
            list += SectionIndexInfo.Section.LINKS to currentIndex
        }

        if (entry2.socialLinks.isNotEmpty()) {
            currentIndex += 2
        }

        if (entry2.streamingLinks.isNotEmpty()) {
            currentIndex += 2
        }

        if (entry2.otherLinks.isNotEmpty()) {
            currentIndex += 2
        }


        val recommendationsCount =
            recommendationsSection?.count(viewer, expandedState.recommendations) ?: 0
        if (recommendationsCount > 0) {
            list += SectionIndexInfo.Section.RECOMMENDATIONS to currentIndex
            currentIndex += recommendationsCount
        }

        val activitiesCount = activitiesSection?.count(viewer, expandedState.activities) ?: 0
        if (activitiesCount > 0) {
            list += SectionIndexInfo.Section.ACTIVITIES to currentIndex
            currentIndex += activitiesCount
        }

        val forumThreadsCount = forumThreadsSection?.count(viewer, expandedState.forumThreads) ?: 0
        if (forumThreadsCount > 0) {
            list += SectionIndexInfo.Section.FORUM_THREADS to currentIndex
            currentIndex += forumThreadsCount
        }

        val reviewsCount = reviewsSection?.count(viewer, expandedState.reviews) ?: 0
        if (reviewsCount > 0) {
            list += SectionIndexInfo.Section.REVIEWS to currentIndex
            currentIndex += reviewsCount
        }

        SectionIndexInfo(list, linksSectionIndex = linksSectionIndex)
    }

    data class SectionIndexInfo(
        val sections: List<Pair<Section, Int>>,
        val linksSectionIndex: Int?,
    ) {

        enum class Section(val titleRes: StringResource) {
            CHARACTERS(Res.string.anime_media_details_characters_label),
            RELATIONS(Res.string.anime_media_details_relations_label),
            INFO(Res.string.anime_media_details_information_label),
            SONGS(Res.string.anime_media_details_songs_label),
            CDS(Res.string.anime_media_details_cds_label),
            STAFF(Res.string.anime_media_details_staff_label),
            STATS(Res.string.anime_media_details_stats_label),
            TAGS(Res.string.anime_media_details_tags_label),
            TRAILER(Res.string.anime_media_details_trailer_label),
            EPISODES(Res.string.anime_media_details_episodes_label),
            LINKS(Res.string.anime_media_details_links_label),
            RECOMMENDATIONS(Res.string.anime_media_details_recommendations_label),
            REVIEWS(Res.string.anime_media_details_reviews_label),
            ACTIVITIES(Res.string.anime_media_details_activities_label),
            FORUM_THREADS(Res.string.anime_media_details_forum_threads_label),
        }

        interface SectionMetadata {
            abstract fun count(viewer: AniListViewer?, expanded: Boolean): Int

            data object Empty : SectionMetadata {
                override fun count(viewer: AniListViewer?, expanded: Boolean) = 0
            }

            data class ListSection<T>(
                val items: List<T>?,
                val aboveFold: Int,
                val hasMore: Boolean,
                val addOneForViewer: Boolean = false,
            ) : SectionMetadata {
                override fun count(viewer: AniListViewer?, expanded: Boolean): Int {
                    if (items != null && items.isEmpty()) return 0
                    val size = items?.size ?: 0
                    var count = 1
                    if (addOneForViewer && viewer != null) {
                        count++
                    }
                    count += size.coerceAtMost(aboveFold)

                    if (size > aboveFold) {
                        if (expanded) {
                            count += size - aboveFold
                        }
                        count += 1
                    } else if (hasMore) {
                        count += 1
                    }
                    return count
                }
            }
        }
    }
}