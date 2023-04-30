package com.thekeeperofpie.artistalleydatabase.anime.media.details

import android.graphics.drawable.BitmapDrawable
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.ColorUtils
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.MediaDetailsQuery.Data.Media
import com.anilist.fragment.MediaDetailsListEntry
import com.anilist.type.ExternalLinkType
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaRankType
import com.anilist.type.MediaRelation
import com.anilist.type.MediaType
import com.anilist.type.ScoreFormat
import com.neovisionaries.i18n.CountryCode
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.thekeeperofpie.artistalleydatabase.android_utils.AnimationUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.AppMediaPlayer
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toColor
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.AnimeMediaEditBottomSheet
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditData
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeTheme
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.AccelerateEasing
import com.thekeeperofpie.artistalleydatabase.compose.AssistChip
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.assistChipColors
import com.thekeeperofpie.artistalleydatabase.compose.multiplyCoerceSaturation
import com.thekeeperofpie.artistalleydatabase.compose.optionalClickable
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid
import de.charlex.compose.HtmlText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt

@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeMediaDetailsScreen {

    private const val RELATIONS_ABOVE_FOLD = 3
    private const val RECOMMENDATIONS_ABOVE_FOLD = 5
    private const val SONGS_ABOVE_FOLD = 3
    private const val STREAMING_EPISODES_ABOVE_FOLD = 3

    // Sorted by most relevant for an anime-first viewer
    private val RELATION_SORT_ORDER = listOf(
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
        onClickBack: () -> Unit = {},
        loading: @Composable () -> Boolean = { false },
        color: () -> Color? = { Color.Transparent },
        coverImage: @Composable () -> String? = { null },
        bannerImage: @Composable () -> String? = { null },
        title: @Composable () -> String = { "Title" },
        subtitle: @Composable () -> String = { "TV - Releasing - 2023" },
        nextEpisode: @Composable () -> Int? = { null },
        nextEpisodeAiringAt: @Composable () -> Int? = { null },
        entry: @Composable () -> Entry? = { null },
        mediaPlayer: @Composable () -> AppMediaPlayer,
        animeSongs: @Composable () -> AnimeMediaDetailsViewModel.AnimeSongs? = { null },
        animeSongState: (animeSongId: String) -> AnimeMediaDetailsViewModel.AnimeSongState,
        onAnimeSongPlayClick: (animeSongId: String) -> Unit = {},
        onAnimeSongProgressUpdate: (animeSongId: String, Float) -> Unit,
        onAnimeSongExpandedToggle: (animeSongId: String, expanded: Boolean) -> Unit,
        cdEntries: @Composable () -> List<CdEntryGridModel> = { emptyList() },
        onGenreClicked: (String) -> Unit = {},
        onGenreLongClicked: (String) -> Unit = {},
        onCharacterClicked: (String) -> Unit = {},
        onCharacterLongClicked: (String) -> Unit = {},
        onStaffClicked: (String) -> Unit = {},
        onStaffLongClicked: (String) -> Unit = {},
        onTagClicked: (tagId: String, tagName: String) -> Unit = { _, _ -> },
        onTagLongClicked: (String) -> Unit = {},
        onMediaClicked: (AnimeMediaListRow.Entry) -> Unit = {},
        trailerPlaybackPosition: () -> Float,
        onTrailerPlaybackPositionUpdate: (Float) -> Unit,
        listEntry: @Composable () -> MediaDetailsListEntry?,
        editData: MediaEditData,
        scoreFormat: @Composable () -> ScoreFormat,
        onDateChange: (start: Boolean, Long?) -> Unit,
        onClickDelete: () -> Unit,
        onClickSave: () -> Unit,
        errorRes: @Composable () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = {},
    ) {
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Hidden,
                skipHiddenState = false,
            )
        )

        val bottomSheetShowing = editData.showing
        LaunchedEffect(bottomSheetShowing) {
            launch {
                if (bottomSheetShowing) {
                    bottomSheetScaffoldState.bottomSheetState.expand()
                } else {
                    bottomSheetScaffoldState.bottomSheetState.hide()
                }
            }
        }

        val currentValue = bottomSheetScaffoldState.bottomSheetState.currentValue
        LaunchedEffect(currentValue) {
            launch {
                if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Hidden) {
                    editData.showing = false
                }
            }
        }

        val scope = rememberCoroutineScope()
        BackHandler(
            enabled = bottomSheetScaffoldState.bottomSheetState.currentValue != SheetValue.Hidden
        ) {
            scope.launch {
                bottomSheetScaffoldState.bottomSheetState.hide()
            }
        }

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val lazyListState = rememberLazyListState()
        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            sheetPeekHeight = 0.dp,
            topBar = {
                CollapsingToolbar(
                    maxHeight = 356.dp,
                    pinnedHeight = 180.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    Header(
                        entry = entry,
                        progress = it,
                        color = color,
                        coverImage = coverImage,
                        bannerImage = bannerImage,
                        titleText = title,
                        subtitleText = subtitle,
                        nextEpisode = nextEpisode,
                        nextEpisodeAiringAt = nextEpisodeAiringAt,
                    )
                }
            },
            snackbarHost = {
                val errorRes = errorRes()
                if (errorRes != null) {
                    SnackbarErrorText(
                        errorRes.first,
                        errorRes.second,
                        onErrorDismiss = onErrorDismiss
                    )
                } else if (editData.errorRes != null) {
                    SnackbarErrorText(
                        editData.errorRes?.first,
                        editData.errorRes?.second,
                        onErrorDismiss = { editData.errorRes = null },
                    )
                } else {
                    // Bottom sheet requires at least one measurable component
                    Spacer(modifier = Modifier.size(0.dp))
                }
            },
            sheetContent = {
                AnimeMediaEditBottomSheet(
                    editData = editData,
                    id = { listEntry()?.id?.toString() },
                    type = { entry()?.media?.type },
                    progressMax = { entry()?.media?.run { episodes ?: volumes } ?: 0 },
                    scoreFormat = scoreFormat,
                    onDateChange = onDateChange,
                    onClickDelete = onClickDelete,
                    onClickSave = onClickSave,
                )
            },
        ) {
            Scaffold(
                floatingActionButton = {
                    val entry = entry()
                    if (entry != null) {
                        val media = entry.media
                        val listEntry = listEntry()

                        val expanded by remember {
                            derivedStateOf { scrollBehavior.state.collapsedFraction == 0f }
                        }

                        var previousIndex by remember(this) { mutableStateOf(lazyListState.firstVisibleItemIndex) }
                        var previousScrollOffset by remember(this) { mutableStateOf(lazyListState.firstVisibleItemScrollOffset) }
                        val showFloatingActionButton by remember(this) {
                            derivedStateOf {
                                if (lazyListState.firstVisibleItemIndex < 3) {
                                    true
                                } else if (previousIndex != lazyListState.firstVisibleItemIndex) {
                                    previousIndex > lazyListState.firstVisibleItemIndex
                                } else {
                                    previousScrollOffset >= lazyListState.firstVisibleItemScrollOffset
                                }.also {
                                    previousIndex = lazyListState.firstVisibleItemIndex
                                    previousScrollOffset =
                                        lazyListState.firstVisibleItemScrollOffset
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = showFloatingActionButton,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            ExtendedFloatingActionButton(
                                text = {
                                    when (listEntry?.status) {
                                        MediaListStatus.CURRENT -> {
                                            if (media.type == MediaType.ANIME) {
                                                stringResource(
                                                    R.string.anime_media_details_fab_user_status_current_anime,
                                                    listEntry.progress ?: 0,
                                                    media.episodes ?: 1,
                                                )
                                            } else {
                                                stringResource(
                                                    R.string.anime_media_details_fab_user_status_current_not_anime,
                                                    listEntry.progressVolumes ?: 0,
                                                    media.volumes ?: 1,
                                                )
                                            }
                                        }
                                        MediaListStatus.PLANNING -> stringResource(
                                            R.string.anime_media_details_fab_user_status_planning
                                        )
                                        MediaListStatus.COMPLETED -> stringResource(
                                            // TODO: Include rating in completed text
                                            R.string.anime_media_details_fab_user_status_completed
                                        )
                                        MediaListStatus.DROPPED -> stringResource(
                                            R.string.anime_media_details_fab_user_status_dropped,
                                            listEntry.progress ?: listEntry.progressVolumes ?: 0,
                                            media.episodes ?: media.volumes ?: 1,
                                        )
                                        MediaListStatus.PAUSED -> stringResource(
                                            R.string.anime_media_details_fab_user_status_paused,
                                            listEntry.progress ?: listEntry.progressVolumes ?: 0,
                                            media.episodes ?: media.volumes ?: 1,
                                        )
                                        MediaListStatus.REPEATING -> stringResource(
                                            R.string.anime_media_details_fab_user_status_repeating,
                                            listEntry.progress ?: listEntry.progressVolumes ?: 0,
                                            media.episodes ?: media.volumes ?: 1,
                                        )
                                        MediaListStatus.UNKNOWN__, null -> stringResource(
                                            R.string.anime_media_details_fab_user_status_unknown
                                        )
                                    }.let {
                                        Text(it)
                                    }
                                },
                                icon = {
                                    when (listEntry?.status) {
                                        MediaListStatus.CURRENT -> if (media.type == MediaType.ANIME) {
                                            Icons.Filled.Monitor to R.string.anime_media_details_fab_user_status_current_anime_icon_content_description
                                        } else {
                                            Icons.Filled.MenuBook to R.string.anime_media_details_fab_user_status_current_not_anime_icon_content_description
                                        }
                                        MediaListStatus.PLANNING -> if (media.type == MediaType.ANIME) {
                                            Icons.Filled.WatchLater
                                        } else {
                                            Icons.Filled.Bookmark
                                        } to R.string.anime_media_details_fab_user_status_planning_icon_content_description
                                        MediaListStatus.COMPLETED -> Icons.Filled.CheckBox to
                                                R.string.anime_media_details_fab_user_status_completed_icon_content_description
                                        MediaListStatus.DROPPED -> Icons.Filled.Delete to
                                                R.string.anime_media_details_fab_user_status_dropped_icon_content_description
                                        MediaListStatus.PAUSED -> Icons.Filled.PauseCircle to
                                                R.string.anime_media_details_fab_user_status_paused_icon_content_description
                                        MediaListStatus.REPEATING -> Icons.Filled.Repeat to
                                                R.string.anime_media_details_fab_user_status_repeating_icon_content_description
                                        MediaListStatus.UNKNOWN__, null -> Icons.Filled.Edit to
                                                R.string.anime_media_details_fab_user_status_edit_icon_content_description
                                    }.let { (vector, contentDescription) ->
                                        Icon(
                                            imageVector = vector,
                                            contentDescription = stringResource(contentDescription),
                                        )
                                    }
                                },
                                expanded = listEntry?.status
                                    ?.takeUnless { it == MediaListStatus.UNKNOWN__ }
                                    ?.takeIf { expanded } != null,
                                containerColor = color()
                                    ?: FloatingActionButtonDefaults.containerColor,
                                onClick = { if (showFloatingActionButton) editData.showing = true },
                            )
                        }
                    }
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                val loading = loading()
                val entry = entry()
                val animeSongs = animeSongs()
                val cdEntries = cdEntries()

                var relationsExpanded by remember { mutableStateOf(false) }
                var recommendationsExpanded by remember { mutableStateOf(false) }
                var songsExpanded by remember { mutableStateOf(false) }
                var streamingEpisodesExpanded by remember { mutableStateOf(false) }
                var streamingEpisodesHidden by remember { mutableStateOf(true) }
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    content(
                        entry = entry,
                        loading = loading,
                        mediaPlayer = mediaPlayer,
                        animeSongs = animeSongs,
                        animeSongState = animeSongState,
                        onAnimeThemePlayClick = onAnimeSongPlayClick,
                        onAnimeSongProgressUpdate = onAnimeSongProgressUpdate,
                        onAnimeSongExpandedToggle = onAnimeSongExpandedToggle,
                        cdEntries = cdEntries,
                        onGenreClicked = onGenreClicked,
                        onGenreLongClicked = onGenreLongClicked,
                        onCharacterClicked = onCharacterClicked,
                        onCharacterLongClicked = onCharacterLongClicked,
                        onStaffClicked = onStaffClicked,
                        onStaffLongClicked = onStaffLongClicked,
                        onTagClicked = onTagClicked,
                        onTagLongClicked = onTagLongClicked,
                        onMediaClicked = onMediaClicked,
                        relationsExpanded = { relationsExpanded },
                        onRelationsExpandedToggled = { relationsExpanded = it },
                        recommendationsExpanded = { recommendationsExpanded },
                        onRecommendationsExpandedToggled = { recommendationsExpanded = it },
                        songsExpanded = { songsExpanded },
                        onSongsExpandedToggled = { songsExpanded = it },
                        streamingEpisodesExpanded = { streamingEpisodesExpanded },
                        onStreamingEpisodesExpandedToggled = { streamingEpisodesExpanded = it },
                        streamingEpisodesHidden = { streamingEpisodesHidden },
                        onStreamingEpisodesHiddenToggled = { streamingEpisodesHidden = it },
                        trailerPlaybackPosition = trailerPlaybackPosition,
                        onTrailerPlaybackPositionUpdate = onTrailerPlaybackPositionUpdate,
                    )
                }
            }
        }
    }

    @Composable
    private fun Error() {
        Text("TODO: Error state")
    }

    private fun LazyListScope.content(
        entry: Entry?,
        loading: Boolean,
        mediaPlayer: @Composable () -> AppMediaPlayer,
        animeSongs: AnimeMediaDetailsViewModel.AnimeSongs?,
        animeSongState: (animeSongId: String) -> AnimeMediaDetailsViewModel.AnimeSongState,
        onAnimeThemePlayClick: (animeSongId: String) -> Unit,
        onAnimeSongProgressUpdate: (animeSongId: String, Float) -> Unit,
        onAnimeSongExpandedToggle: (animeSongId: String, expanded: Boolean) -> Unit,
        cdEntries: List<CdEntryGridModel>,
        onGenreClicked: (String) -> Unit,
        onGenreLongClicked: (String) -> Unit,
        onCharacterClicked: (String) -> Unit,
        onCharacterLongClicked: (String) -> Unit,
        onStaffClicked: (String) -> Unit,
        onStaffLongClicked: (String) -> Unit,
        onTagClicked: (tagId: String, tagName: String) -> Unit,
        onTagLongClicked: (String) -> Unit,
        onMediaClicked: (AnimeMediaListRow.Entry) -> Unit,
        relationsExpanded: () -> Boolean,
        onRelationsExpandedToggled: (Boolean) -> Unit,
        recommendationsExpanded: () -> Boolean,
        onRecommendationsExpandedToggled: (Boolean) -> Unit,
        songsExpanded: () -> Boolean,
        onSongsExpandedToggled: (Boolean) -> Unit,
        streamingEpisodesExpanded: () -> Boolean,
        onStreamingEpisodesExpandedToggled: (Boolean) -> Unit,
        streamingEpisodesHidden: () -> Boolean,
        onStreamingEpisodesHiddenToggled: (Boolean) -> Unit,
        trailerPlaybackPosition: () -> Float,
        onTrailerPlaybackPositionUpdate: (Float) -> Unit,
    ) {
        if (entry == null) {
            if (loading) {
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
                    Error()
                }
            }
            return
        }
        genreSection(
            entry = entry,
            onGenreClicked = onGenreClicked,
            onGenreLongClicked = onGenreLongClicked,
        )

        descriptionSection(entry)

        charactersSection(
            entry = entry,
            onCharacterClicked = onCharacterClicked,
            onCharacterLongClicked = onCharacterLongClicked,
        )

        relationsSection(
            entry = entry,
            relationsExpanded = relationsExpanded,
            onRelationsExpandedToggled = onRelationsExpandedToggled,
            onMediaClicked = onMediaClicked,
            onTagClicked = onTagClicked,
            onTagLongClicked = onTagLongClicked,
        )

        infoSection(entry)

        songsSection(
            animeSongs = animeSongs,
            songsExpanded = songsExpanded,
            onSongsExpandedToggled = onSongsExpandedToggled,
            mediaPlayer = mediaPlayer,
            animeSongState = animeSongState,
            onAnimeThemePlayClick = onAnimeThemePlayClick,
            onAnimeSongProgressUpdate = onAnimeSongProgressUpdate,
            onAnimeSongExpandedToggle = onAnimeSongExpandedToggle,
        )

        cdsSection(cdEntries)

        staffSection(
            entry = entry,
            onStaffClicked = onStaffClicked,
            onStaffLongClicked = onStaffLongClicked,
        )

        statsSection(entry)

        tagSection(
            entry = entry,
            onTagClicked = onTagClicked,
            onTagLongClicked = onTagLongClicked,
        )

        trailerSection(
            entry = entry,
            playbackPosition = trailerPlaybackPosition,
            onPlaybackPositionUpdate = onTrailerPlaybackPositionUpdate,
        )

        streamingEpisodesSection(
            entry = entry,
            expanded = streamingEpisodesExpanded,
            expandedToggled = onStreamingEpisodesExpandedToggled,
            hidden = streamingEpisodesHidden,
            onHiddenToggled = onStreamingEpisodesHiddenToggled,
        )

        socialLinksSection(entry = entry)
        streamingLinksSection(entry = entry)
        otherLinksSection(entry = entry)

        recommendationsSection(
            entry = entry,
            recommendationsExpanded = recommendationsExpanded,
            onRecommendationsExpandedToggled = onRecommendationsExpandedToggled,
            onMediaClicked = onMediaClicked,
            onTagClicked = onTagClicked,
            onTagLongClicked = onTagLongClicked,
        )
    }

    @Composable
    private fun Header(
        entry: @Composable () -> Entry?,
        progress: Float,
        color: () -> Color?,
        coverImage: @Composable () -> String?,
        bannerImage: @Composable () -> String?,
        titleText: @Composable () -> String,
        subtitleText: @Composable () -> String?,
        nextEpisode: @Composable () -> Int?,
        nextEpisodeAiringAt: @Composable () -> Int?,
    ) {
        val elevation = lerp(0.dp, 16.dp, AccelerateEasing.transform(progress))
        var preferredTitle by remember { mutableStateOf<Int?>(null) }

        @Suppress("NAME_SHADOWING")
        val entry = entry()
        Surface(
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = elevation,
            shadowElevation = elevation,
            modifier = Modifier.clickable(enabled = (entry?.titlesUnique?.size ?: 0) > 1) {
                preferredTitle =
                    ((preferredTitle ?: 0) + 1) % (entry?.titlesUnique?.size ?: 1)
            }
        ) {
            Box {
                AsyncImage(
                    model = bannerImage(),
                    contentScale = ContentScale.FillHeight,
                    contentDescription = stringResource(R.string.anime_media_banner_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithCache {
                            val brush = Brush.verticalGradient(
                                AnimationUtils.lerp(0.5f, 0f, progress) to
                                        Color.Black.copy(
                                            alpha = AnimationUtils.lerp(1f, 0.25f, progress)
                                        ),
                                1f to Color.Transparent,
                            )
                            onDrawWithContent {
                                drawContent()
                                drawRect(brush, blendMode = BlendMode.DstIn)
                            }
                        }
                        .background(color() ?: Color.Unspecified)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = lerp(100.dp, 10.dp, progress), bottom = 10.dp)
                        .height(lerp(256.dp, 180.dp, progress))
                ) {
                    ElevatedCard {
                        AsyncImage(
                            model = coverImage(),
                            contentScale = ContentScale.FillHeight,
                            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                            contentDescription = stringResource(R.string.anime_media_cover_image),
                            modifier = Modifier
                                .height(256.dp)
                                .widthIn(max = 256.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .padding(top = lerp(32.dp, 0.dp, progress))
                            .animateContentSize()
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            AutoResizeHeightText(
                                text = when (val index = preferredTitle) {
                                    null -> null
                                    else -> entry?.titlesUnique?.get(index)
                                } ?: titleText(),
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

                        nextEpisodeAiringAt()?.let { airingAtTime ->
                            nextEpisode()?.let {
                                val context = LocalContext.current
                                val airingAt = remember {
                                    MediaUtils.formatAiringAt(context, airingAtTime * 1000L)
                                }

                                val remainingTime = remember {
                                    MediaUtils.formatRemainingTime(airingAtTime * 1000L)
                                }

                                Text(
                                    text = stringResource(
                                        R.string.anime_media_next_airing_episode,
                                        it,
                                        airingAt,
                                        remainingTime,
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.surfaceTint,
                                    modifier = Modifier
                                        .wrapContentHeight(Alignment.Bottom)
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
        )
    }

    @Composable
    private fun SubsectionHeader(text: String, modifier: Modifier = Modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.surfaceTint,
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)
        )
    }

    private fun LazyListScope.descriptionSection(entry: Entry) {
        entry.description?.let {
            item {
                SectionHeader(stringResource(R.string.anime_media_details_description_label))
            }
            item {
                var expanded by remember { mutableStateOf(false) }
                ElevatedCard(
                    onClick = { expanded = !expanded },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateContentSize(),
                ) {
                    val style = MaterialTheme.typography.bodyMedium
                    HtmlText(
                        text = it,
                        style = style,
                        color = style.color.takeOrElse { LocalContentColor.current },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .wrapContentHeight()
                            .heightIn(max = if (expanded) Dp.Unspecified else 80.dp)
                            .bottomFadingEdge(expanded)
                    )
                }
            }
        }
    }

    private fun LazyListScope.genreSection(
        entry: Entry,
        onGenreClicked: (String) -> Unit,
        onGenreLongClicked: (String) -> Unit
    ) {
        if (entry.genres.isNotEmpty()) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 10.dp)
                        .animateContentSize(),
                ) {
                    entry.genres.forEach {
                        AssistChip(
                            onClick = { onGenreClicked(it.name) },
                            onLongClickLabel = stringResource(
                                R.string.anime_media_tag_long_click_content_description
                            ),
                            onLongClick = { onGenreLongClicked(it.name) },
                            label = { AutoHeightText(it.name) },
                            colors = assistChipColors(containerColor = it.color),
                        )
                    }
                }
            }
        }
    }

    private fun LazyListScope.charactersSection(
        entry: Entry,
        onCharacterClicked: (String) -> Unit,
        onCharacterLongClicked: (String) -> Unit
    ) {
        if (entry.characters.isEmpty()) return
        item {
            val coroutineScope = rememberCoroutineScope()
            SectionHeader(stringResource(R.string.anime_media_details_characters_label))

            // TODO: Even wider scoped cache?
            // Cache character color calculation
            val colorMap = remember { mutableStateMapOf<String, Pair<Color, Color>>() }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(entry.characters, { it.id }) {
                    CharacterCard(
                        coroutineScope = coroutineScope,
                        id = it.id,
                        image = it.image,
                        colorMap = colorMap,
                        onClick = onCharacterClicked,
                        innerImage = (it.languageToVoiceActor["Japanese"]
                            ?: it.languageToVoiceActor.values.firstOrNull())?.image,
                    ) { textColor ->
                        AutoHeightText(
                            text = it.name.orEmpty(),
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineBreak = LineBreak(
                                    strategy = LineBreak.Strategy.Balanced,
                                    strictness = LineBreak.Strictness.Strict,
                                    wordBreak = LineBreak.WordBreak.Default,
                                )
                            ),
                            minTextSizeSp = 8f,
                            modifier = Modifier
                                .size(width = 100.dp, height = 56.dp)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun CharacterCard(
        coroutineScope: CoroutineScope,
        id: String,
        image: String?,
        colorMap: MutableMap<String, Pair<Color, Color>>,
        onClick: (id: String) -> Unit,
        innerImage: String? = null,
        content: @Composable (textColor: Color) -> Unit,
    ) {
        val defaultTextColor = MaterialTheme.typography.bodyMedium.color
        val colors = colorMap[id]

        val animationProgress by animateIntAsState(
            if (colors == null) 0 else 255,
            label = "Character card color fade in",
        )

        val containerColor = when {
            colors == null || animationProgress == 0 ->
                MaterialTheme.colorScheme.surface
            animationProgress == 255 -> colors.first
            else -> Color(
                ColorUtils.compositeColors(
                    ColorUtils.setAlphaComponent(
                        colors.first.toArgb(),
                        animationProgress
                    ),
                    MaterialTheme.colorScheme.surface.toArgb()
                )
            )
        }

        val textColor = when {
            colors == null || animationProgress == 0 -> defaultTextColor
            animationProgress == 255 -> colors.second
            else -> Color(
                ColorUtils.compositeColors(
                    ColorUtils.setAlphaComponent(
                        colors.second.toArgb(),
                        animationProgress
                    ),
                    defaultTextColor.toArgb()
                )
            )
        }

        ElevatedCard(
            onClick = { onClick(id) },
            colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
            modifier = Modifier.width(100.dp),
        ) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image)
                        .crossfade(true)
                        .allowHardware(false)
                        .build(),
                    contentScale = ContentScale.Crop,
                    fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                    contentDescription = stringResource(
                        R.string.anime_media_character_image
                    ),
                    onSuccess = {
                        if (!colorMap.containsKey(id)) {
                            (it.result.drawable as? BitmapDrawable)?.bitmap?.let {
                                coroutineScope.launch(CustomDispatchers.IO) {
                                    try {
                                        val palette = Palette.from(it)
                                            .setRegion(
                                                0,
                                                // Only capture the bottom 1/4th so
                                                // color flows from image better
                                                it.height / 4 * 3,
                                                // Only capture left 3/5ths to ignore
                                                // part covered by voice actor
                                                if (innerImage == null) {
                                                    it.width
                                                } else {
                                                    it.width / 5 * 3
                                                },
                                                it.height
                                            )
                                            .generate()
                                        val swatch = palette.swatches
                                            .maxByOrNull { it.population }
                                        if (swatch != null) {
                                            withContext(CustomDispatchers.Main) {
                                                colorMap[id] =
                                                    Color(swatch.rgb) to Color(
                                                        ColorUtils.setAlphaComponent(
                                                            swatch.bodyTextColor,
                                                            0xFF
                                                        )
                                                    )
                                            }
                                        }
                                    } catch (ignored: Exception) {
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.size(width = 100.dp, height = 150.dp)
                )

                if (innerImage != null) {
                    var showInnerImage by remember { mutableStateOf(true) }
                    if (showInnerImage) {
                        var showBorder by remember(id) { mutableStateOf(false) }
                        val alpha by animateFloatAsState(
                            if (showBorder) 1f else 0f,
                            label = "Character card inner image fade",
                        )
                        val clipShape = RoundedCornerShape(topStart = 8.dp)
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(innerImage)
                                .crossfade(false)
                                .listener(onError = { _, _ ->
                                    showInnerImage = false
                                }, onSuccess = { _, _ ->
                                    showBorder = true
                                })
                                .build(),
                            contentScale = ContentScale.Crop,
                            contentDescription = stringResource(
                                R.string.anime_media_voice_actor_image
                            ),
                            modifier = Modifier
                                .size(width = 40.dp, height = 40.dp)
                                .alpha(alpha)
                                .align(Alignment.BottomEnd)
                                .clip(clipShape)
                                .border(
                                    width = 1.dp,
                                    color = Color.Black,
                                    shape = clipShape
                                )
                        )
                    }
                }
            }

            content(textColor)
        }
    }

    private fun LazyListScope.staffSection(
        entry: Entry,
        onStaffClicked: (String) -> Unit,
        onStaffLongClicked: (String) -> Unit
    ) {
        if (entry.staff.isEmpty()) return
        item {
            val coroutineScope = rememberCoroutineScope()
            SectionHeader(stringResource(R.string.anime_media_details_staff_label))

            // TODO: Even wider scoped cache?
            // Cache staff color calculation
            val colorMap = remember { mutableStateMapOf<String, Pair<Color, Color>>() }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(entry.staff, { it.id }) {
                    CharacterCard(
                        coroutineScope = coroutineScope,
                        id = it.id,
                        image = it.image,
                        colorMap = colorMap,
                        onClick = onStaffClicked,
                    ) { textColor ->
                        it.role?.let {
                            AutoHeightText(
                                text = it,
                                color = textColor,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    lineBreak = LineBreak(
                                        strategy = LineBreak.Strategy.Simple,
                                        strictness = LineBreak.Strictness.Strict,
                                        wordBreak = LineBreak.WordBreak.Default,
                                    )
                                ),
                                minLines = 2,
                                maxLines = 2,
                                minTextSizeSp = 8f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                            )
                        }

                        it.name?.let {
                            AutoHeightText(
                                text = it,
                                color = textColor,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineBreak = LineBreak(
                                        strategy = LineBreak.Strategy.Balanced,
                                        strictness = LineBreak.Strictness.Strict,
                                        wordBreak = LineBreak.WordBreak.Default,
                                    )
                                ),
                                minTextSizeSp = 8f,
                                minLines = 2,
                                maxLines = 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun LazyListScope.relationsSection(
        entry: Entry,
        relationsExpanded: () -> Boolean,
        onRelationsExpandedToggled: (Boolean) -> Unit,
        onMediaClicked: (AnimeMediaListRow.Entry) -> Unit,
        onTagClicked: (tagId: String, tagName: String) -> Unit,
        onTagLongClicked: (String) -> Unit,
    ) {
        mediaListSection(
            titleRes = R.string.anime_media_details_relations_label,
            values = entry.relations,
            valueToEntry = { it.entry },
            aboveFold = RELATIONS_ABOVE_FOLD,
            expanded = relationsExpanded,
            onExpandedToggled = onRelationsExpandedToggled,
            onMediaClicked = onMediaClicked,
            onTagClicked = onTagClicked,
            onTagLongClicked = onTagLongClicked,
            label = { RelationLabel(it.relation) },
        )
    }

    private fun LazyListScope.infoSection(entry: Entry) {
        item {
            SectionHeader(stringResource(R.string.anime_media_details_information_label))
        }
        item {
            ElevatedCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .animateContentSize(),
            ) {
                val media = entry.media
                TwoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_format_label),
                    bodyOne = stringResource(entry.formatTextRes),
                    labelTwo = stringResource(R.string.anime_media_details_status_label),
                    bodyTwo = stringResource(entry.statusTextRes),
                    showDividerAbove = false,
                )

                TwoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_episodes_label),
                    bodyOne = media.episodes?.toString(),
                    labelTwo = stringResource(R.string.anime_media_details_duration_label),
                    bodyTwo = media.duration?.let {
                        stringResource(R.string.anime_media_details_duration_minutes, it)
                    },
                )

                TwoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_volumes_label),
                    bodyOne = media.volumes?.toString(),
                    labelTwo = stringResource(R.string.anime_media_details_chapters_label),
                    bodyTwo = media.chapters?.toString(),
                )

                TwoColumnInfoText(
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

                TwoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_start_date_label),
                    bodyOne = startDateFormatted,
                    labelTwo = stringResource(R.string.anime_media_details_end_date_label),
                    bodyTwo = endDateFormatted,
                )

                TwoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_average_score_label),
                    bodyOne = media.averageScore?.toString(),
                    labelTwo = stringResource(R.string.anime_media_details_mean_score_label),
                    bodyTwo = media.meanScore?.toString(),
                )

                TwoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_popularity_label),
                    bodyOne = media.popularity?.toString(),
                    labelTwo = stringResource(R.string.anime_media_details_favorites_label),
                    bodyTwo = media.favourites?.toString(),
                )

                val uriHandler = LocalUriHandler.current
                TwoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_trending_label),
                    bodyOne = media.trending?.toString(),
                    labelTwo = "",
                    bodyTwo = null,
                )

                TwoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_licensed_label),
                    bodyOne = entry.licensedTextRes?.let { stringResource(it) },
                    labelTwo = stringResource(R.string.anime_media_details_country_label),
                    bodyTwo = entry.country,
                )

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
                            showDividerAbove = true
                        )
                    }
                }

                // TODO: isFavorite, isAdult, airingSchedule, reviews, trends

                ExpandableListInfoText(
                    labelTextRes = R.string.anime_media_details_studios_label,
                    contentDescriptionTextRes = R.string.anime_media_details_studios_expand_content_description,
                    values = entry.studios,
                    valueToText = { it.name },
                    onClick = { uriHandler.openUri(AniListUtils.studioUrl(it.id)) },
                )

                ExpandableListInfoText(
                    labelTextRes = R.string.anime_media_details_synonyms_label,
                    contentDescriptionTextRes = R.string.anime_media_details_synonyms_expand_content_description,
                    values = entry.allSynonyms,
                    valueToText = { it },
                )
            }
        }
    }

    @Composable
    private fun TwoColumnInfoText(
        labelOne: String, bodyOne: String?, onClickOne: (() -> Unit)? = null,
        labelTwo: String, bodyTwo: String?, onClickTwo: (() -> Unit)? = null,
        showDividerAbove: Boolean = true
    ) {
        if (bodyOne != null && bodyTwo != null) {
            if (showDividerAbove) {
                Divider()
            }
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .optionalClickable(onClickOne)
                ) {
                    InfoText(label = labelOne, body = bodyOne, showDividerAbove = false)
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(DividerDefaults.Thickness)
                        .background(color = DividerDefaults.color)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .optionalClickable(onClickTwo)
                ) {
                    InfoText(label = labelTwo, body = bodyTwo, showDividerAbove = false)
                }
            }
        } else if (bodyOne != null) {
            Column(modifier = Modifier.optionalClickable(onClickOne)) {
                InfoText(label = labelOne, body = bodyOne, showDividerAbove = showDividerAbove)
            }
        } else if (bodyTwo != null) {
            Column(modifier = Modifier.optionalClickable(onClickTwo)) {
                InfoText(label = labelTwo, body = bodyTwo, showDividerAbove = showDividerAbove)
            }
        }
    }

    @Suppress("UnusedReceiverParameter")
    @Composable
    private fun ColumnScope.InfoText(
        label: String,
        body: String,
        showDividerAbove: Boolean = true
    ) {
        if (showDividerAbove) {
            Divider()
        }

        SubsectionHeader(label)

        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp)
        )
    }

    @Composable
    private fun <T> ExpandableListInfoText(
        @StringRes labelTextRes: Int,
        @StringRes contentDescriptionTextRes: Int,
        values: List<T>,
        valueToText: @Composable (T) -> String,
        onClick: ((T) -> Unit)? = null,
        showDividerAbove: Boolean = true,
    ) {
        if (values.isEmpty()) return

        var expanded by remember { mutableStateOf(false) }

        Box {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .heightIn(max = if (expanded) Dp.Unspecified else 120.dp)
                    .clickable { expanded = !expanded }
                    .bottomFadingEdge(expanded)
            ) {
                if (showDividerAbove) {
                    Divider()
                }

                SubsectionHeader(stringResource(labelTextRes))

                values.forEachIndexed { index, value ->
                    if (index != 0) {
                        Divider(modifier = Modifier.padding(start = 16.dp))
                    }

                    val bottomPadding = if (index == values.size - 1) {
                        12.dp
                    } else {
                        8.dp
                    }

                    Text(
                        text = valueToText(value),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .optionalClickable(
                                onClick = onClick
                                    ?.takeIf { expanded }
                                    ?.let { { onClick(value) } }
                            )
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = bottomPadding,
                            )
                    )
                }
            }

            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = stringResource(contentDescriptionTextRes),
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }

    private fun LazyListScope.songsSection(
        animeSongs: AnimeMediaDetailsViewModel.AnimeSongs?,
        songsExpanded: () -> Boolean,
        onSongsExpandedToggled: (Boolean) -> Unit,
        mediaPlayer: @Composable () -> AppMediaPlayer,
        animeSongState: (animeSongId: String) -> AnimeMediaDetailsViewModel.AnimeSongState,
        onAnimeThemePlayClick: (animeSongId: String) -> Unit,
        onAnimeSongProgressUpdate: (animeSongId: String, Float) -> Unit,
        onAnimeSongExpandedToggle: (animeSongId: String, expanded: Boolean) -> Unit,
    ) {
        if (animeSongs == null) return
        listSection(
            titleRes = R.string.anime_media_details_songs_label,
            values = animeSongs.entries,
            aboveFold = SONGS_ABOVE_FOLD,
            expanded = songsExpanded,
            onExpandedToggled = onSongsExpandedToggled,
        ) { item, paddingBottom ->
            AnimeThemeRow(
                entry = item,
                mediaPlayer = mediaPlayer,
                state = animeSongState,
                onClickPlay = onAnimeThemePlayClick,
                onProgressUpdate = onAnimeSongProgressUpdate,
                onExpandedToggle = onAnimeSongExpandedToggle,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
            )
        }
    }

    @Suppress("NAME_SHADOWING")
    @Composable
    private fun AnimeThemeRow(
        entry: AnimeMediaDetailsViewModel.AnimeSongEntry,
        mediaPlayer: @Composable () -> AppMediaPlayer,
        state: (animeSongId: String) -> AnimeMediaDetailsViewModel.AnimeSongState,
        onClickPlay: (animeSongId: String) -> Unit,
        onProgressUpdate: (animeSongId: String, Float) -> Unit,
        onExpandedToggle: (animeSongId: String, expanded: Boolean) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val state = state(entry.id)
        val mediaPlayer = mediaPlayer()
        val playingState by mediaPlayer.playingState.collectAsState()
        val playing by remember {
            derivedStateOf { playingState.first == entry.id && playingState.second }
        }

        ElevatedCard(
            modifier = modifier.animateContentSize(),
        ) {
            var hidden by remember { mutableStateOf(entry.spoiler) }
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
                    modifier = Modifier.clickable { onExpandedToggle(entry.id, !state.expanded()) },
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
                            AnimeTheme.Type.Opening -> if (entry.episodes.isNullOrBlank()) {
                                stringResource(R.string.anime_media_details_song_opening)
                            } else {
                                stringResource(
                                    R.string.anime_media_details_song_opening_episodes,
                                    entry.episodes,
                                )
                            }
                            AnimeTheme.Type.Ending -> if (entry.episodes.isNullOrBlank()) {
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
                                IconButton(onClick = { onClickPlay(entry.id) }) {
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
                                    onClick = { onExpandedToggle(entry.id, !state.expanded()) },
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
                                                val visible = it == View.VISIBLE
                                                linkButtonVisible = visible
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
                    } else if (playing) {
                        val progress = mediaPlayer.progress
                        Slider(
                            value = progress,
                            onValueChange = { onProgressUpdate(entry.id, it) },
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
                    artists.forEach { artist ->
                        Divider()
                        Row(
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .clickable { uriHandler.openUri(artist.link) }
                        ) {
                            val artistImage = artist.image
                            val characterImage = artist.character?.image

                            @Composable
                            fun ArtistImage() {
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
                                    modifier = Modifier
                                        .sizeIn(minWidth = 44.dp, minHeight = 64.dp)
                                        .fillMaxHeight()
                                )
                            }

                            @Composable
                            fun CharacterImage() {
                                AsyncImage(
                                    model = characterImage!!,
                                    contentScale = ContentScale.FillHeight,
                                    fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                                    contentDescription = stringResource(
                                        R.string.anime_media_character_image
                                    ),
                                    modifier = Modifier
                                        .sizeIn(minWidth = 44.dp, minHeight = 64.dp)
                                        .fillMaxHeight()
                                )
                            }

                            val firstImage: (@Composable () -> Unit)?
                            val secondImage: (@Composable () -> Unit)?

                            val asCharacter = artist.asCharacter
                            if (asCharacter) {
                                if (characterImage == null) {
                                    if (artistImage == null) {
                                        firstImage = null
                                        secondImage = null
                                    } else {
                                        firstImage = { ArtistImage() }
                                        secondImage = null
                                    }
                                } else {
                                    firstImage = { CharacterImage() }
                                    secondImage = { ArtistImage() }
                                }
                            } else {
                                firstImage = { ArtistImage() }
                                secondImage = characterImage?.let { { CharacterImage() } }
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
                                firstImage()
                            }

                            val artistText = if (artist.character == null) {
                                artist.name
                            } else if (artist.asCharacter) {
                                stringResource(
                                    R.string.anime_media_details_song_artist_as_character,
                                    artist.character.name,
                                    artist.name,
                                )
                            } else {
                                stringResource(
                                    R.string.anime_media_details_song_artist_with_character,
                                    artist.name,
                                    artist.character.name,
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
                                secondImage()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun LazyListScope.cdsSection(
        cdEntries: List<CdEntryGridModel>,
    ) {
        if (cdEntries.isEmpty()) return

        item {
            SectionHeader(stringResource(R.string.anime_media_details_cds_label))
        }

        item {
            val width = LocalDensity.current.run { Dimension.Pixels(200.dp.toPx().roundToInt()) }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                itemsIndexed(cdEntries) { index, cdEntry ->
                    ElevatedCard {
                        EntryGrid.Entry(
                            imageScreenKey = "anime_details",
                            expectedWidth = width,
                            index = index,
                            entry = cdEntry,
                        )
                    }
                }
            }
        }
    }

    private fun LazyListScope.recommendationsSection(
        entry: Entry,
        recommendationsExpanded: () -> Boolean,
        onRecommendationsExpandedToggled: (Boolean) -> Unit,
        onMediaClicked: (AnimeMediaListRow.Entry) -> Unit,
        onTagClicked: (tagId: String, tagName: String) -> Unit,
        onTagLongClicked: (String) -> Unit,
    ) {
        mediaListSection(
            titleRes = R.string.anime_media_details_recommendations_label,
            values = entry.recommendations,
            valueToEntry = { it.entry },
            aboveFold = RECOMMENDATIONS_ABOVE_FOLD,
            expanded = recommendationsExpanded,
            onExpandedToggled = onRecommendationsExpandedToggled,
            onMediaClicked = onMediaClicked,
            onTagClicked = onTagClicked,
            onTagLongClicked = onTagLongClicked,
        )
    }

    private fun <T> LazyListScope.mediaListSection(
        @StringRes titleRes: Int,
        values: Collection<T>,
        valueToEntry: (T) -> AnimeMediaListRow.Entry,
        aboveFold: Int,
        expanded: () -> Boolean,
        onExpandedToggled: (Boolean) -> Unit,
        onMediaClicked: (AnimeMediaListRow.Entry) -> Unit,
        onTagClicked: (tagId: String, tagName: String) -> Unit,
        onTagLongClicked: (String) -> Unit,
        label: (@Composable (T) -> Unit)? = null,
    ) = listSection(
        titleRes = titleRes,
        values = values,
        aboveFold = aboveFold,
        expanded = expanded,
        onExpandedToggled = onExpandedToggled,
    ) { item, paddingBottom ->
        val entry = valueToEntry(item)
        AnimeMediaListRow(
            entry = entry,
            label = if (label == null) null else {
                { label(item) }
            },
            onClick = onMediaClicked,
            onTagClick = onTagClicked,
            onTagLongClick = onTagLongClicked,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
        )
    }

    private fun <T> LazyListScope.listSection(
        @StringRes titleRes: Int,
        values: Collection<T>,
        aboveFold: Int,
        expanded: () -> Boolean,
        onExpandedToggled: (Boolean) -> Unit,
        hidden: () -> Boolean = { false },
        hiddenContent: @Composable () -> Unit = {},
        itemContent: @Composable (T, paddingBottom: Dp) -> Unit,
    ) {
        if (values.isNotEmpty()) {
            item {
                SectionHeader(
                    text = stringResource(titleRes),
                    modifier = Modifier.clickable { onExpandedToggled(!expanded()) }
                )
            }

            if (hidden()) {
                item {
                    hiddenContent()
                }
                return
            }

            val hasMore = values.size > aboveFold

            itemsIndexed(values.take(aboveFold)) { index, item ->
                val paddingBottom = if (index == values.size
                        .coerceAtMost(aboveFold) - 1
                ) {
                    if (hasMore) 16.dp else 0.dp
                } else {
                    16.dp
                }
                itemContent(item, paddingBottom)
            }

            if (hasMore) {
                if (expanded()) {
                    items(values.drop(aboveFold)) {
                        itemContent(it, 16.dp)
                    }
                }

                item {
                    @Suppress("NAME_SHADOWING") val expanded = expanded()
                    ElevatedCard(
                        onClick = { onExpandedToggled(!expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (expanded) {
                                    UtilsStringR.show_less
                                } else {
                                    UtilsStringR.show_more
                                }
                            ),
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun RelationLabel(relation: MediaRelation) {
        Text(
            text = stringResource(relation.toTextRes()),
            style = MaterialTheme.typography.titleMedium,
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

    private fun LazyListScope.statsSection(entry: Entry) {
        item {
            SectionHeader(stringResource(R.string.anime_media_details_stats_label))
        }

        item {
            ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                ExpandableListInfoText(
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
                    SubsectionHeader(
                        stringResource(R.string.anime_media_details_status_distribution_label)
                    )
                    Row {
                        val sliceVisibility =
                            remember { mutableStateMapOf<MediaListStatus, Boolean>() }
                        val brush = remember(sliceVisibility.values.toList()) {
                            val total = statusDistribution.sumOf {
                                val visible = sliceVisibility[it.status] ?: true
                                it.amount?.takeIf { visible } ?: 0
                            }.toFloat()

                            val colorStops =
                                statusDistribution.fold(mutableListOf<Pair<Float, Color>>()) { list, slice ->
                                    val color = slice.status.toColor()
                                    val lastValue = list.lastOrNull()?.first ?: 0f
                                    val visible = sliceVisibility[slice.status] ?: true
                                    val amount = slice.amount?.takeIf { visible } ?: 0

                                    val portion = amount / total
                                    list += lastValue to color
                                    list += lastValue + portion to color
                                    list
                                }.toTypedArray()

                            Brush.sweepGradient(*colorStops)
                        }

                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .clip(CircleShape)
                                .background(brush, CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                .widthIn(max = 280.dp)
                                .weight(0.5f, fill = false)
                                .aspectRatio(1f)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                        ) {
                            statusDistribution.forEach {
                                val visible = sliceVisibility[it.status] ?: true
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .height(IntrinsicSize.Min)
                                        .clickable {
                                            it.status?.let { sliceVisibility[it] = !visible }
                                        },
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(color = it.status.toColor())
                                            .aspectRatio(1f, matchHeightConstraintsFirst = true)
                                    )

                                    val textRes = when (it.status) {
                                        MediaListStatus.CURRENT -> R.string.anime_media_details_status_distribution_current
                                        MediaListStatus.PLANNING -> R.string.anime_media_details_status_distribution_planning
                                        MediaListStatus.COMPLETED -> R.string.anime_media_details_status_distribution_completed
                                        MediaListStatus.DROPPED -> R.string.anime_media_details_status_distribution_dropped
                                        MediaListStatus.PAUSED -> R.string.anime_media_details_status_distribution_paused
                                        MediaListStatus.REPEATING -> R.string.anime_media_details_status_distribution_repeating
                                        MediaListStatus.UNKNOWN__, null -> R.string.anime_media_details_status_distribution_unknown
                                    }
                                    Text(
                                        text = stringResource(textRes, it.amount ?: 0),
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier
                                            .alpha(if (visible) 1f else 0.38f)
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                val scoreDistribution = entry.scoreDistribution
                if (scoreDistribution.isNotEmpty()) {
                    Divider()
                    SubsectionHeader(
                        stringResource(R.string.anime_media_details_score_distribution_label)
                    )

                    Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                        val maxAmount = scoreDistribution.maxOf { it.amount ?: 0 } * 1.1f
                        val firstColorIndex =
                            scoreDistribution.size - MediaUtils.scoreDistributionColors.size
                        scoreDistribution.forEachIndexed { index, it ->
                            val score = it.score ?: return@forEachIndexed
                            val amount = it.amount ?: return@forEachIndexed

                            val color = if (index >= firstColorIndex) {
                                MediaUtils.scoreDistributionColors[index - firstColorIndex]
                            } else {
                                MediaUtils.scoreDistributionColors.first()
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                0f to Color.Transparent,
                                                1f - (amount / maxAmount) to Color.Transparent,
                                                1f - (amount / maxAmount) to color,
                                                1f to color,
                                            )
                                        )
                                )

                                AutoSizeText(
                                    text = score.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                            .height(IntrinsicSize.Min)
                    ) {
                        scoreDistribution.forEach {
                            val amount = it.amount ?: return@forEach
                            AutoSizeText(
                                text = amount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentHeight()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun LazyListScope.tagSection(
        entry: Entry,
        onTagClicked: (tagId: String, tagName: String) -> Unit = { _, _ -> },
        onTagLongClicked: (tagId: String) -> Unit = {},
    ) {
        if (entry.tags.isNotEmpty()) {
            item {
                SectionHeader(stringResource(R.string.anime_media_details_tags_label))
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    entry.tags.forEach {
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
                            onTagClicked = onTagClicked,
                            onTagLongClicked = onTagLongClicked
                        )
                    }
                }
            }
        }
    }

    private fun LazyListScope.trailerSection(
        entry: Entry,
        playbackPosition: () -> Float,
        onPlaybackPositionUpdate: (Float) -> Unit,
    ) {
        val trailer = entry.media.trailer ?: return
        if (trailer.site != "youtube" && trailer.site != "dailymotion") return

        val videoId = trailer.id ?: return

        item {
            SectionHeader(stringResource(R.string.anime_media_details_trailer_label))
        }

        if (trailer.site == "youtube") {
            item {
                val lifecycleOwner = LocalLifecycleOwner.current
                ElevatedCard(
                    modifier = Modifier.padding(horizontal = 16.dp)
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
                                                second: Float
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
            item {
                val uriHandler = LocalUriHandler.current
                ElevatedCard(
                    onClick = { uriHandler.openUri(MediaUtils.dailymotionUrl(videoId)) },
                    modifier = Modifier.padding(horizontal = 16.dp)
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
        entry: Entry,
        expanded: () -> Boolean,
        expandedToggled: (Boolean) -> Unit,
        hidden: () -> Boolean,
        onHiddenToggled: (Boolean) -> Unit,
    ) {
        val streamingEpisodes = entry.media.streamingEpisodes?.filterNotNull()
            ?.takeIf { it.isNotEmpty() } ?: return
        listSection(
            titleRes = R.string.anime_media_details_streaming_episodes_label,
            values = streamingEpisodes,
            aboveFold = STREAMING_EPISODES_ABOVE_FOLD,
            expanded = expanded,
            onExpandedToggled = expandedToggled,
            hidden = hidden,
            hiddenContent = {
                ElevatedCard(
                    onClick = { onHiddenToggled(false) },
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
        ) { item, paddingBottom ->
            val uriHandler = LocalUriHandler.current
            ElevatedCard(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
                    .optionalClickable(onClick = item.url?.let { { uriHandler.openUri(it) } }),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = item.thumbnail,
                        contentScale = ContentScale.FillHeight,
                        contentDescription = stringResource(
                            R.string.anime_media_details_streaming_episode_content_description
                        ),
                        modifier = Modifier.widthIn(max = 200.dp),
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

    private fun LazyListScope.socialLinksSection(entry: Entry) {
        linksSection(R.string.anime_media_details_social_links_label, entry.socialLinks)
    }

    private fun LazyListScope.streamingLinksSection(entry: Entry) {
        linksSection(R.string.anime_media_details_streaming_links_label, entry.streamingLinks)
    }

    private fun LazyListScope.otherLinksSection(entry: Entry) {
        linksSection(R.string.anime_media_details_other_links_label, entry.otherLinks)
    }

    private fun LazyListScope.linksSection(@StringRes headerRes: Int, links: List<Entry.Link>) {
        if (links.isEmpty()) return

        item {
            SectionHeader(stringResource(headerRes))
        }

        item {
            val uriHandler = LocalUriHandler.current
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
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
                                    modifier = Modifier.size(24.dp),
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

    private fun Modifier.bottomFadingEdge(expanded: Boolean, firstStop: Float = 0.8f) =
        graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                val brush = Brush.verticalGradient(
                    firstStop to Color.Black,
                    1f to Color.Transparent,
                )
                onDrawWithContent {
                    drawContent()
                    if (!expanded) {
                        drawRect(brush, blendMode = BlendMode.DstIn)
                    }
                }
            }

    data class Entry(
        val mediaId: String,
        val media: Media,
    ) {
        val id = EntryId("media", mediaId)
        val titlesUnique
            get() = media.title?.run {
                listOfNotNull(romaji, english, native).distinct()
            }
        val description get() = media.description

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

        val genres = media.genres?.filterNotNull().orEmpty().map(::Genre)

        val characters = media.characters?.run {
            nodes?.filterNotNull()?.map { node ->
                val edge = edges?.find { it?.node?.id == node.id }
                Character(
                    id = node.id.toString(),
                    name = node.name?.userPreferred,
                    image = node.image?.large,
                    languageToVoiceActor = edge?.voiceActors?.filterNotNull()
                        ?.mapNotNull {
                            it.languageV2?.let { language ->
                                language to Character.VoiceActor(
                                    id = it.id.toString(),
                                    name = it.name?.userPreferred?.replace(Regex("\\s"), " "),
                                    image = it.image?.large,
                                    language = language,
                                )
                            }
                        }
                        ?.associate { it }
                        .orEmpty()
                )
            }
        }.orEmpty().distinctBy { it.id }

        val staff = media.staff?.run {
            nodes?.filterNotNull()?.map { node ->
                val edge = edges?.find { it?.node?.id == node.id }
                Staff(
                    id = node.id.toString(),
                    name = node.name?.userPreferred,
                    image = node.image?.large,
                    role = edge?.role,
                )
            }
        }.orEmpty().distinctBy { it.id }

        val relations = media.relations?.edges?.filterNotNull()
            ?.mapNotNull {
                val node = it.node ?: return@mapNotNull null
                val relation = it.relationType ?: return@mapNotNull null
                Relation(it.id.toString(), relation, AnimeMediaListRow.MediaEntry(node))
            }
            .orEmpty()
            .sortedBy { RELATION_SORT_ORDER.indexOf(it.relation) }

        val recommendations = media.recommendations?.edges?.filterNotNull()
            ?.mapNotNull {
                val node = it.node ?: return@mapNotNull null
                val media = node.mediaRecommendation ?: return@mapNotNull null
                Recommendation(node.id.toString(), node.rating, AnimeMediaListRow.MediaEntry(media))
            }
            .orEmpty()

        val tags = media.tags?.filterNotNull()?.map(::AnimeMediaTagEntry).orEmpty()

        private val links = media.externalLinks?.filterNotNull()?.mapNotNull {
            Link(
                id = it.id.toString(),
                type = it.type,
                url = it.url ?: return@mapNotNull null,
                icon = it.icon,
                site = it.site,
                color = it.color
                    ?.let(com.thekeeperofpie.artistalleydatabase.compose.ColorUtils::hexToColor)
                    ?.multiplyCoerceSaturation(0.75f, 0.75f)
            )
        }.orEmpty()

        val socialLinks = links.filter { it.type == ExternalLinkType.SOCIAL }
        val streamingLinks = links.filter { it.type == ExternalLinkType.STREAMING }
        val otherLinks = links.filter {
            it.type != ExternalLinkType.SOCIAL && it.type != ExternalLinkType.STREAMING
        }

        val studios = media.studios?.run {
            nodes?.filterNotNull()?.map { node ->
                val edge = edges?.find { it?.node?.id == node.id }
                Studio(
                    id = node.id.toString(),
                    name = node.name,
                    main = edge?.isMain ?: false,
                )
            }
        }.orEmpty()
            .sortedByDescending { it.main }

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
                            list += Media.Stats.ScoreDistribution(
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

        data class Genre(
            val name: String,
            val color: Color = MediaUtils.genreColor(name),
        )

        data class Character(
            val id: String,
            val name: String?,
            val image: String?,
            val languageToVoiceActor: Map<String, VoiceActor>,
        ) {
            data class VoiceActor(
                val id: String,
                val name: String?,
                val image: String?,
                val language: String,
            )
        }

        data class Relation(
            val id: String,
            val relation: MediaRelation,
            val entry: AnimeMediaListRow.Entry,
        )

        data class Recommendation(
            val id: String,
            // TODO: Actually surface rating
            val rating: Int?,
            val entry: AnimeMediaListRow.Entry,
        )

        data class Link(
            val id: String,
            val type: ExternalLinkType?,
            val url: String,
            val icon: String?,
            val site: String,
            val color: Color?,
            val textColor: Color? = color
                ?.let(com.thekeeperofpie.artistalleydatabase.compose.ColorUtils::bestTextColor),
        )

        data class Staff(
            val id: String,
            val name: String?,
            val image: String?,
            val role: String?,
        )

        data class Studio(
            val id: String,
            val name: String,
            val main: Boolean,
        )
    }
}
