package com.thekeeperofpie.artistalleydatabase.anime.media.details

import android.view.View
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.ThumbDownAlt
import androidx.compose.material.icons.filled.ThumbUpAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
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
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anime.character.charactersSection
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toColor
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toStatusIcon
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toStatusText
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.AnimeMediaEditBottomSheet
import com.thekeeperofpie.artistalleydatabase.anime.media.mediaListSection
import com.thekeeperofpie.artistalleydatabase.anime.staff.DetailsStaff
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsSubsectionHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.ExpandableListInfoText
import com.thekeeperofpie.artistalleydatabase.anime.ui.InfoText
import com.thekeeperofpie.artistalleydatabase.anime.ui.descriptionSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.twoColumnInfoText
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeTheme
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.AssistChip
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.BarChart
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.PieChart
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.assistChipColors
import com.thekeeperofpie.artistalleydatabase.compose.multiplyCoerceSaturation
import com.thekeeperofpie.artistalleydatabase.compose.optionalClickable
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.showFloatingActionButtonOnVerticalScroll
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt

@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeMediaDetailsScreen {

    private const val RELATIONS_ABOVE_FOLD = 3
    private const val RECOMMENDATIONS_ABOVE_FOLD = 5
    private const val SONGS_ABOVE_FOLD = 3
    private const val STREAMING_EPISODES_ABOVE_FOLD = 3
    private const val REVIEWS_ABOVE_FOLD = 3

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
        viewModel: AnimeMediaDetailsViewModel = hiltViewModel(),
        color: () -> Color? = { Color.Transparent },
        coverImage: @Composable () -> String? = { null },
        coverImageWidthToHeightRatio: Float = 1f,
        bannerImage: @Composable () -> String? = { null },
        title: @Composable () -> String = { "Title" },
        subtitle: @Composable () -> String? = { "TV - Releasing - 2023" },
        nextEpisode: @Composable () -> Int? = { null },
        nextEpisodeAiringAt: @Composable () -> Int? = { null },
        entry: @Composable () -> Entry? = { null },
        onGenreLongClick: (String) -> Unit = {},
        onCharacterLongClick: (String) -> Unit = {},
        onStaffLongClick: (String) -> Unit = {},
        onTagLongClick: (String) -> Unit = {},
        navigationCallback: AnimeNavigator.NavigationCallback,
        listEntry: @Composable () -> MediaDetailsListEntry?,
        scoreFormat: @Composable () -> ScoreFormat,
        errorRes: @Composable () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = {},
    ) {
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Hidden,
                confirmValueChange = viewModel::onEditSheetValueChange,
                skipHiddenState = false,
            )
        )

        val bottomSheetShowing = viewModel.editData.showing
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
                    viewModel.editData.showing = false
                }
            }
        }

        val scope = rememberCoroutineScope()
        BackHandler(
            enabled = bottomSheetScaffoldState.bottomSheetState.currentValue != SheetValue.Hidden
        ) {
            if (viewModel.onEditSheetValueChange(SheetValue.Hidden)) {
                scope.launch {
                    bottomSheetScaffoldState.bottomSheetState.hide()
                }
            }
        }

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val lazyListState = rememberLazyListState()
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
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
                        coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
                        bannerImage = bannerImage,
                        titleText = title,
                        subtitleText = subtitle,
                        nextEpisode = nextEpisode,
                        nextEpisodeAiringAt = nextEpisodeAiringAt,
                        colorCalculationState = colorCalculationState,
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
                } else {
                    val editData = viewModel.editData
                    if (editData.errorRes != null) {
                        SnackbarErrorText(
                            editData.errorRes?.first,
                            editData.errorRes?.second,
                            onErrorDismiss = { editData.errorRes = null },
                        )
                    } else {
                        // Bottom sheet requires at least one measurable component
                        Spacer(modifier = Modifier.size(0.dp))
                    }
                }
            },
            sheetContent = {
                AnimeMediaEditBottomSheet(
                    editData = viewModel.editData,
                    id = { listEntry()?.id?.toString() },
                    type = { entry()?.media?.type },
                    progressMax = { entry()?.media?.run { episodes ?: volumes } ?: 0 },
                    scoreFormat = scoreFormat,
                    onDateChange = viewModel::onDateChange,
                    onStatusChange = viewModel::onStatusChange,
                    onClickDelete = viewModel::onClickDelete,
                    onClickSave = viewModel::onClickSave,
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

                        val showFloatingActionButton =
                            lazyListState.showFloatingActionButtonOnVerticalScroll()
                        AnimatedVisibility(
                            visible = showFloatingActionButton,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            val containerColor = color()
                                ?: FloatingActionButtonDefaults.containerColor
                            val contentColor =
                                ComposeColorUtils.bestTextColor(containerColor)
                                    ?: contentColorFor(containerColor)

                            ExtendedFloatingActionButton(
                                text = {
                                    val progress = if (media.type == MediaType.ANIME) {
                                        listEntry?.progress
                                    } else {
                                        listEntry?.progressVolumes
                                    } ?: 0
                                    val progressMax = if (media.type == MediaType.ANIME) {
                                        media.episodes
                                    } else {
                                        media.volumes
                                    } ?: 1

                                    Text(
                                        listEntry?.status.toStatusText(
                                            mediaType = media.type,
                                            progress = progress,
                                            progressMax = progressMax
                                        )
                                    )
                                },
                                icon = {
                                    val (vector, contentDescription) = listEntry?.status
                                        .toStatusIcon(mediaType = media.type)
                                    Icon(
                                        imageVector = vector,
                                        contentDescription = stringResource(contentDescription),
                                    )
                                },
                                expanded = listEntry?.status
                                    ?.takeUnless { it == MediaListStatus.UNKNOWN__ }
                                    ?.takeIf { expanded } != null,
                                containerColor = containerColor,
                                contentColor = contentColor,
                                onClick = {
                                    if (showFloatingActionButton) {
                                        viewModel.editData.showing = true
                                    }
                                },
                            )
                        }
                    }
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                val entry = entry()
                val expandedState = rememberExpandedState()

                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    content(
                        viewModel = viewModel,
                        entry = entry,
                        errorRes = errorRes,
                        onGenreLongClick = onGenreLongClick,
                        onCharacterLongClick = onCharacterLongClick,
                        onStaffLongClick = onStaffLongClick,
                        onTagLongClick = onTagLongClick,
                        navigationCallback = navigationCallback,
                        expandedState = expandedState,
                        colorCalculationState = colorCalculationState,
                    )
                }
            }
        }

        if (viewModel.editData.showConfirmClose) {
            AlertDialog(
                onDismissRequest = { viewModel.editData.showConfirmClose = false },
                title = { Text(stringResource(R.string.anime_media_edit_confirm_close_title)) },
                text = { Text(stringResource(R.string.anime_media_edit_confirm_close_text)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.editData.showConfirmClose = false
                        viewModel.onClickSave()
                    }) {
                        Text(stringResource(UtilsStringR.yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.editData.showConfirmClose = false
                        viewModel.editData.showing = false
                        scope.launch {
                            bottomSheetScaffoldState.bottomSheetState.hide()
                        }
                    }) {
                        Text(stringResource(UtilsStringR.no))
                    }
                },
            )
        }
    }

    private fun LazyListScope.content(
        viewModel: AnimeMediaDetailsViewModel,
        entry: Entry?,
        errorRes: @Composable () -> Pair<Int, Exception?>? = { null },
        onGenreLongClick: (String) -> Unit,
        onCharacterLongClick: (String) -> Unit,
        onStaffLongClick: (String) -> Unit,
        onTagLongClick: (String) -> Unit,
        navigationCallback: AnimeNavigator.NavigationCallback,
        expandedState: ExpandedState,
        colorCalculationState: ColorCalculationState,
    ) {
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
                    val errorRes = errorRes()
                    AnimeMediaListScreen.Error(
                        errorTextRes = errorRes?.first,
                        exception = errorRes?.second,
                    )
                }
            }
            return
        }
        genreSection(
            entry = entry,
            onGenreClick = navigationCallback::onGenreClick,
            onGenreLongClick = onGenreLongClick,
        )

        descriptionSection(
            titleTextRes = R.string.anime_media_details_description_label,
            htmlText = entry.media.description,
            expanded = expandedState::description,
            onExpandedChange = { expandedState.description = it },
        )

        charactersSection(
            titleRes = R.string.anime_media_details_characters_label,
            characters = entry.characters,
            onCharacterClick = navigationCallback::onCharacterClick,
            onCharacterLongClick = onCharacterLongClick,
            onStaffClick = navigationCallback::onStaffClick,
            colorCalculationState = colorCalculationState,
        )

        relationsSection(
            entry = entry,
            relationsExpanded = expandedState::relations,
            onRelationsExpandedChange = { expandedState.relations = it },
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            onTagLongClick = onTagLongClick,
        )

        infoSection(entry)

        songsSection(
            viewModel = viewModel,
            songsExpanded = expandedState::songs,
            onSongsExpandedChange = { expandedState.songs = it },
        )

        cdsSection(viewModel.cdEntries)

        staffSection(
            titleRes = R.string.anime_media_details_staff_label,
            staff = entry.staff,
            onStaffClick = navigationCallback::onStaffClick,
            onStaffLongClick = onStaffLongClick,
            colorCalculationState = colorCalculationState,
        )

        statsSection(entry)

        tagSection(
            entry = entry,
            onTagClick = navigationCallback::onTagClick,
            onTagLongClick = onTagLongClick,
            colorCalculationState = colorCalculationState,
        )

        trailerSection(
            entry = entry,
            playbackPosition = { viewModel.trailerPlaybackPosition },
            onPlaybackPositionUpdate = { viewModel.trailerPlaybackPosition = it },
        )

        streamingEpisodesSection(
            entry = entry,
            expanded = expandedState::streamingEpisodes,
            onExpandedChange = { expandedState.streamingEpisodes = it },
            hidden = expandedState::streamingEpisodesHidden,
            onHiddenChange = { expandedState.streamingEpisodesHidden = it },
        )

        socialLinksSection(entry = entry)
        streamingLinksSection(entry = entry)
        otherLinksSection(entry = entry)

        recommendationsSection(
            entry = entry,
            recommendationsExpanded = expandedState::recommendations,
            onRecommendationsExpandedChange = { expandedState.recommendations = it },
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            onTagLongClick = onTagLongClick,
        )

        reviewsSection(
            reviews = entry.media.reviews?.nodes?.filterNotNull().orEmpty(),
            expanded = expandedState::reviews,
            onExpandedChange = { expandedState.reviews = it },
            onUserClick = navigationCallback::onUserClick,
        )
    }

    @Composable
    private fun Header(
        entry: @Composable () -> Entry?,
        progress: Float,
        color: () -> Color?,
        coverImage: @Composable () -> String?,
        coverImageWidthToHeightRatio: Float,
        bannerImage: @Composable () -> String?,
        titleText: @Composable () -> String,
        subtitleText: @Composable () -> String?,
        nextEpisode: @Composable () -> Int?,
        nextEpisodeAiringAt: @Composable () -> Int?,
        colorCalculationState: ColorCalculationState,
    ) {
        val entry = entry()
        var preferredTitle by remember { mutableStateOf<Int?>(null) }
        CoverAndBannerHeader(
            pinnedHeight = 180.dp,
            progress = progress,
            color = color,
            coverImage = coverImage,
            coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
            bannerImage = bannerImage,
            onClickEnabled = (entry?.titlesUnique?.size ?: 0) > 1,
            onClick = {
                preferredTitle =
                    ((preferredTitle ?: 0) + 1) % (entry?.titlesUnique?.size ?: 1)
            },
            coverImageOnSuccess = { success ->
                entry?.id?.valueId?.let { entryId ->
                    ComposeColorUtils.calculatePalette(entryId, success, colorCalculationState)
                }
            }
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

    private fun LazyListScope.genreSection(
        entry: Entry,
        onGenreClick: (String) -> Unit,
        onGenreLongClick: (String) -> Unit
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
                            onClick = { onGenreClick(it.name) },
                            onLongClickLabel = stringResource(
                                R.string.anime_media_tag_long_click_content_description
                            ),
                            onLongClick = { onGenreLongClick(it.name) },
                            label = { AutoHeightText(it.name) },
                            colors = assistChipColors(containerColor = it.color),
                        )
                    }
                }
            }
        }
    }

    private fun LazyListScope.relationsSection(
        entry: Entry,
        relationsExpanded: () -> Boolean,
        onRelationsExpandedChange: (Boolean) -> Unit,
        onTagLongClick: (String) -> Unit,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        mediaListSection(
            titleRes = R.string.anime_media_details_relations_label,
            values = entry.relations,
            valueToEntry = { it.entry },
            aboveFold = RELATIONS_ABOVE_FOLD,
            expanded = relationsExpanded,
            onExpandedChange = onRelationsExpandedChange,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            onTagLongClick = onTagLongClick,
            label = { RelationLabel(it.relation) },
        )
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
                val media = entry.media
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

                twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_average_score_label),
                    bodyOne = media.averageScore?.toString(),
                    labelTwo = stringResource(R.string.anime_media_details_mean_score_label),
                    bodyTwo = media.meanScore?.toString(),
                )

                twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_popularity_label),
                    bodyOne = media.popularity?.toString(),
                    labelTwo = stringResource(R.string.anime_media_details_favorites_label),
                    bodyTwo = media.favourites?.toString(),
                )

                val uriHandler = LocalUriHandler.current
                twoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_trending_label),
                    bodyOne = media.trending?.toString(),
                    labelTwo = "",
                    bodyTwo = null,
                )

                twoColumnInfoText(
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

    private fun LazyListScope.songsSection(
        viewModel: AnimeMediaDetailsViewModel,
        songsExpanded: () -> Boolean,
        onSongsExpandedChange: (Boolean) -> Unit,
    ) {
        val animeSongs = viewModel.animeSongs ?: return
        listSection(
            titleRes = R.string.anime_media_details_songs_label,
            values = animeSongs.entries,
            aboveFold = SONGS_ABOVE_FOLD,
            expanded = songsExpanded,
            onExpandedChange = onSongsExpandedChange,
        ) { item, paddingBottom ->
            AnimeThemeRow(
                viewModel = viewModel,
                entry = item,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
            )
        }
    }

    @Composable
    private fun AnimeThemeRow(
        viewModel: AnimeMediaDetailsViewModel,
        entry: AnimeMediaDetailsViewModel.AnimeSongEntry,
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
            modifier = modifier.animateContentSize(),
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
                                        R.string.anime_media_character_image_content_description
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
            DetailsSectionHeader(stringResource(R.string.anime_media_details_cds_label))
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
        onRecommendationsExpandedChange: (Boolean) -> Unit,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
        onTagLongClick: (String) -> Unit,
    ) {
        mediaListSection(
            titleRes = R.string.anime_media_details_recommendations_label,
            values = entry.recommendations,
            valueToEntry = { it.entry },
            aboveFold = RECOMMENDATIONS_ABOVE_FOLD,
            expanded = recommendationsExpanded,
            onExpandedChange = onRecommendationsExpandedChange,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            onTagLongClick = onTagLongClick,
        )
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
            DetailsSectionHeader(stringResource(R.string.anime_media_details_stats_label))
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
                                MediaListStatus.CURRENT -> R.string.anime_media_details_status_distribution_current
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

    private fun LazyListScope.tagSection(
        entry: Entry,
        onTagClick: (tagId: String, tagName: String) -> Unit = { _, _ -> },
        onTagLongClick: (tagId: String) -> Unit = {},
        colorCalculationState: ColorCalculationState,
    ) {
        if (entry.tags.isNotEmpty()) {
            item {
                DetailsSectionHeader(stringResource(R.string.anime_media_details_tags_label))
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    entry.tags.forEach {
                        val (containerColor, textColor) =
                            colorCalculationState.getColors(entry.id.valueId)
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
                            onTagClick = onTagClick,
                            onTagLongClick = onTagLongClick,
                            containerColor = containerColor,
                            textColor = textColor,
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
            DetailsSectionHeader(stringResource(R.string.anime_media_details_trailer_label))
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
        onExpandedChange: (Boolean) -> Unit,
        hidden: () -> Boolean,
        onHiddenChange: (Boolean) -> Unit,
    ) {
        val streamingEpisodes = entry.media.streamingEpisodes?.filterNotNull()
            ?.takeIf { it.isNotEmpty() } ?: return
        listSection(
            titleRes = R.string.anime_media_details_streaming_episodes_label,
            values = streamingEpisodes,
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
            DetailsSectionHeader(stringResource(headerRes))
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

    private fun LazyListScope.reviewsSection(
        reviews: List<Media.Reviews.Node>,
        expanded: () -> Boolean,
        onExpandedChange: (Boolean) -> Unit,
        onUserClick: (String) -> Unit,
    ) {
        if (reviews.isEmpty()) return
        listSection(
            titleRes = R.string.anime_media_details_reviews_label,
            values = reviews,
            aboveFold = REVIEWS_ABOVE_FOLD,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
        ) { item, paddingBottom ->
            val uriHandler = LocalUriHandler.current
            ElevatedCard(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
                    .clickable { uriHandler.openUri(AniListUtils.reviewUrl(item.id.toString())) },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .height(IntrinsicSize.Min)
                        .heightIn(min = 72.dp)
                ) {
                    AsyncImage(
                        model = item.user?.avatar?.medium,
                        contentScale = ContentScale.FillHeight,
                        contentDescription = stringResource(
                            R.string.anime_media_details_reviews_user_avatar_content_description
                        ),
                        modifier = Modifier
                            .heightIn(min = 64.dp)
                            .padding(vertical = 10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onUserClick(item.user!!.id.toString()) },
                    )

                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = item.user?.name.orEmpty(),
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .align(Alignment.CenterStart)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.Top)
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .height(24.dp),
                    ) {
                        val score = item.score
                        if (score != null) {
                            AutoHeightText(
                                text = score.toString(),
                                style = MaterialTheme.typography.labelLarge,
                            )

                            val iconTint = remember(score) {
                                when {
                                    score > 80 -> Color.Green
                                    score > 70 -> Color.Yellow
                                    score > 50 -> Color(0xFFFF9000) // Orange
                                    else -> Color.Red
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.BarChart,
                                contentDescription = stringResource(
                                    R.string.anime_media_rating_icon_content_description
                                ),
                                tint = iconTint,
                            )
                        }

                        val ratingAmount = item.ratingAmount ?: 0
                        val rating = item.rating
                        if (rating != null) {
                            AutoHeightText(
                                text = rating.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(start = 8.dp),
                            )

                            val ratio = rating / ratingAmount.toFloat()
                            val iconTint = when {
                                ratio > 0.6f -> Color.Green
                                ratio > 0.4f -> Color.Yellow
                                ratio > 0.2f -> Color(0xFFFF9000) // Orange
                                else -> Color.Red
                            }
                            Icon(
                                imageVector = if (ratio > 0.4f) {
                                    Icons.Filled.ThumbUpAlt
                                } else {
                                    Icons.Filled.ThumbDownAlt
                                },
                                contentDescription = stringResource(
                                    R.string.anime_media_details_reviews_rating_upvote_content_description
                                ),
                                tint = iconTint,
                            )
                        }

                        if (ratingAmount > 0) {
                            AutoHeightText(
                                text = ratingAmount.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(start = 8.dp),
                            )

                            Icon(
                                imageVector = when {
                                    ratingAmount > 100 -> Icons.Filled.PeopleAlt
                                    ratingAmount > 50 -> Icons.Outlined.PeopleAlt
                                    ratingAmount > 10 -> Icons.Filled.Person
                                    else -> Icons.Filled.PersonOutline
                                },
                                contentDescription = stringResource(
                                    R.string.anime_media_details_reviews_rating_amount_content_description
                                ),
                            )
                        }
                    }
                }

                Text(
                    text = item.summary.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }

    data class Entry(
        val mediaId: String,
        val media: Media,
    ) {
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

        val genres = media.genres?.filterNotNull().orEmpty().map(::Genre)

        val characters = CharacterUtils.toDetailsCharacters(media.characters?.edges)

        val staff = media.staff?.edges?.filterNotNull()?.mapNotNull {
            val role = it.role
            it.node?.let {
                DetailsStaff(
                    id = it.id.toString(),
                    name = it.name?.userPreferred,
                    image = it.image?.large,
                    role = role,
                    staff = it,
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
                    url = AniListUtils.mediaUrl(it, mediaId),
                    site = "AniList",
                )
            }
        )

        val studios = media.studios?.edges?.filterNotNull()?.map {
            Studio(
                id = it.node?.id.toString(),
                name = it.node?.name.orEmpty(),
                main = it.isMain,
            )
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
            val site: String,
            val icon: String? = null,
            val color: Color? = null,
            val textColor: Color? = color
                ?.let(ComposeColorUtils::bestTextColor),
        )

        data class Studio(
            val id: String,
            val name: String,
            val main: Boolean,
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
                it.reviews
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
        streamingEpisodesHidden: Boolean = false,
        reviews: Boolean = false,
    ) {
        var description by mutableStateOf(description)
        var relations by mutableStateOf(relations)
        var recommendations by mutableStateOf(recommendations)
        var songs by mutableStateOf(songs)
        var streamingEpisodes by mutableStateOf(streamingEpisodes)
        var streamingEpisodesHidden by mutableStateOf(streamingEpisodesHidden)
        var reviews by mutableStateOf(reviews)
    }
}
