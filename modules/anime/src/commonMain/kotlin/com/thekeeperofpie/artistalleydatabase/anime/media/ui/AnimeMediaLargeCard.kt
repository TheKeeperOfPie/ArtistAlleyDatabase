package com.thekeeperofpie.artistalleydatabase.anime.media.ui

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_banner_image_content_description
import com.anilist.data.fragment.AniListDate
import com.anilist.data.type.MediaFormat
import com.anilist.data.type.MediaSeason
import com.anilist.data.type.MediaStatus
import com.anilist.data.type.MediaType
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.data.CoverImage
import com.thekeeperofpie.artistalleydatabase.anime.data.MediaQuickEditData
import com.thekeeperofpie.artistalleydatabase.anime.data.Title
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.search.SearchDestinations
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalAppTheme
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalAnimatedVisibilityScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.rememberSharedContentState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeBottom
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.blurForScreenshotMode
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.recomposeHighlighter
import org.jetbrains.compose.resources.stringResource

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class
)
object AnimeMediaLargeCard {

    private val HEIGHT = 200.dp
    private val DESCRIPTION_PLACEHOLDER = buildAnnotatedString {
        append(
            "Some really long placeholder description for a loading media large card, "
                .repeat(3)
        )
    }

    @Composable
    operator fun invoke(
        viewer: AniListViewer?,
        entry: Entry?,
        modifier: Modifier = Modifier,
        label: (@Composable () -> Unit)? = null,
        forceListEditIcon: Boolean = false,
        showQuickEdit: Boolean = true,
        shouldTransitionCoverImageIfUsed: Boolean = true,
    ) {
        val sharedTransitionKey = entry?.mediaId?.let { SharedTransitionKey.makeKeyForId(it) }
        val sharedContentState = rememberSharedContentState(
            sharedTransitionKey.takeIf { entry?.bannerImage != null || shouldTransitionCoverImageIfUsed },
            if (entry?.bannerImage != null) "media_banner_image" else "media_image",
        )
        ElevatedCard(
            modifier = modifier
                .conditionally(entry?.bannerImage != null || shouldTransitionCoverImageIfUsed) {
                    sharedElement(sharedContentState)
                }
                .fillMaxWidth()
                .heightIn(min = HEIGHT)
                .alpha(if (entry?.ignored == true) 0.38f else 1f)
                .recomposeHighlighter()
        ) {
            val navigationController = LocalNavigationController.current
            val ignoreController = LocalIgnoreController.current
            val title = entry?.primaryTitle()
            val imageState =
                rememberCoilImageState(entry?.bannerImage ?: entry?.image, requestColors = true)
            val isBanner = entry?.bannerImage != null
            Box(
                modifier = Modifier.combinedClickable(
                    enabled = entry != null,
                    onClick = {
                        if (entry != null) {
                            navigationController.navigate(
                                AnimeDestination.MediaDetails(
                                    mediaId = entry.mediaId,
                                    title = title,
                                    coverImage = if (!isBanner) {
                                        imageState.toImageState()
                                    } else {
                                        ImageState(entry.image)
                                    },
                                    sharedTransitionKey = sharedTransitionKey,
                                    headerParams = entry.toMediaHeaderParams(
                                        bannerImageState = imageState.takeIf { isBanner }
                                            ?.toImageState(),
                                        coverImageState = imageState.takeUnless { isBanner }
                                            ?.toImageState(),
                                        title = title,
                                    ),
                                )
                            )
                        }
                    },
                    onLongClick = {
                        if (entry != null) {
                            ignoreController.toggle(entry)
                        }
                    }
                )
            ) {
                BannerImage(
                    entry = entry,
                    imageState = imageState,
                )

                val foregroundAlpha by LocalAnimatedVisibilityScope.current.transition
                    .animateFloat(transitionSpec = { tween() }, label = "Foreground text alpha") {
                        when (it) {
                            EnterExitState.PreEnter -> 0f
                            EnterExitState.Visible -> 1f
                            EnterExitState.PostExit -> 0f
                        }
                    }
                Column(
                    modifier = Modifier
                        .alpha(foregroundAlpha.takeIf { sharedContentState?.isMatchFound == true }
                            ?: 1f)
                        .fillMaxHeight()
                        .heightIn(min = HEIGHT)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            label?.invoke()
                            TitleText(entry, title)
                            SubtitleText(entry)
                        }

                        MediaRatingIconsSection(
                            rating = entry?.rating,
                            popularity = entry?.popularity,
                            loading = entry == null,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .wrapContentWidth()
                        )
                    }

                    Description(entry = entry)

                    Row(verticalAlignment = Alignment.Bottom) {
                        Column(modifier = Modifier.weight(1f)) {
                            val nextAiringEpisode = entry?.nextAiringEpisode
                            if (nextAiringEpisode != null) {
                                MediaNextAiringSection(
                                    nextAiringEpisode = nextAiringEpisode,
                                    episodes = entry.episodes,
                                    format = entry.format,
                                )
                            }
                            val (containerColor, textColor) = imageState.colors
                            MediaTagRow(
                                loading = entry == null,
                                tags = entry?.tags ?: AnimeMediaTagEntry.PLACEHOLDERS,
                                onTagClick = { id, name ->
                                    if (entry != null) {
                                        navigationController.navigate(
                                            SearchDestinations.SearchMedia(
                                                title = SearchDestinations.SearchMedia.Title.Custom(
                                                    name
                                                ),
                                                tagId = id,
                                                mediaType = entry.type ?: MediaType.ANIME,
                                            )
                                        )
                                    }
                                },
                                tagContainerColor = containerColor,
                                tagTextColor = textColor,
                            )
                        }

                        if (viewer != null && entry != null && showQuickEdit) {
                            MediaQuickEdit(
                                viewer = viewer,
                                entry = entry,
                                forceListEditIcon = forceListEditIcon,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BannerImage(
        entry: Entry?,
        imageState: CoilImageState,
    ) {
        val foregroundColor = MaterialTheme.colorScheme.surface
        val appTheme = LocalAppTheme.current
        val isLightTheme = appTheme == AppThemeSetting.LIGHT
                || (appTheme == AppThemeSetting.AUTO && !isSystemInDarkTheme())
        val alpha by animateFloatAsState(
            if (imageState.success) 1f else 0f,
            label = "AnimeMediaLargeCard banner image alpha",
        )
        CoilImage(
            state = imageState,
            model = imageState.request().build(),
            contentScale = ContentScale.Crop,
            contentDescription = stringResource(
                Res.string.anime_media_banner_image_content_description
            ),
            modifier = Modifier
                .background(entry?.color ?: MaterialTheme.colorScheme.surfaceVariant)
                .fillMaxWidth()
                .height(HEIGHT)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        foregroundColor,
                        alpha = if (isLightTheme) 0.8f else 0.6f,
                    )
                }
                // Clip to match card so that shared element animation keeps rounded corner
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .alpha(alpha)
                .blurForScreenshotMode()
        )
    }

    @Composable
    private fun TitleText(entry: Entry?, title: String?) {
        Text(
            text = if (entry == null) {
                "Placeholder media title..."
            } else {
                title.orEmpty()
            },
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Top)
                .padding(start = 12.dp, top = 10.dp, end = 16.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun SubtitleText(entry: Entry?) {
        Text(
            text = if (entry == null) {
                "Placeholder subtitle text..."
            } else {
                MediaDataUtils.formatSubtitle(
                    format = entry.format,
                    status = entry.status,
                    season = entry.season,
                    seasonYear = entry.seasonYear,
                    startDate = entry.startDate,
                ).orEmpty()
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.W500,
            color = MaterialTheme.typography.bodySmall.color
                .takeOrElse { LocalContentColor.current }
                .copy(alpha = 0.8f),
            modifier = Modifier
                .wrapContentHeight()
                .padding(start = 12.dp, top = 4.dp, end = 16.dp, bottom = 4.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun ColumnScope.Description(entry: Entry?) {
        val description = if (entry == null) {
            DESCRIPTION_PLACEHOLDER
        } else {
            entry.description
        }

        if (description == null) {
            Spacer(Modifier.weight(1f))
        } else {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                    .fadingEdgeBottom(firstStop = 0.7f)
                    .placeholder(
                        visible = entry == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }

    @Composable
    private fun MediaQuickEdit(
        viewer: AniListViewer?,
        entry: Entry,
        forceListEditIcon: Boolean,
    ) {
        Box(
            modifier = Modifier
                .padding(end = 4.dp, bottom = 4.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            val animeComponent = LocalAnimeComponent.current
            val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
            MediaListQuickEditIconButton(
                viewer = viewer,
                media = entry,
                forceListEditIcon = forceListEditIcon,
                onClick = { editViewModel.initialize(entry) },
            )
        }
    }

    interface Entry : IgnoreController.Data, MediaQuickEditData, MediaListQuickEditButtonData {
        val image: String?
        val color: Color?
        val rating: Int?
        val tags: List<AnimeMediaTagEntry>
        val description: AnnotatedString?
        val title: Title?
        val coverImage: CoverImage?
        val bannerImage: String?
        val format: MediaFormat?
        val status: MediaStatus?
        val season: MediaSeason?
        val seasonYear: Int?
        val startDate: AniListDate?
        val popularity: Int?
        val isFavourite: Boolean
        val ignored: Boolean

        @Composable
        fun primaryTitle(): String?

        fun toMediaHeaderParams(
            bannerImageState: ImageState?,
            coverImageState: ImageState?,
            title: String?,
        ) = MediaHeaderParams(
            title = title,
            bannerImage = bannerImageState,
            coverImage = coverImageState,
            subtitleFormat = format,
            subtitleStatus = status,
            subtitleSeason = season,
            subtitleSeasonYear = seasonYear,
            subtitleStartDate = startDate,
            nextAiringEpisode = nextAiringEpisode,
            colorArgb = coverImage?.color?.toArgb(),
            type = type,
            favorite = isFavourite,
        )
    }
}
