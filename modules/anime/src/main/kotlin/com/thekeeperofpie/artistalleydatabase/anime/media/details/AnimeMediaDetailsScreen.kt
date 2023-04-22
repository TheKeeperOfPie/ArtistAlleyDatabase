package com.thekeeperofpie.artistalleydatabase.anime.media.details

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.view.View
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.ColorUtils
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.MediaDetailsQuery.Data.Media
import com.anilist.type.MediaRelation
import com.thekeeperofpie.artistalleydatabase.android_utils.AnimationUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anime.AppMediaPlayer
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeTheme
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.AccelerateEasing
import com.thekeeperofpie.artistalleydatabase.compose.AssistChip
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.assistChipColors
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid
import de.charlex.compose.HtmlText
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeMediaDetailsScreen {

    private const val RELATIONS_ABOVE_FOLD = 3
    private const val RECOMMENDATIONS_ABOVE_FOLD = 5
    private const val SONGS_ABOVE_FOLD = 3

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
        onTagClicked: (tagId: String, tagName: String) -> Unit = { _, _ -> },
        onTagLongClicked: (String) -> Unit = {},
        onMediaClicked: (AnimeMediaListRow.Entry) -> Unit = {},
        errorRes: @Composable () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = {},
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        @Suppress("NAME_SHADOWING")
        Scaffold(
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
                SnackbarErrorText(
                    errorRes()?.first,
                    errorRes()?.second,
                    onErrorDismiss = onErrorDismiss
                )
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(bottom = 12.dp)
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
                    onTagClicked = onTagClicked,
                    onTagLongClicked = onTagLongClicked,
                    onMediaClicked = onMediaClicked,
                    relationsExpanded = { relationsExpanded },
                    onRelationsExpandedToggled = { relationsExpanded = it },
                    recommendationsExpanded = { recommendationsExpanded },
                    onRecommendationsExpandedToggled = { recommendationsExpanded = it },
                    songsExpanded = { songsExpanded },
                    onSongsExpandedToggled = { songsExpanded = it },
                )
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
        onTagClicked: (tagId: String, tagName: String) -> Unit,
        onTagLongClicked: (String) -> Unit,
        onMediaClicked: (AnimeMediaListRow.Entry) -> Unit,
        relationsExpanded: () -> Boolean,
        onRelationsExpandedToggled: (Boolean) -> Unit,
        recommendationsExpanded: () -> Boolean,
        onRecommendationsExpandedToggled: (Boolean) -> Unit,
        songsExpanded: () -> Boolean,
        onSongsExpandedToggled: (Boolean) -> Unit,
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

        tagSection(
            entry = entry,
            onTagClicked = onTagClicked,
            onTagLongClicked = onTagLongClicked,
        )

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
                            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                            contentDescription = stringResource(R.string.anime_media_cover_image),
                            modifier = Modifier
                                .wrapContentWidth()
                                .height(256.dp)
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
                            AutoHeightText(
                                text = when (val index = preferredTitle) {
                                    null -> null
                                    else -> entry?.titlesUnique?.get(index)
                                } ?: titleText(),
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
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
            style = MaterialTheme.typography.labelLarge,
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
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
        if (entry.characters.isNotEmpty()) {
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
                        val defaultTextColor = MaterialTheme.typography.bodyMedium.color

                        val characterId = it.id
                        val colors = colorMap[characterId]

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
                            onClick = { onCharacterClicked(it.id) },
                            colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
                        ) {
                            Box {
                                val voiceActorImage = (it.languageToVoiceActor["Japanese"]
                                    ?: it.languageToVoiceActor.values.firstOrNull())?.image

                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(it.image)
                                        .allowHardware(false)
                                        .build(),
                                    contentScale = ContentScale.Crop,
                                    fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                                    contentDescription = stringResource(
                                        R.string.anime_media_character_image
                                    ),
                                    onSuccess = {
                                        if (!colorMap.containsKey(characterId)) {
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
                                                                if (voiceActorImage == null) {
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
                                                                colorMap[characterId] =
                                                                    Color(swatch.rgb) to
                                                                            Color(swatch.bodyTextColor)
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

                                if (voiceActorImage != null) {
                                    var showVoiceActor by remember { mutableStateOf(true) }
                                    val alpha by animateFloatAsState(
                                        if (showVoiceActor) 1f else 0f,
                                        label = "Character card voice actor fade",
                                    )

                                    if (showVoiceActor) {
                                        var showBorder by remember { mutableStateOf(false) }
                                        val clipShape = RoundedCornerShape(topStart = 8.dp)
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(voiceActorImage)
                                                .crossfade(false)
                                                .listener(onError = { _, _ ->
                                                    showVoiceActor = false
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
                                                .align(Alignment.BottomEnd)
                                                .clip(clipShape)
                                                .run {
                                                    if (showBorder) {
                                                        border(
                                                            width = 1.dp,
                                                            color = Color.Black,
                                                            shape = clipShape
                                                        )
                                                    } else this
                                                }
                                                .alpha(alpha)
                                        )
                                    }
                                }
                            }

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
                TwoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_format_label),
                    bodyOne = stringResource(entry.formatTextRes),
                    labelTwo = stringResource(R.string.anime_media_details_status_label),
                    bodyTwo = stringResource(entry.statusTextRes),
                    showDividerAbove = false,
                )

                TwoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_episodes_label),
                    bodyOne = entry.episodes?.toString(),
                    labelTwo = stringResource(R.string.anime_media_details_duration_label),
                    bodyTwo = entry.duration?.let {
                        stringResource(R.string.anime_media_details_duration_minutes, it)
                    },
                )

                TwoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_volumes_label),
                    bodyOne = entry.volumes?.toString(),
                    labelTwo = stringResource(R.string.anime_media_details_chapters_label),
                    bodyTwo = entry.chapters?.toString(),
                )

                TwoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_source_label),
                    bodyOne = stringResource(entry.source.toTextRes()),
                    labelTwo = stringResource(R.string.anime_media_details_season_label),
                    bodyTwo = MediaUtils.formatSeasonYear(entry.season, entry.seasonYear),
                )

                val context = LocalContext.current

                val startDateFormatted = entry.startDate?.let {
                    remember { MediaUtils.formatDateTime(context, it.year, it.month, it.day) }
                }
                val endDateFormatted = entry.endDate?.let {
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
                    bodyOne = entry.averageScore?.toString(),
                    labelTwo = stringResource(R.string.anime_media_details_mean_score_label),
                    bodyTwo = entry.meanScore?.toString(),
                )

                TwoColumnInfoText(
                    labelOne = stringResource(R.string.anime_media_details_popularity_label),
                    bodyOne = entry.popularity?.toString(),
                    labelTwo = stringResource(R.string.anime_media_details_favorites_label),
                    bodyTwo = entry.favorites?.toString(),
                )

                if (entry.allSynonyms.size > 1) {
                    var expanded by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .heightIn(max = if (expanded) Dp.Unspecified else 120.dp)
                            .bottomFadingEdge(expanded)
                            .clickable { expanded = !expanded }
                    ) {
                        Divider()

                        Row {
                            Text(
                                text = stringResource(R.string.anime_media_details_synonyms_label),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.surfaceTint,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)
                            )

                            TrailingDropdownIconButton(
                                expanded = expanded,
                                contentDescription = stringResource(
                                    R.string.anime_media_details_synonyms_expand_content_description
                                ),
                                onClick = { expanded = !expanded },
                            )
                        }

                        entry.allSynonyms.forEachIndexed { index, synonym ->
                            if (index != 0) {
                                Divider(modifier = Modifier.padding(start = 16.dp))
                            }

                            val topPadding = if (index == 0) {
                                0.dp
                            } else {
                                8.dp
                            }

                            val bottomPadding = if (index == entry.allSynonyms.size - 1) {
                                12.dp
                            } else {
                                8.dp
                            }

                            Text(
                                text = synonym,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = topPadding,
                                        bottom = bottomPadding,
                                    )
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ColumnScope.TwoColumnInfoText(
        labelOne: String, bodyOne: String?,
        labelTwo: String, bodyTwo: String?,
        showDividerAbove: Boolean = true
    ) {
        if (bodyOne != null && bodyTwo != null) {
            if (showDividerAbove) {
                Divider()
            }
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(modifier = Modifier.weight(1f)) {
                    InfoText(label = labelOne, body = bodyOne, showDividerAbove = false)
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(DividerDefaults.Thickness)
                        .background(color = DividerDefaults.color)
                )
                Column(modifier = Modifier.weight(1f)) {
                    InfoText(label = labelTwo, body = bodyTwo, showDividerAbove = false)
                }
            }
        } else if (bodyOne != null) {
            InfoText(label = labelOne, body = bodyOne, showDividerAbove = showDividerAbove)
        } else if (bodyTwo != null) {
            InfoText(label = labelTwo, body = bodyTwo, showDividerAbove = showDividerAbove)
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

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.surfaceTint,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp)
        )
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
            onClick = { onExpandedToggle(entry.id, !state.expanded()) },
            modifier = modifier.animateContentSize(),
        ) {
            // TODO: This doesn't line up perfectly (too much space between label and title),
            //  consider migrating to ConstraintLayout
            Row {
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
                            PlayerView(it).apply {
                                @SuppressLint("UnsafeOptInUsageError")
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
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

            val artists = entry.artists
            if (artists.isNotEmpty()) {
                artists.forEachIndexed { index, artist ->
                    if (index == 0) {
                        Divider()
                    } else {
                        Divider(modifier = Modifier.padding(start = 64.dp))
                    }
                    Row(
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .padding(start = 64.dp)
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
        itemContent: @Composable (T, paddingBottom: Dp) -> Unit,
    ) {
        if (values.isNotEmpty()) {
            item {
                SectionHeader(
                    text = stringResource(titleRes),
                    modifier = Modifier.clickable { onExpandedToggled(!expanded()) }
                )
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
        val season = media.season
        val seasonYear = media.seasonYear

        val formatTextRes = media.format.toTextRes()
        val statusTextRes = media.status.toTextRes()

        val episodes = media.episodes
        val duration = media.duration
        val volumes = media.volumes
        val chapters = media.chapters
        val source = media.source
        val startDate = media.startDate
        val endDate = media.endDate
        val averageScore = media.averageScore
        val meanScore = media.meanScore
        val popularity = media.popularity
        val favorites = media.favourites
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
        }.orEmpty()

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
    }
}
