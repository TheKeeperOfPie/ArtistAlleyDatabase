package com.thekeeperofpie.artistalleydatabase.anime.media.details

import android.view.View
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.size.Dimension
import com.anilist.MediaDetails2Query
import com.anilist.MediaDetailsQuery.Data.Media
import com.anilist.type.ExternalLinkType
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaRankType
import com.anilist.type.MediaRelation
import com.anilist.type.MediaType
import com.mxalbert.sharedelements.SharedElement
import com.neovisionaries.i18n.CountryCode
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.orEmpty
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ListActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anime.character.DetailsCharacter
import com.thekeeperofpie.artistalleydatabase.anime.character.charactersSection
import com.thekeeperofpie.artistalleydatabase.anime.forum.ThreadSmallCard
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadEntry
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadToggleUpdate
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toColor
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toStatusIcon
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toStatusText
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.mediaListSection
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationData
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewSmallCard
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongEntry
import com.thekeeperofpie.artistalleydatabase.anime.staff.DetailsStaff
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.DescriptionSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSectionWithoutHeader
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.AssistChip
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.BarChart
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSubsectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.InfoText
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.PieChart
import com.thekeeperofpie.artistalleydatabase.compose.StableSpanned
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.assistChipColors
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.expandableListInfoText
import com.thekeeperofpie.artistalleydatabase.compose.multiplyCoerceSaturation
import com.thekeeperofpie.artistalleydatabase.compose.optionalClickable
import com.thekeeperofpie.artistalleydatabase.compose.recomposeHighlighter
import com.thekeeperofpie.artistalleydatabase.compose.showFloatingActionButtonOnVerticalScroll
import com.thekeeperofpie.artistalleydatabase.compose.twoColumnInfoText
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@Suppress("NAME_SHADOWING")
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class, ExperimentalMaterialApi::class
)
object AnimeMediaDetailsScreen {

    private const val RELATIONS_ABOVE_FOLD = 3
    private const val RECOMMENDATIONS_ABOVE_FOLD = 3
    private const val SONGS_ABOVE_FOLD = 3
    private const val STREAMING_EPISODES_ABOVE_FOLD = 3
    private const val REVIEWS_ABOVE_FOLD = 3
    private const val ACTIVITIES_ABOVE_FOLD = 3
    private const val FORUM_THREADS_ABOVE_FOLD = 3

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
        viewModel: AnimeMediaDetailsViewModel = hiltViewModel(),
        upIconOption: UpIconOption,
        headerValues: MediaHeaderValues,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        var coverImageWidthToHeightRatio by remember {
            mutableFloatStateOf(headerValues.coverImageWidthToHeightRatio)
        }
        val coverImageWidthToHeightRatioGetter: () -> Float =
            remember { { coverImageWidthToHeightRatio } }
        var headerTransitionFinished by remember { mutableStateOf(false) }
        val entry = viewModel.entry
        val entry2 = viewModel.entry2
        val charactersInitial = entry.result?.charactersInitial.orEmpty()
        val charactersDeferred = viewModel.charactersDeferred.collectAsLazyPagingItems()
        val charactersDeferredGetter = remember { { charactersDeferred } }
        val staff = viewModel.staff.collectAsLazyPagingItems()
        val expandedState = rememberExpandedState()
        val animeSongs = viewModel.animeSongs

        val viewer by viewModel.viewer.collectAsState()
        val activities = viewModel.activities
        var activityTabIsFollowing by rememberSaveable(viewer, activities) {
            mutableStateOf(!activities?.following.isNullOrEmpty())
        }
        val onActivityTabChange: (ActivityTab) -> Unit =
            remember { { activityTabIsFollowing = it == ActivityTab.FOLLOWING } }

        val sectionIndexInfo = buildSectionIndexInfo(
            entry = entry.result,
            entry2 = entry2.result,
            charactersCount = charactersDeferred.itemCount.coerceAtLeast(charactersInitial.size),
            staff = staff,
            expandedState = expandedState,
            animeSongs = animeSongs,
            cdEntries = viewModel.cdEntries,
            viewer = viewer,
            activities = if (activityTabIsFollowing) activities?.following else activities?.global,
            forumThreads = viewModel.forumThreads,
        )

        var loadingThresholdPassed by remember { mutableStateOf(false) }
        val refreshing =
            headerTransitionFinished && (entry.loading || entry2.loading) && loadingThresholdPassed
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
            val editViewModel = hiltViewModel<MediaEditViewModel>()
            MediaEditBottomSheetScaffold(
                screenKey = viewModel.screenKey,
                viewModel = editViewModel,
                topBar = {
                    CollapsingToolbar(
                        maxHeight = 356.dp,
                        pinnedHeight = 120.dp,
                        scrollBehavior = scrollBehavior,
                    ) {
                        MediaHeader(
                            screenKey = viewModel.screenKey,
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
                            onImageWidthToHeightRatioAvailable = {
                                coverImageWidthToHeightRatio = it
                            },
                            onCoverImageSharedElementFractionChanged = {
                                if (it == 0f) {
                                    headerTransitionFinished = true
                                }
                            },
                            menuContent = {
                                Box {
                                    var showMenu by remember { mutableStateOf(false) }
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Filled.MoreVert,
                                            contentDescription = stringResource(
                                                R.string.anime_media_details_more_actions_content_description,
                                            ),
                                        )
                                    }

                                    val uriHandler = LocalUriHandler.current
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.anime_media_details_open_external)) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Filled.OpenInBrowser,
                                                    contentDescription = stringResource(
                                                        R.string.anime_media_details_open_external_icon_content_description
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
                                    }
                                }
                            }
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
                                val containerColor = headerValues.color
                                    ?: FloatingActionButtonDefaults.containerColor
                                val contentColor =
                                    ComposeColorUtils.bestTextColor(containerColor)
                                        ?: contentColorFor(containerColor)

                                val listStatus = viewModel.listStatus
                                val status = listStatus?.entry?.status
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
                                                coverImage = null,
                                                type = null,
                                                title = null,
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
                            Content(
                                scaffoldPadding = scaffoldPadding,
                                lazyListState = lazyListState,
                                viewModel = viewModel,
                                editViewModel = editViewModel,
                                viewer = viewer,
                                entry = entry.result!!,
                                entry2Result = entry2,
                                charactersInitial = charactersInitial,
                                charactersDeferred = charactersDeferredGetter,
                                staff = staff,
                                activityTab = if (activityTabIsFollowing) ActivityTab.FOLLOWING else ActivityTab.GLOBAL,
                                activities = if (activityTabIsFollowing) activities?.following else activities?.global,
                                onActivityTabChange = onActivityTabChange,
                                expandedState = expandedState,
                                coverImageWidthToHeightRatio = coverImageWidthToHeightRatioGetter,
                            )
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

    @Composable
    private fun Content(
        scaffoldPadding: PaddingValues,
        lazyListState: LazyListState,
        viewModel: AnimeMediaDetailsViewModel,
        editViewModel: MediaEditViewModel,
        viewer: AniListViewer?,
        entry: Entry,
        entry2Result: LoadingResult<Entry2>,
        charactersInitial: ImmutableList<DetailsCharacter>,
        charactersDeferred: () -> LazyPagingItems<DetailsCharacter>,
        staff: LazyPagingItems<DetailsStaff>,
        activityTab: ActivityTab,
        activities: ImmutableList<AnimeMediaDetailsViewModel.ActivityEntry>?,
        onActivityTabChange: (ActivityTab) -> Unit,
        expandedState: ExpandedState,
        coverImageWidthToHeightRatio: () -> Float,
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.padding(scaffoldPadding)
        ) {
            content(
                viewModel = viewModel,
                editViewModel = editViewModel,
                viewer = viewer,
                entry = entry,
                entry2Result = entry2Result,
                charactersInitial = charactersInitial,
                charactersDeferred = charactersDeferred,
                staff = staff,
                activityTab = activityTab,
                activities = activities,
                onActivityTabChange = onActivityTabChange,
                expandedState = expandedState,
                coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
            )
        }
    }

    private fun LazyListScope.content(
        viewModel: AnimeMediaDetailsViewModel,
        editViewModel: MediaEditViewModel,
        viewer: AniListViewer?,
        entry: Entry,
        entry2Result: LoadingResult<Entry2>,
        charactersInitial: ImmutableList<DetailsCharacter>,
        charactersDeferred: () -> LazyPagingItems<DetailsCharacter>,
        staff: LazyPagingItems<DetailsStaff>,
        activityTab: ActivityTab,
        activities: ImmutableList<AnimeMediaDetailsViewModel.ActivityEntry>?,
        onActivityTabChange: (ActivityTab) -> Unit,
        coverImageWidthToHeightRatio: () -> Float,
        expandedState: ExpandedState,
    ) {
        val screenKey = viewModel.screenKey
        if (entry.genres.isNotEmpty()) {
            item("genreSection", "genreSection") {
                GenreSection(genres = entry.genres, mediaType = entry.media.type)
            }
        }

        if (!entry.description?.value.isNullOrEmpty()) {
            item("descriptionSection", "descriptionSection") {
                DescriptionSection(
                    markdownText = entry.description,
                    expanded = remember { { expandedState.description } },
                    onExpandedChange = remember { { expandedState.description = it } },
                )
            }
        }

        charactersSection(
            screenKey = screenKey,
            titleRes = R.string.anime_media_details_characters_label,
            charactersInitial = charactersInitial,
            charactersDeferred = charactersDeferred,
            mediaId = entry.mediaId,
            media = entry.media,
            mediaFavorite = viewModel.favoritesToggleHelper.favorite,
            mediaCoverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
            viewAllContentDescriptionTextRes = R.string.anime_media_details_view_all_content_description,
        )

        relationsSection(
            screenKey = screenKey,
            editViewModel = editViewModel,
            viewer = viewer,
            entry = entry,
            relationsExpanded = expandedState::relations,
            onRelationsExpandedChange = { expandedState.relations = it },
        )

        infoSection(entry)

        songsSection(
            screenKey = screenKey,
            viewModel = viewModel,
            songsExpanded = expandedState::songs,
            onSongsExpandedChange = { expandedState.songs = it },
        )

        cdsSection(
            screenKey = screenKey,
            cdEntries = viewModel.cdEntries,
        )

        staffSection(
            screenKey = screenKey,
            titleRes = R.string.anime_media_details_staff_label,
            staffList = staff,
        )

        val entry2 = entry2Result.result
        if (entry2 != null) {
            statsSection(entry2)
            tagsSection(entry.mediaId, entry2)

            trailerSection(
                entry = entry2,
                playbackPosition = { viewModel.trailerPlaybackPosition },
                onPlaybackPositionUpdate = { viewModel.trailerPlaybackPosition = it },
            )

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
                        text = stringResource(R.string.anime_media_details_error_loading_secondary_data),
                        textAlign = TextAlign.Center,
                    )

                    Button(onClick = viewModel::refreshSecondary) {
                        Text(stringResource(R.string.anime_media_details_error_loading_secondary_data_retry_button))
                    }
                }
            }
        }

        recommendationsSection(
            screenKey = screenKey,
            viewModel = viewModel,
            editViewModel = editViewModel,
            viewer = viewer,
            entry = entry,
            entry2 = entry2,
            coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
            expanded = expandedState::recommendations,
            onExpandedChange = { expandedState.recommendations = it },
        )

        activitiesSection(
            screenKey = screenKey,
            viewer = viewer,
            viewModel = viewModel,
            editViewModel = editViewModel,
            activityTab = activityTab,
            activities = activities,
            onActivityTabChange = onActivityTabChange,
            expanded = expandedState::activities,
            onExpandedChange = { expandedState.activities = it },
            onClickViewAll = {
                it.onMediaActivitiesClick(
                    entry,
                    activityTab == ActivityTab.FOLLOWING,
                    viewModel.favoritesToggleHelper.favorite,
                    coverImageWidthToHeightRatio()
                )
            },
        )

        forumThreadsSection(
            viewer = viewer,
            viewModel = viewModel,
            expanded = expandedState::forumThreads,
            onExpandedChange = { expandedState.forumThreads = it },
            onClickViewAll = {
                it.onForumMediaCategoryClick(entry.media.title?.userPreferred, entry.mediaId)
            },
            onStatusUpdate = viewModel.threadToggleHelper::toggle,
        )

        reviewsSection(
            viewModel = viewModel,
            entry = entry,
            entry2 = entry2,
            coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
            expanded = expandedState::reviews,
            onExpandedChange = { expandedState.reviews = it },
        )
    }

    @Composable
    private fun LazyItemScope.GenreSection(
        genres: ImmutableList<Entry.Genre>,
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
                .animateItemPlacement()
                .recomposeHighlighter()
        ) {
            genres.forEach {
                AssistChip(
                    onClick = {
                        navigationCallback.onGenreClick(
                            mediaType ?: MediaType.ANIME,
                            it.name
                        )
                    },
                    onLongClickLabel = stringResource(
                        R.string.anime_media_tag_long_click_content_description
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
        screenKey: String,
        editViewModel: MediaEditViewModel,
        viewer: AniListViewer?,
        entry: Entry,
        relationsExpanded: () -> Boolean,
        onRelationsExpandedChange: (Boolean) -> Unit,
    ) {
        mediaListSection(
            screenKey = screenKey,
            editViewModel = editViewModel,
            viewer = viewer,
            titleRes = R.string.anime_media_details_relations_label,
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
                stringResource(R.string.anime_media_details_information_label),
                modifier = Modifier.animateItemPlacement()
            )
        }

        val media = entry.media

        item("infoSectionOne") {
            ElevatedCard(
                modifier = Modifier
                    .animateContentSize()
                    .animateItemPlacement()
                    .padding(start = 16.dp, end = 16.dp, bottom = 2.dp)
            ) {
                twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_format_label),
                    bodyOne = stringResource(entry.formatTextRes),
                    labelTwo = stringResource(R.string.anime_media_details_status_label),
                    bodyTwo = stringResource(entry.statusTextRes),
                    showDividerAbove = false,
                )

                twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_episodes_label),
                    bodyOne = media.episodes?.toString(),
                    labelTwo = stringResource(R.string.anime_media_details_duration_label),
                    bodyTwo = media.duration?.let {
                        stringResource(R.string.anime_media_details_duration_minutes, it)
                    },
                )

                twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_volumes_label),
                    bodyOne = media.volumes?.toString(),
                    labelTwo = stringResource(R.string.anime_media_details_chapters_label),
                    bodyTwo = media.chapters?.toString(),
                )

                twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_source_label),
                    bodyOne = stringResource(media.source.toTextRes()),
                    labelTwo = stringResource(R.string.anime_media_details_season_label),
                    bodyTwo = MediaUtils.formatSeasonYear(media.season, media.seasonYear),
                )

                val context = LocalContext.current

                val startDateFormatted = media.startDate?.let {
                    remember { MediaUtils.formatDateTime(context, it.year, it.month, it.day) }
                }
                val endDateFormatted = media.endDate?.let {
                    remember { MediaUtils.formatDateTime(context, it.year, it.month, it.day) }
                }

                twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_start_date_label),
                    bodyOne = startDateFormatted,
                    labelTwo = stringResource(R.string.anime_media_details_end_date_label),
                    bodyTwo = endDateFormatted,
                )
            }
        }

        item("infoSectionTwo") {
            ElevatedCard(
                modifier = Modifier
                    .animateContentSize()
                    .animateItemPlacement()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 2.dp)
            ) {
                var shown = twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_average_score_label),
                    bodyOne = media.averageScore?.toString(),
                    labelTwo = stringResource(R.string.anime_media_details_mean_score_label),
                    bodyTwo = media.meanScore?.toString(),
                    showDividerAbove = false,
                )

                shown = twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_popularity_label),
                    bodyOne = media.popularity?.toString(),
                    labelTwo = stringResource(R.string.anime_media_details_favorites_label),
                    bodyTwo = media.favourites?.toString(),
                    showDividerAbove = shown,
                ) || shown

                twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_trending_label),
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
                    .animateItemPlacement(),
            ) {
                var shown = twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_licensed_label),
                    bodyOne = entry.licensedTextRes?.let { stringResource(it) },
                    labelTwo = stringResource(R.string.anime_media_details_country_label),
                    bodyTwo = entry.country,
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
                            label = stringResource(R.string.anime_media_details_hashtags_label),
                            body = media.hashtag!!,
                            showDividerAbove = shown,
                        )
                    }

                    shown = true
                }

                // TODO: isFavorite, isAdult, airingSchedule, reviews, trends

                shown = expandableListInfoText(
                    labelTextRes = R.string.anime_media_details_studios_label,
                    contentDescriptionTextRes = R.string.anime_media_details_studios_expand_content_description,
                    values = entry.studios,
                    valueToText = { it.name },
                    onClick = { uriHandler.openUri(AniListUtils.studioUrl(it.id)) },
                    showDividerAbove = shown,
                ) || shown

                expandableListInfoText(
                    labelTextRes = R.string.anime_media_details_synonyms_label,
                    contentDescriptionTextRes = R.string.anime_media_details_synonyms_expand_content_description,
                    values = entry.allSynonyms,
                    valueToText = { it },
                    showDividerAbove = shown,
                )
            }
        }
    }

    private fun LazyListScope.songsSection(
        screenKey: String,
        viewModel: AnimeMediaDetailsViewModel,
        songsExpanded: () -> Boolean,
        onSongsExpandedChange: (Boolean) -> Unit,
    ) {
        val animeSongs = viewModel.animeSongs ?: return
        listSection(
            titleRes = R.string.anime_media_details_songs_label,
            values = animeSongs.entries,
            valueToId = { it.id },
            aboveFold = SONGS_ABOVE_FOLD,
            expanded = songsExpanded,
            onExpandedChange = onSongsExpandedChange,
        ) { item, paddingBottom, modifier ->
            AnimeThemeRow(
                screenKey,
                viewModel = viewModel,
                entry = item,
                modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
            )
        }
    }

    @Composable
    private fun AnimeThemeRow(
        screenKey: String,
        viewModel: AnimeMediaDetailsViewModel,
        entry: AnimeSongEntry,
        modifier: Modifier = Modifier,
    ) {
        val state = viewModel.getAnimeSongState(entry.id)
        val mediaPlayer = viewModel.mediaPlayer
        val playingState by mediaPlayer.playingState.collectAsState()
        val active by remember {
            derivedStateOf { playingState.first == entry.id }
        }
        val playing = active && playingState.second

        ElevatedCard(
            modifier = Modifier
                .animateContentSize()
                .then(modifier),
        ) {
            var hidden by remember { mutableStateOf(entry.spoiler && !active) }
            if (hidden) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { hidden = false }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = stringResource(
                            R.string.anime_media_details_song_spoiler_content_description
                        ),
                    )

                    Text(
                        text = stringResource(R.string.anime_media_details_song_spoiler),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f)
                    )
                }
            } else {
                // TODO: This doesn't line up perfectly (too much space between label and title),
                //  consider migrating to ConstraintLayout
                Column(
                    modifier = Modifier.clickable {
                        viewModel.onAnimeSongExpandedToggle(entry.id, !state.expanded())
                    },
                ) {
                    Row {
                        if (entry.spoiler) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = stringResource(
                                    R.string.anime_media_details_song_spoiler_content_description
                                ),
                                modifier = Modifier
                                    .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        }

                        val labelText = when (entry.type) {
                            AnimeSongEntry.Type.OP -> if (entry.episodes.isNullOrBlank()) {
                                stringResource(R.string.anime_media_details_song_opening)
                            } else {
                                stringResource(
                                    R.string.anime_media_details_song_opening_episodes,
                                    entry.episodes,
                                )
                            }
                            AnimeSongEntry.Type.ED -> if (entry.episodes.isNullOrBlank()) {
                                stringResource(R.string.anime_media_details_song_ending)
                            } else {
                                stringResource(
                                    R.string.anime_media_details_song_ending_episodes,
                                    entry.episodes,
                                )
                            }
                            null -> null
                        }

                        Text(
                            text = labelText.orEmpty(),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.surfaceTint,
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(Alignment.CenterVertically)
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        if (entry.videoUrl != null || entry.audioUrl != null) {
                            if (!state.expanded()) {
                                IconButton(
                                    onClick = { viewModel.onAnimeSongPlayAudioClick(entry.id) },
                                ) {
                                    if (playing) {
                                        Icon(
                                            imageVector = Icons.Filled.PauseCircleOutline,
                                            contentDescription = stringResource(
                                                R.string.anime_media_details_song_pause_content_description
                                            ),
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.PlayCircleOutline,
                                            contentDescription = stringResource(
                                                R.string.anime_media_details_song_play_content_description
                                            ),
                                        )
                                    }
                                }
                            }

                            if (entry.videoUrl != null) {
                                TrailingDropdownIconButton(
                                    expanded = state.expanded(),
                                    contentDescription = stringResource(
                                        R.string.anime_media_details_song_expand_content_description
                                    ),
                                    onClick = {
                                        viewModel.onAnimeSongExpandedToggle(
                                            entry.id,
                                            !state.expanded(),
                                        )
                                    },
                                )
                            }
                        }
                    }

                    if (state.expanded()) {
                        Box {
                            var linkButtonVisible by remember { mutableStateOf(true) }
                            AndroidView(
                                factory = {
                                    @Suppress("UnsafeOptInUsageError")
                                    PlayerView(it).apply {
                                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                                        setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                                        setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE)
                                        setControllerVisibilityListener(
                                            PlayerView.ControllerVisibilityListener {
                                                linkButtonVisible = it == View.VISIBLE
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .heightIn(min = 180.dp),
                                update = { it.player = mediaPlayer.player },
                                onReset = { it.player = null },
                                onRelease = { it.player = null },
                            )

                            val uriHandler = LocalUriHandler.current
                            val alpha by animateFloatAsState(
                                targetValue = if (linkButtonVisible) 1f else 0f,
                                label = "Song open link button alpha",
                            )
                            IconButton(
                                onClick = { uriHandler.openUri(entry.link!!) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .alpha(alpha)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.OpenInBrowser,
                                    contentDescription = stringResource(
                                        R.string.anime_media_details_song_open_link_content_description
                                    ),
                                )
                            }
                        }
                    } else if (active) {
                        val progress = mediaPlayer.progress
                        Slider(
                            value = progress,
                            onValueChange = { viewModel.onAnimeSongProgressUpdate(entry.id, it) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }

                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = if (state.expanded()) 10.dp else 0.dp,
                                bottom = 10.dp
                            )
                    )
                }
            }

            if (!hidden) {
                val uriHandler = LocalUriHandler.current
                val artists = entry.artists
                if (artists.isNotEmpty()) {
                    artists.forEachIndexed { index, artist ->
                        val isLast = index == artists.lastIndex
                        Divider()
                        Row(
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .clickable { uriHandler.openUri(artist.link) }
                        ) {
                            val artistImage = artist.image
                            val characterImage = artist.character?.image

                            @Composable
                            fun ArtistImage(modifier: Modifier) {
                                val image = @Composable {
                                    AsyncImage(
                                        model = artistImage,
                                        contentScale = ContentScale.FillHeight,
                                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                                        contentDescription = stringResource(
                                            if (artist.asCharacter) {
                                                R.string.anime_media_voice_actor_image
                                            } else {
                                                R.string.anime_media_artist_image
                                            }
                                        ),
                                        modifier = modifier
                                            .sizeIn(minWidth = 44.dp, minHeight = 64.dp)
                                            .fillMaxHeight()
                                    )
                                }
                                if (artist.aniListId != null) {
                                    SharedElement(
                                        key = "anime_staff_${artist.aniListId}_image",
                                        screenKey = screenKey
                                    ) {
                                        image()
                                    }
                                } else {
                                    image()
                                }
                            }

                            @Composable
                            fun CharacterImage(modifier: Modifier) {
                                val image = @Composable { modifier: Modifier ->
                                    AsyncImage(
                                        model = characterImage!!,
                                        contentScale = ContentScale.FillHeight,
                                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                                        contentDescription = stringResource(
                                            R.string.anime_character_image_content_description
                                        ),
                                        modifier = modifier
                                            .sizeIn(minWidth = 44.dp, minHeight = 64.dp)
                                            .fillMaxHeight()
                                    )
                                }
                                if (artist.character?.aniListId != null) {
                                    SharedElement(
                                        key = "anime_character_${artist.character.aniListId}_image",
                                        screenKey = screenKey
                                    ) {
                                        image(modifier.clickable {
                                            // TODO: Use navigation callback
                                            uriHandler.openUri(artist.character.link)
                                        })
                                    }
                                } else {
                                    image(modifier)
                                }
                            }

                            val firstImage: (@Composable (modifier: Modifier) -> Unit)?
                            val secondImage: (@Composable (modifier: Modifier) -> Unit)?

                            val asCharacter = artist.asCharacter
                            if (asCharacter) {
                                if (characterImage == null) {
                                    if (artistImage == null) {
                                        firstImage = null
                                        secondImage = null
                                    } else {
                                        firstImage = { ArtistImage(it) }
                                        secondImage = null
                                    }
                                } else {
                                    firstImage = { CharacterImage(it) }
                                    secondImage = { ArtistImage(it) }
                                }
                            } else {
                                firstImage = { ArtistImage(it) }
                                secondImage = characterImage?.let { { CharacterImage(it) } }
                            }

                            if (firstImage == null) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(width = 44.dp, height = 64.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = stringResource(
                                            R.string.anime_media_artist_no_image
                                        ),
                                    )
                                }
                            } else {
                                firstImage(Modifier.conditionally(isLast) {
                                    clip(RoundedCornerShape(bottomStart = 12.dp))
                                })
                            }

                            val artistText = if (artist.character == null) {
                                artist.name
                            } else if (artist.asCharacter) {
                                stringResource(
                                    R.string.anime_media_details_song_artist_as_character,
                                    artist.character.name(),
                                    artist.name,
                                )
                            } else {
                                stringResource(
                                    R.string.anime_media_details_song_artist_with_character,
                                    artist.name,
                                    artist.character.name(),
                                )
                            }

                            Text(
                                text = artistText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterVertically)
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            )

                            if (secondImage != null) {
                                secondImage(Modifier.conditionally(isLast) {
                                    clip(RoundedCornerShape(bottomEnd = 12.dp))
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    private fun LazyListScope.cdsSection(
        screenKey: String,
        cdEntries: List<CdEntryGridModel>,
    ) {
        if (cdEntries.isEmpty()) return

        item("cdsHeader") {
            DetailsSectionHeader(
                stringResource(R.string.anime_media_details_cds_label),
                modifier = Modifier.animateItemPlacement()
            )
        }

        item("cdsSection") {
            val width = LocalDensity.current.run { Dimension.Pixels(200.dp.toPx().roundToInt()) }
            val navigationCallback = LocalNavigationCallback.current
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.animateItemPlacement(),
            ) {
                itemsIndexed(cdEntries) { index, cdEntry ->
                    var transitionProgress by remember { mutableFloatStateOf(0f) }
                    val cornerDp = lerp(12.dp, 0.dp, transitionProgress)
                    ElevatedCard(
                        shape = RoundedCornerShape(cornerDp),
                    ) {
                        EntryGrid.Entry(
                            imageScreenKey = screenKey,
                            expectedWidth = width,
                            index = index,
                            entry = cdEntry,
                            onClickEntry = { _, entry ->
                                navigationCallback.onCdEntryClick(
                                    model = entry,
                                    imageCornerDp = 12.dp,
                                )
                            },
                            onSharedElementFractionChanged = { transitionProgress = it }
                        )
                    }
                }
            }
        }
    }

    private fun LazyListScope.recommendationsSection(
        screenKey: String,
        viewModel: AnimeMediaDetailsViewModel,
        editViewModel: MediaEditViewModel,
        viewer: AniListViewer?,
        entry: Entry,
        entry2: Entry2?,
        coverImageWidthToHeightRatio: () -> Float,
        expanded: () -> Boolean,
        onExpandedChange: (Boolean) -> Unit,
    ) {
        listSection(
            titleRes = R.string.anime_media_details_recommendations_label,
            values = entry2?.recommendations,
            valueToId = { "anime_media_${it.entry.media.id}" },
            aboveFold = RECOMMENDATIONS_ABOVE_FOLD,
            hasMoreValues = entry2?.recommendationsHasMore ?: false,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            onClickViewAll = {
                it.onMediaRecommendationsClick(
                    entry,
                    viewModel.favoritesToggleHelper.favorite,
                    coverImageWidthToHeightRatio()
                )
            },
            viewAllContentDescriptionTextRes = R.string.anime_media_details_view_all_content_description,
        ) { item, paddingBottom, modifier ->
            val entry = item.entry
            AnimeMediaListRow(
                screenKey = screenKey,
                entry = entry,
                viewer = viewer,
                onClickListEdit = { editViewModel.initialize(it.media) },
                recommendation = item.data,
                onUserRecommendationRating = viewModel.recommendationToggleHelper::toggle,
                modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
            )
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
                stringResource(R.string.anime_media_details_stats_label),
                modifier = Modifier.animateItemPlacement()
            )
        }

        item("statsSection") {
            ElevatedCard(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 2.dp)
                    .animateItemPlacement()
            ) {
                expandableListInfoText(
                    labelTextRes = R.string.anime_media_details_rankings_label,
                    contentDescriptionTextRes = R.string.anime_media_details_rankings_expand_content_description,
                    values = entry.rankings,
                    valueToText = {
                        when (it.type) {
                            MediaRankType.RATED -> MediaUtils.formatRanking(
                                ranking = it,
                                seasonYearTextRes = R.string.anime_media_details_ranking_rated_season_year,
                                yearTextRes = R.string.anime_media_details_ranking_rated_year,
                                allTimeTextRes = R.string.anime_media_details_ranking_rated_all_time,
                            )
                            MediaRankType.POPULAR -> MediaUtils.formatRanking(
                                ranking = it,
                                seasonYearTextRes = R.string.anime_media_details_ranking_popular_season_year,
                                yearTextRes = R.string.anime_media_details_ranking_popular_year,
                                allTimeTextRes = R.string.anime_media_details_ranking_popular_all_time,
                            )
                            MediaRankType.UNKNOWN__ -> stringResource(
                                R.string.anime_media_details_ranking_unknown,
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

                    Divider()
                    DetailsSubsectionHeader(
                        stringResource(R.string.anime_media_details_status_distribution_label)
                    )

                    PieChart(
                        slices = statusDistribution,
                        sliceToKey = { it.status },
                        sliceToAmount = { it.amount ?: 0 },
                        sliceToColor = { it.status.toColor() },
                        sliceToText = { slice ->
                            when (slice.status) {
                                MediaListStatus.CURRENT -> if (entry.media.type == MediaType.ANIME) {
                                    R.string.anime_media_details_status_distribution_current_anime
                                } else {
                                    R.string.anime_media_details_status_distribution_current_manga
                                }
                                MediaListStatus.PLANNING -> R.string.anime_media_details_status_distribution_planning
                                MediaListStatus.COMPLETED -> R.string.anime_media_details_status_distribution_completed
                                MediaListStatus.DROPPED -> R.string.anime_media_details_status_distribution_dropped
                                MediaListStatus.PAUSED -> R.string.anime_media_details_status_distribution_paused
                                MediaListStatus.REPEATING -> R.string.anime_media_details_status_distribution_repeating
                                MediaListStatus.UNKNOWN__, null -> R.string.anime_media_details_status_distribution_unknown
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
                    Divider()
                    DetailsSubsectionHeader(
                        stringResource(R.string.anime_media_details_score_distribution_label)
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

    private fun LazyListScope.tagsSection(mediaId: String, entry: Entry2) {
        if (entry.tags.isNotEmpty()) {
            item("tagsHeader") {
                DetailsSectionHeader(
                    stringResource(R.string.anime_media_details_tags_label),
                    modifier = Modifier.animateItemPlacement()
                )
            }

            item("tagsSection") {
                val navigationCallback = LocalNavigationCallback.current
                val colorCalculationState = LocalColorCalculationState.current
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateItemPlacement()
                ) {
                    entry.tags.forEach {
                        val (containerColor, textColor) =
                            colorCalculationState.getColors(mediaId)
                        AnimeMediaTagEntry.Chip(
                            tag = it,
                            title = {
                                if (it.rank == null) {
                                    it.name
                                } else {
                                    stringResource(
                                        R.string.anime_media_details_tag_with_rank_format,
                                        it.name,
                                        it.rank
                                    )
                                }
                            },
                            onTagClick = { id, name ->
                                navigationCallback.onTagClick(
                                    entry.media.type ?: MediaType.ANIME,
                                    id,
                                    name,
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
        playbackPosition: () -> Float,
        onPlaybackPositionUpdate: (Float) -> Unit,
    ) {
        val trailer = entry.media.trailer ?: return
        if (trailer.site != "youtube" && trailer.site != "dailymotion") return

        val videoId = trailer.id ?: return

        item("trailerHeader") {
            DetailsSectionHeader(
                stringResource(R.string.anime_media_details_trailer_label),
                modifier = Modifier.animateItemPlacement()
            )
        }

        if (trailer.site == "youtube") {
            item("trailerSection") {
                val lifecycleOwner = LocalLifecycleOwner.current
                ElevatedCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateItemPlacement()
                ) {
                    val player = remember { AtomicReference<YouTubePlayer>(null) }
                    AndroidView(
                        factory = {
                            YouTubePlayerView(it).apply {
                                lifecycleOwner.lifecycle.addObserver(this)
                                getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                                        player.set(youTubePlayer)
                                        youTubePlayer.cueVideo(videoId, playbackPosition())
                                        youTubePlayer.addListener(object :
                                            AbstractYouTubePlayerListener() {
                                            override fun onCurrentSecond(
                                                youTubePlayer: YouTubePlayer,
                                                second: Float,
                                            ) {
                                                onPlaybackPositionUpdate(second)
                                            }
                                        })
                                    }
                                })
                            }
                        },
                        onRelease = {
                            lifecycleOwner.lifecycle.removeObserver(it)
                            it.release()
                        },
                        update = {
                            lifecycleOwner.lifecycle.addObserver(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f)
                    )
                }
            }
        } else {
            item("trailerSection") {
                val uriHandler = LocalUriHandler.current
                ElevatedCard(
                    onClick = { uriHandler.openUri(MediaUtils.dailymotionUrl(videoId)) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateItemPlacement()
                ) {
                    AsyncImage(
                        model = trailer.thumbnail,
                        contentDescription = stringResource(
                            R.string.anime_media_details_trailer_dailymotion_content_description
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
            titleRes = R.string.anime_media_details_streaming_episodes_label,
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
                                R.string.anime_media_details_streaming_episode_spoiler_content_description
                            ),
                            modifier = Modifier
                                .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                        )

                        Text(
                            text = stringResource(
                                R.string.anime_media_details_streaming_episode_spoiler
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .weight(1f)
                        )
                    }
                }
            }
        ) { item, paddingBottom, modifier ->
            val uriHandler = LocalUriHandler.current
            ElevatedCard(
                modifier = modifier
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
                            R.string.anime_media_details_streaming_episode_content_description
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
                                    R.string.anime_media_details_streaming_episode_icon_content_description
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
        linksSection(R.string.anime_media_details_social_links_label, entry.socialLinks)
    }

    private fun LazyListScope.streamingLinksSection(entry: Entry2) {
        linksSection(R.string.anime_media_details_streaming_links_label, entry.streamingLinks)
    }

    private fun LazyListScope.otherLinksSection(entry: Entry2) {
        linksSection(R.string.anime_media_details_other_links_label, entry.otherLinks)
    }

    private fun LazyListScope.linksSection(@StringRes headerRes: Int, links: List<Entry2.Link>) {
        if (links.isEmpty()) return

        item("linksHeader-$headerRes") {
            DetailsSectionHeader(
                stringResource(headerRes),
                modifier = Modifier.animateItemPlacement()
            )
        }

        item("linksSection-$headerRes") {
            val uriHandler = LocalUriHandler.current
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .animateItemPlacement()
            ) {
                links.forEach {
                    androidx.compose.material3.AssistChip(
                        leadingIcon = if (it.icon == null) null else {
                            {
                                AsyncImage(
                                    model = it.icon,
                                    contentDescription = stringResource(
                                        R.string.anime_media_details_link_content_description,
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

    private fun LazyListScope.activitiesSection(
        screenKey: String,
        viewer: AniListViewer?,
        editViewModel: MediaEditViewModel,
        activityTab: ActivityTab,
        activities: ImmutableList<AnimeMediaDetailsViewModel.ActivityEntry>?,
        onActivityTabChange: (ActivityTab) -> Unit,
        viewModel: AnimeMediaDetailsViewModel,
        expanded: () -> Boolean,
        onExpandedChange: (Boolean) -> Unit,
        onClickViewAll: (AnimeNavigator.NavigationCallback) -> Unit,
    ) {
        item("activitiesHeader") {
            val navigationCallback = LocalNavigationCallback.current
            DetailsSectionHeader(
                text = stringResource(R.string.anime_media_details_activities_label),
                modifier = Modifier.clickable { onClickViewAll(navigationCallback) },
                onClickViewAll = { onClickViewAll(navigationCallback) },
                viewAllContentDescriptionTextRes = R.string.anime_media_details_view_all_content_description
            )
        }

        if (viewer != null) {
            item("activitiesTabHeader") {
                TabRow(
                    selectedTabIndex = if (activityTab == ActivityTab.FOLLOWING) 0 else 1,
                    modifier = Modifier
                        .padding(bottom = if (activities.isNullOrEmpty()) 0.dp else 16.dp)
                        .fillMaxWidth()
                ) {
                    Tab(
                        selected = activityTab == ActivityTab.FOLLOWING,
                        onClick = { onActivityTabChange(ActivityTab.FOLLOWING) },
                        text = {
                            Text(stringResource(R.string.anime_media_details_activity_following))
                        },
                    )
                    Tab(
                        selected = activityTab == ActivityTab.GLOBAL,
                        onClick = { onActivityTabChange(ActivityTab.GLOBAL) },
                        text = {
                            Text(stringResource(R.string.anime_media_details_activity_global))
                        },
                    )
                }
            }
        }

        listSectionWithoutHeader(
            titleRes = R.string.anime_media_details_activities_label,
            values = activities,
            valueToId = { it.activityId },
            aboveFold = ACTIVITIES_ABOVE_FOLD,
            hasMoreValues = true,
            noResultsTextRes = R.string.anime_media_details_activities_no_results,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            onClickViewAll = onClickViewAll,
        ) { item, paddingBottom, modifier ->
            ListActivitySmallCard(
                screenKey = screenKey,
                viewer = viewer,
                activity = item.activity,
                entry = item,
                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                onClickListEdit = { editViewModel.initialize(it.media) },
                clickable = true,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
            )
        }
    }

    private fun LazyListScope.forumThreadsSection(
        viewer: AniListViewer?,
        viewModel: AnimeMediaDetailsViewModel,
        expanded: () -> Boolean,
        onExpandedChange: (Boolean) -> Unit,
        onClickViewAll: (AnimeNavigator.NavigationCallback) -> Unit,
        onStatusUpdate: (ForumThreadToggleUpdate) -> Unit,
    ) {
        listSection(
            titleRes = R.string.anime_media_details_forum_threads_label,
            values = viewModel.forumThreads,
            valueToId = { it.thread.id.toString() },
            aboveFold = FORUM_THREADS_ABOVE_FOLD,
            hasMoreValues = true,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            onClickViewAll = onClickViewAll,
            viewAllContentDescriptionTextRes = R.string.anime_media_details_view_all_content_description,
        ) { item, paddingBottom, modifier ->
            ThreadSmallCard(
                viewer = viewer,
                entry = item,
                onStatusUpdate = onStatusUpdate,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
            )
        }
    }

    private fun LazyListScope.reviewsSection(
        viewModel: AnimeMediaDetailsViewModel,
        entry: Entry,
        entry2: Entry2?,
        coverImageWidthToHeightRatio: () -> Float,
        expanded: () -> Boolean,
        onExpandedChange: (Boolean) -> Unit,
    ) {
        val reviews = entry2?.reviews
        if (reviews != null && reviews.isEmpty()) return
        listSection(
            titleRes = R.string.anime_media_details_reviews_label,
            values = reviews,
            valueToId = { it.id.toString() },
            aboveFold = REVIEWS_ABOVE_FOLD,
            hasMoreValues = entry2?.reviewsHasMore ?: false,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            onClickViewAll = {
                it.onMediaReviewsClick(
                    entry,
                    viewModel.favoritesToggleHelper.favorite,
                    coverImageWidthToHeightRatio(),
                )
            },
            viewAllContentDescriptionTextRes = R.string.anime_media_details_view_all_content_description,
        ) { item, paddingBottom, modifier ->
            val navigationCallback = LocalNavigationCallback.current
            ReviewSmallCard(
                screenKey = viewModel.screenKey,
                review = item,
                onClick = {
                    navigationCallback.onReviewClick(
                        reviewId = item.id.toString(),
                        media = entry.media,
                        viewModel.favoritesToggleHelper.favorite,
                        imageWidthToHeightRatio = coverImageWidthToHeightRatio()
                    )
                },
                modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
            )
        }
    }

    @Stable
    data class Entry(
        val mediaId: String,
        val media: Media,
        val relations: ImmutableList<Relation>,
        val description: StableSpanned?,
    ) {
        val charactersInitial =
            CharacterUtils.toDetailsCharacters(media.characters?.edges?.filterNotNull().orEmpty())
                .toImmutableList()

        val id = EntryId("media", mediaId)
        val titlesUnique = media.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()

        val formatTextRes = media.format.toTextRes()
        val statusTextRes = media.status.toTextRes()
        val licensedTextRes = media.isLicensed
            ?.let { if (it) UtilsStringR.yes else UtilsStringR.no }
        val country = CountryCode.getByAlpha2Code(media.countryOfOrigin.toString())?.getName()
        val hashtags = media.hashtag?.split("#")
            ?.filter { it.isNotEmpty() }
            ?.map { "#${it.trim()}" }

        val allSynonyms = listOfNotNull(
            media.title?.userPreferred,
            media.title?.romaji,
            media.title?.english,
            media.title?.native,
        ).distinct() + media.synonyms?.filterNotNull().orEmpty()

        val genres = media.genres?.filterNotNull().orEmpty().map(::Genre).toImmutableList()

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
        val recommendations: ImmutableList<Recommendation>,
    ) {
        val recommendationsHasMore = media.recommendations?.pageInfo?.hasNextPage ?: true

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

        val reviews = media.reviews?.nodes?.filterNotNull()?.toImmutableList().orEmpty()
        val reviewsHasMore = media.reviews?.pageInfo?.hasNextPage ?: true

        val streamingEpisodes =
            media.streamingEpisodes?.filterNotNull()?.toImmutableList().orEmpty()

        data class Recommendation(
            val id: String,
            val data: RecommendationData,
            val entry: MediaPreviewEntry,
        )

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
        staff: LazyPagingItems<DetailsStaff>,
        expandedState: ExpandedState,
        animeSongs: AnimeMediaDetailsViewModel.AnimeSongs?,
        cdEntries: List<CdEntryGridModel>,
        viewer: AniListViewer?,
        activities: List<AnimeMediaDetailsViewModel.ActivityEntry>?,
        forumThreads: List<ForumThreadEntry>?,
    ) = remember(
        entry,
        entry2,
        charactersCount,
        staff.itemCount,
        expandedState.allValues(),
        animeSongs,
        cdEntries,
        viewer,
        activities,
        forumThreads,
    ) {
        if (entry == null) return@remember SectionIndexInfo(emptyList())
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

        if (animeSongs != null && animeSongs.entries.isNotEmpty()) {
            list += SectionIndexInfo.Section.SONGS to currentIndex
            runListSection(
                size = animeSongs.entries.size,
                aboveFold = SONGS_ABOVE_FOLD,
                expanded = expandedState.songs,
                hasMore = false,
            )
        }

        if (cdEntries.isNotEmpty()) {
            list += SectionIndexInfo.Section.CDS to currentIndex
            currentIndex += 2
        }

        if (staff.itemCount > 0) {
            list += SectionIndexInfo.Section.STAFF to currentIndex
            currentIndex += 2
        }

        list += SectionIndexInfo.Section.STATS to currentIndex
        currentIndex += 2

        if (entry2 == null) {
            currentIndex++
        } else {
            // TODO: If entry2 values are null, show/mock header
            if (entry2.tags.isNotEmpty()) {
                list += SectionIndexInfo.Section.TAGS to currentIndex
                currentIndex += 2
            }

            val trailer = entry2.media?.trailer
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
        }

        val recommendations = entry2?.recommendations
        if (!recommendations.isNullOrEmpty()) {
            list += SectionIndexInfo.Section.RECOMMENDATIONS to currentIndex
            runListSection(
                size = recommendations.size,
                aboveFold = RECOMMENDATIONS_ABOVE_FOLD,
                expanded = expandedState.recommendations,
                hasMore = entry2.recommendationsHasMore,
            )
        }

        if (!activities.isNullOrEmpty()) {
            list += SectionIndexInfo.Section.ACTIVITIES to currentIndex
            // Add one for tab row, only shown if there's a viewer
            if (viewer != null) {
                currentIndex++
            }
            runListSection(
                size = activities.size,
                aboveFold = ACTIVITIES_ABOVE_FOLD,
                expanded = expandedState.activities,
                hasMore = true,
            )
        }

        if (!forumThreads.isNullOrEmpty()) {
            list += SectionIndexInfo.Section.FORUM_THREADS to currentIndex
            runListSection(
                size = forumThreads.size,
                aboveFold = FORUM_THREADS_ABOVE_FOLD,
                expanded = expandedState.forumThreads,
                hasMore = true,
            )
        }

        val reviews = entry2?.reviews
        if (!reviews.isNullOrEmpty()) {
            list += SectionIndexInfo.Section.REVIEWS to currentIndex
            runListSection(
                size = reviews.size,
                aboveFold = REVIEWS_ABOVE_FOLD,
                expanded = expandedState.reviews,
                hasMore = entry2.reviewsHasMore,
            )
        }

        SectionIndexInfo(list)
    }

    private data class SectionIndexInfo(val sections: List<Pair<Section, Int>>) {

        enum class Section(@StringRes val titleRes: Int) {
            CHARACTERS(R.string.anime_media_details_characters_label),
            RELATIONS(R.string.anime_media_details_relations_label),
            INFO(R.string.anime_media_details_information_label),
            SONGS(R.string.anime_media_details_songs_label),
            CDS(R.string.anime_media_details_cds_label),
            STAFF(R.string.anime_media_details_staff_label),
            STATS(R.string.anime_media_details_stats_label),
            TAGS(R.string.anime_media_details_tags_label),
            TRAILER(R.string.anime_media_details_trailer_label),
            EPISODES(R.string.anime_media_details_episodes_label),
            LINKS(R.string.anime_media_details_links_label),
            RECOMMENDATIONS(R.string.anime_media_details_recommendations_label),
            REVIEWS(R.string.anime_media_details_reviews_label),
            ACTIVITIES(R.string.anime_media_details_activities_label),
            FORUM_THREADS(R.string.anime_media_details_forum_threads_label),
        }
    }

    private enum class ActivityTab {
        FOLLOWING, GLOBAL
    }
}
