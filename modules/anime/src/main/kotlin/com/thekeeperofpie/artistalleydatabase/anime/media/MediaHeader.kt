package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import com.anilist.fragment.MediaHeaderData
import com.anilist.type.MediaFormat
import com.anilist.type.MediaSeason
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaRatingIconsSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.navArguments
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.sharedBounds
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@Composable
fun MediaHeader(
    screenKey: String,
    upIconOption: UpIconOption,
    viewer: AniListViewer?,
    mediaId: String?,
    mediaType: MediaType?,
    titles: List<String>?,
    episodes: Int?,
    format: MediaFormat?,
    averageScore: Int?,
    popularity: Int?,
    progress: Float,
    headerValues: MediaHeaderValues,
    onFavoriteChanged: (Boolean) -> Unit,
    sharedElementKey: String? = null,
    onImageWidthToHeightRatioAvailable: (Float) -> Unit = {},
    enableCoverImageSharedElement: Boolean = true,
    onCoverImageSharedElementFractionChanged: ((Float) -> Unit)? = null,
    onCoverImageClick: (() -> Unit)? = null,
    menuContent: (@Composable () -> Unit)? = null,
) {
    val defaultTitle = headerValues.title()
    var preferredTitle by remember(defaultTitle, titles) {
        mutableIntStateOf(
            titles?.indexOf(defaultTitle)?.coerceAtLeast(0) ?: 0
        )
    }
    val colorCalculationState = LocalColorCalculationState.current
    CoverAndBannerHeader(
        screenKey = screenKey,
        upIconOption = upIconOption,
        entryId = if (enableCoverImageSharedElement) {
            EntryId("anime_media", mediaId ?: "unknown")
        } else {
            EntryId("unknown", "disabled")
        },
        coverImage = { headerValues.coverImage },
        coverImageAllowHardware = colorCalculationState.allowHardware(mediaId),
        modifier = Modifier.sharedBounds(sharedElementKey, "media_header"),
        sharedElementKey = sharedElementKey,
        bannerImage = headerValues.bannerImage,
        progress = progress,
        coverImageWidthToHeightRatio = headerValues.coverImageWidthToHeightRatio,
        color = { headerValues.color },
        onClickEnabled = (titles?.size ?: 0) > 1,
        onClick = {
            preferredTitle = (preferredTitle + 1) % (titles?.size ?: 1)
        },
        coverImageOnSuccess = {
            onImageWidthToHeightRatioAvailable(it.widthToHeightRatio())
            if (mediaId != null) {
                ComposeColorUtils.calculatePalette(mediaId, it, colorCalculationState)
            }
        },
        menuContent = {
            FavoriteIconButton(
                favorite = headerValues.favorite,
                onFavoriteChanged = onFavoriteChanged,
            )
        },
        onCoverImageClick = onCoverImageClick,
    ) {
        Column {
            Row(modifier = Modifier.weight(1f)) {
                AutoResizeHeightText(
                    text = titles?.getOrNull(preferredTitle) ?: headerValues.title(),
                    style = MaterialTheme.typography.headlineLarge
                        .copy(lineBreak = LineBreak.Simple),
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                )
                AnimatedVisibility(
                    visible = progress > 0.6f,
                    enter = fadeIn() + expandHorizontally(
                        animationSpec = spring(
                            stiffness = Spring.StiffnessHigh,
                            visibilityThreshold = IntSize.VisibilityThreshold
                        ),
                        expandFrom = Alignment.Start,
                    ),
                    exit = fadeOut() + shrinkHorizontally(),
                    modifier = Modifier.align(Alignment.Top)
                ) {
                    MediaRatingIconsSection(
                        rating = averageScore,
                        popularity = popularity,
                        modifier = Modifier
                            .alpha(if (progress > 0.6f) 1f - ((1f - progress) / 0.2f) else 0f)
                            .padding(start = 8.dp, end = 8.dp, top = 12.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 10.dp)
                ) {
                    val subtitleText = headerValues.subtitleText()
                    AnimatedVisibility(
                        subtitleText != null,
                        label = "Media details subtitle text"
                    ) {
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

                    val nextEpisodeAiringAt = headerValues.nextEpisodeAiringAt
                    val nextEpisode = headerValues.nextEpisode
                    AnimatedVisibility(
                        nextEpisodeAiringAt != null && nextEpisode != null,
                        label = "Media details nextEpisodeAiringAt text"
                    ) {
                        if (nextEpisodeAiringAt != null && nextEpisode != null) {
                            val nextAiringAtText = MediaUtils.nextAiringSectionText(
                                airingAtAniListTimestamp = nextEpisodeAiringAt,
                                episode = nextEpisode,
                                episodes = episodes,
                                format = format,
                            )

                            Text(
                                text = nextAiringAtText,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.surfaceTint,
                                modifier = Modifier
                                    .wrapContentHeight(Alignment.Bottom)
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (menuContent != null) {
                    menuContent()
                } else {
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
                                            // TODO: Better infer media type
                                            mediaType ?: MediaType.ANIME,
                                            mediaId.toString()
                                        ) + "?${UriUtils.FORCE_EXTERNAL_URI_PARAM}=true"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

class MediaHeaderValues(
    destination: AnimeDestinations.MediaDetails,
    val coverImageWidthToHeightRatio: Float = destination.imageWidthToHeightRatio ?: 1f,
    private val _title: String? = destination.title,
    private val _coverImage: String? = destination.coverImage,
    private val _bannerImage: String? = destination.bannerImage,
    private val _subtitleFormatRes: Int? = destination.subtitleFormatRes,
    private val _subtitleStatusRes: Int? = destination.subtitleStatusRes,
    private val _subtitleSeason: MediaSeason? = destination.subtitleSeason,
    private val _subtitleSeasonYear: Int? = destination.subtitleSeasonYear,
    private val _nextEpisode: Int? = destination.nextEpisode,
    private val _nextEpisodeAiringAt: Int? = destination.nextEpisodeAiringAt,
    private val _color: Color? = destination.colorArgb?.let(::Color),
    private val _favorite: Boolean? = destination.favorite,
    private val _type: MediaType? = destination.type,
    private val media: () -> MediaHeaderData?,
    private val favoriteUpdate: () -> Boolean?,
) {
    companion object {
        const val routeSuffix = "&title={title}" +
                "&subtitleFormatRes={subtitleFormatRes}" +
                "&subtitleStatusRes={subtitleStatusRes}" +
                "&subtitleSeason={subtitleSeason}" +
                "&subtitleSeasonYear={subtitleSeasonYear}" +
                "&nextEpisode={nextEpisode}" +
                "&nextEpisodeAiringAt={nextEpisodeAiringAt}" +
                "&coverImage={coverImage}" +
                "&coverImageWidthToHeightRatio={coverImageWidthToHeightRatio}" +
                "&color={color}" +
                "&bannerImage={bannerImage}" +
                "&favorite={favorite}" +
                "&type={type}"

        fun routeSuffix(
            media: MediaHeaderData?,
            languageOption: AniListLanguageOption,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
        ) =
            if (media == null) {
                ""
            } else {
                "&title=${media.title?.primaryTitle(languageOption)}" +
                        "&subtitleFormatRes=${media.format.toTextRes()}" +
                        "&subtitleStatusRes=${media.status.toTextRes()}" +
                        "&subtitleSeason=${media.season}" +
                        "&subtitleSeasonYear=${media.seasonYear}" +
                        "&nextEpisode=${media.nextAiringEpisode?.episode}" +
                        "&nextEpisodeAiringAt=${media.nextAiringEpisode?.airingAt}" +
                        "&bannerImage=${media.bannerImage}" +
                        "&coverImage=${media.coverImage?.extraLarge}" +
                        "&coverImageWidthToHeightRatio=$imageWidthToHeightRatio" +
                        "&color=${
                            media.coverImage?.color?.let(ComposeColorUtils::hexToColor)?.toArgb()
                        }" +
                        "&favorite=$favorite" +
                        "&type=${media.type?.name}"
            }

        fun navArguments() = navArguments(
            "title",
            "subtitleFormatRes",
            "subtitleStatusRes",
            "subtitleSeason",
            "subtitleSeasonYear",
            "nextEpisode",
            "nextEpisodeAiringAt",
            "coverImage",
            "coverImageWidthToHeightRatio",
            "bannerImage",
            "color",
            "favorite",
            "type",
        ) {
            type = NavType.StringType
            nullable = true
        }
    }

    val color
        get() = media()?.coverImage?.color
            ?.let(ComposeColorUtils::hexToColor)
            ?: _color
    val coverImage
        get() = media()?.coverImage?.extraLarge ?: _coverImage
    val bannerImage
        get() = media()?.bannerImage ?: _bannerImage
    val nextEpisode
        get() = media()?.nextAiringEpisode?.episode
            ?: _nextEpisode
    val nextEpisodeAiringAt
        get() = media()?.nextAiringEpisode?.airingAt
            ?: _nextEpisodeAiringAt
    val favorite
        get() = favoriteUpdate() ?: media()?.isFavourite ?: _favorite
    val type
        get() = media()?.type ?: _type ?: MediaType.UNKNOWN__

    @Composable
    fun title() = media()?.title?.primaryTitle() ?: _title ?: ""

    @Composable
    fun subtitleText() = media()?.let {
        MediaUtils.formatSubtitle(
            format = it.format,
            status = it.status,
            season = it.season,
            seasonYear = it.seasonYear,
        )
    } ?: listOfNotNull(
        _subtitleFormatRes?.let { stringResource(it) },
        _subtitleStatusRes?.let { stringResource(it) },
        MediaUtils.formatSeasonYear(
            _subtitleSeason,
            _subtitleSeasonYear,
            withSeparator = true,
        ),
    )
        .joinToString(separator = " - ")
        .ifEmpty { null }
}
