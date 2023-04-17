package com.thekeeperofpie.artistalleydatabase.anime.media.details

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.atLeast
import androidx.constraintlayout.compose.atMost
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anilist.MediaDetailsQuery.Data.Media
import com.anilist.type.MediaRelation
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.compose.AssistChip
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.assistChipColors
import de.charlex.compose.HtmlText
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeMediaDetailsScreen {

    private const val RELATIONS_ABOVE_FOLD = 3

    @Composable
    operator fun invoke(
        onClickBack: () -> Unit = {},
        loading: @Composable () -> Boolean = { false },
        color: () -> Color? = { Color.Transparent },
        coverImage: @Composable () -> String? = { null },
        bannerImage: @Composable () -> String? = { null },
        title: @Composable () -> String = { "Title" },
        entry: @Composable () -> Entry? = { null },
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
        Scaffold(
            snackbarHost = {
                SnackbarErrorText(
                    errorRes()?.first,
                    errorRes()?.second,
                    onErrorDismiss = onErrorDismiss
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 12.dp)
            ) {
                if (loading()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    @Suppress("NAME_SHADOWING")
                    val entry = entry()
                    if (entry == null) {
                        Error()
                    } else {
                        Content(
                            entry = entry,
                            color = color,
                            coverImage = coverImage,
                            bannerImage = bannerImage,
                            titleText = title,
                            onGenreClicked = onGenreClicked,
                            onGenreLongClicked = onGenreLongClicked,
                            onCharacterClicked = onCharacterClicked,
                            onCharacterLongClicked = onCharacterLongClicked,
                            onTagClicked = onTagClicked,
                            onTagLongClicked = onTagLongClicked,
                            onMediaClicked = onMediaClicked,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun Error() {
        Text("TODO: Error state")
    }

    @Composable
    private fun Content(
        entry: Entry,
        color: () -> Color?,
        coverImage: @Composable () -> String?,
        bannerImage: @Composable () -> String?,
        titleText: @Composable () -> String,
        onGenreClicked: (String) -> Unit,
        onGenreLongClicked: (String) -> Unit,
        onCharacterClicked: (String) -> Unit,
        onCharacterLongClicked: (String) -> Unit,
        onTagClicked: (tagId: String, tagName: String) -> Unit,
        onTagLongClicked: (String) -> Unit,
        onMediaClicked: (AnimeMediaListRow.Entry) -> Unit,
    ) {
        Header(
            entry = entry,
            color = color,
            coverImage = coverImage,
            bannerImage = bannerImage,
            titleText = titleText,
        )

        GenreSection(
            entry = entry,
            onGenreClicked = onGenreClicked,
            onGenreLongClicked = onGenreLongClicked,
        )

        DescriptionSection(entry)

        CharactersSection(
            entry = entry,
            onCharacterClicked = onCharacterClicked,
            onCharacterLongClicked = onCharacterLongClicked,
        )

        RelationsSection(
            entry = entry,
            onMediaClicked = onMediaClicked,
            onTagClicked = onTagClicked,
            onTagLongClicked = onTagLongClicked,
        )

        TagSection(
            entry = entry,
            onTagClicked = onTagClicked,
            onTagLongClicked = onTagLongClicked,
        )
    }

    @Composable
    private fun Header(
        entry: Entry,
        color: () -> Color?,
        coverImage: @Composable () -> String?,
        bannerImage: @Composable () -> String?,
        titleText: @Composable () -> String,
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            val (banner, cover, title, summary) = createRefs()
            AsyncImage(
                model = bannerImage(),
                contentScale = ContentScale.FillHeight,
                contentDescription = stringResource(R.string.anime_media_banner_image),
                modifier = Modifier
                    .constrainAs(banner) {
                        width = Dimension.fillToConstraints
                        height = Dimension.preferredWrapContent
                            .atLeast(180.dp)
                            .atMost(180.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                    }
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithCache {
                        val brush = Brush.verticalGradient(
                            0.25f to Color.Black,
                            1f to Color.Transparent,
                        )
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush, blendMode = BlendMode.DstIn)
                        }
                    }
                    .background(color() ?: Color.Unspecified)
            )

            val halfWidthGuideline = createGuidelineFromStart(0.5f)

            ElevatedCard(
                modifier = Modifier
                    .constrainAs(cover) {
                        width = Dimension.wrapContent
                        height = Dimension.preferredWrapContent.atLeast(180.dp).atMost(240.dp)
                        top.linkTo(parent.top, 100.dp)
                        linkTo(
                            start = parent.start,
                            end = halfWidthGuideline,
                            startMargin = 16.dp,
                            bias = 0f,
                        )
                    }
            ) {
                AsyncImage(
                    model = coverImage(),
                    fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                    contentDescription = stringResource(R.string.anime_media_cover_image),
                )
            }

            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.constrainAs(title) {
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                    linkTo(
                        start = cover.end,
                        end = parent.end,
                        top = banner.bottom,
                        bottom = summary.top,
                        horizontalBias = 0f,
                        verticalBias = 0.5f
                    )
                },
            ) {
                AutoHeightText(
                    text = titleText(),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
                )
            }

            SummaryText(
                entry = entry,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp, top = 4.dp)
                    .constrainAs(summary) {
                        linkTo(
                            start = cover.end,
                            end = parent.end,
                            bias = 0f,
                        )
                        bottom.linkTo(parent.bottom)
                    }
            )
        }
    }

    @Composable
    private fun SectionHeader(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        )
    }

    @Composable
    private fun DescriptionSection(entry: Entry) {
        entry.description?.let {
            var expanded by remember { mutableStateOf(false) }
            SectionHeader(stringResource(R.string.anime_media_details_description_label))

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
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithCache {
                            val brush = Brush.verticalGradient(
                                0.8f to Color.Black,
                                1f to Color.Transparent,
                            )
                            onDrawWithContent {
                                drawContent()
                                if (!expanded) {
                                    drawRect(brush, blendMode = BlendMode.DstIn)
                                }
                            }
                        }
                )
            }
        }
    }

    @Composable
    private fun SummaryText(entry: Entry, modifier: Modifier = Modifier) {
        val seasonYear = entry.seasonTextRes?.let { stringResource(it) + " " }.orEmpty() +
                entry.seasonYear.orEmpty()
        Text(
            text = listOfNotNull(
                entry.formatTextRes?.let { stringResource(it) },
                entry.statusTextRes?.let { stringResource(it) },
                seasonYear.ifEmpty { null },
            ).joinToString(separator = " - "),
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier.wrapContentHeight()
        )
    }

    @Composable
    private fun GenreSection(
        entry: Entry,
        onGenreClicked: (String) -> Unit,
        onGenreLongClicked: (String) -> Unit
    ) {
        if (entry.genres.isNotEmpty()) {
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

    @Composable
    private fun CharactersSection(
        entry: Entry,
        onCharacterClicked: (String) -> Unit,
        onCharacterLongClicked: (String) -> Unit
    ) {
        if (entry.characters.isNotEmpty()) {
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

    @Composable
    private fun RelationsSection(
        entry: Entry, onMediaClicked: (AnimeMediaListRow.Entry) -> Unit,
        onTagClicked: (tagId: String, tagName: String) -> Unit,
        onTagLongClicked: (String) -> Unit,
    ) {
        if (entry.relations.isNotEmpty()) {
            SectionHeader(stringResource(R.string.anime_media_details_relations_label))
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                entry.relations.take(RELATIONS_ABOVE_FOLD).forEach {
                    AnimeMediaListRow(
                        entry = it.entry,
                        label = { RelationLabel(it.relation) },
                        onClick = onMediaClicked,
                        onTagClick = onTagClicked,
                        onTagLongClick = onTagLongClicked,
                    )
                }

                if (entry.relations.size > RELATIONS_ABOVE_FOLD) {
                    var expanded by remember { mutableStateOf(false) }
                    if (expanded) {
                        entry.relations.drop(RELATIONS_ABOVE_FOLD).forEach {
                            AnimeMediaListRow(
                                entry = it.entry,
                                label = { RelationLabel(it.relation) },
                                onClick = onMediaClicked,
                                onTagClick = onTagClicked,
                                onTagLongClick = onTagLongClicked,
                            )
                        }
                    }

                    ElevatedCard(
                        onClick = { expanded = !expanded },
                        modifier = Modifier
                            .fillMaxWidth()
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

    @Composable
    private fun TagSection(
        entry: Entry,
        onTagClicked: (tagId: String, tagName: String) -> Unit = { _, _ -> },
        onTagLongClicked: (tagId: String) -> Unit = {},
    ) {
        if (entry.tags.isNotEmpty()) {
            SectionHeader(stringResource(R.string.anime_media_details_tags_label))

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

    data class Entry(
        val media: Media,
    ) {
        val description get() = media.description
        val seasonTextRes = media.season?.toTextRes()
        val seasonYear = media.seasonYear?.toString()

        val formatTextRes = media.format?.toTextRes()
        val statusTextRes = media.status?.toTextRes()

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
    }
}