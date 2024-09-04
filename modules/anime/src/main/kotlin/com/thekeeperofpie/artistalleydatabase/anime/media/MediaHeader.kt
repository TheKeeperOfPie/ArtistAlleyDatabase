@file:OptIn(ExperimentalCoilApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import com.anilist.fragment.MediaCompactWithTags
import com.anilist.fragment.MediaHeaderData
import com.anilist.fragment.MediaWithListStatus
import com.anilist.type.MediaFormat
import com.anilist.type.MediaSeason
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaRatingIconsSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedBounds
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.maybeOverride
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import kotlinx.serialization.Serializable

@Composable
fun MediaHeader(
    upIconOption: UpIconOption,
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
    sharedTransitionKey: SharedTransitionKey? = null,
    coverImageState: CoilImageState? = rememberCoilImageState(headerValues.coverImage),
    enableCoverImageSharedElement: Boolean = true,
    onCoverImageClick: (() -> Unit)? = null,
    menuContent: @Composable() (() -> Unit)? = null,
) {
    val defaultTitle = headerValues.title()
    var preferredTitle by remember(defaultTitle, titles) {
        mutableIntStateOf(
            titles?.indexOf(defaultTitle)?.coerceAtLeast(0) ?: 0
        )
    }
    CoverAndBannerHeader(
        upIconOption = upIconOption,
        headerValues = headerValues,
        coverImageState = coverImageState,
        sharedTransitionKey = sharedTransitionKey.takeIf { enableCoverImageSharedElement },
        coverImageSharedTransitionIdentifier = "media_image",
        bannerImageSharedTransitionIdentifier = "media_banner_image",
        progress = progress,
        onClickEnabled = (titles?.size ?: 0) > 1,
        onClick = {
            preferredTitle = (preferredTitle + 1) % (titles?.size ?: 1)
        },
        menuContent = {
            FavoriteIconButton(
                favorite = headerValues.favorite,
                onFavoriteChanged = onFavoriteChanged,
            )
        },
        onCoverImageClick = onCoverImageClick,
        modifier = Modifier.sharedBounds(sharedTransitionKey, "media_header")
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
                MediaRatingIconsSection(
                    rating = averageScore,
                    popularity = popularity,
                    modifier = Modifier
                        .alpha(if (progress > 0.6f) 1f - ((1f - progress) / 0.2f) else 0f)
                        .padding(start = 8.dp, end = 8.dp, top = 12.dp)
                        .align(Alignment.Top)
                )
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

@Serializable
data class MediaHeaderParams(
    val title: String? = null,
    val bannerImage: ImageState? = null,
    val coverImage: ImageState? = null,
    val subtitleFormatRes: Int? = null,
    val subtitleStatusRes: Int? = null,
    val subtitleSeason: MediaSeason? = null,
    val subtitleSeasonYear: Int? = null,
    val nextEpisode: Int? = null,
    val nextEpisodeAiringAt: Int? = null,
    val colorArgb: Int? = null,
    val type: MediaType? = null,
    val favorite: Boolean? = null,
) {
    constructor(
        title: String?,
        media: MediaHeaderData?,
        favorite: Boolean? = null,
        bannerImage: ImageState? = media?.bannerImage?.let(::ImageState),
        coverImage: ImageState?,
    ) : this(
        title = title,
        bannerImage = bannerImage,
        coverImage = coverImage,
        subtitleFormatRes = media?.format.toTextRes(),
        subtitleStatusRes = media?.status.toTextRes(),
        subtitleSeason = media?.season,
        subtitleSeasonYear = media?.seasonYear,
        nextEpisode = media?.nextAiringEpisode?.episode,
        nextEpisodeAiringAt = media?.nextAiringEpisode?.airingAt,
        colorArgb = media?.coverImage?.color?.let(ComposeColorUtils::hexToColor)?.toArgb(),
        type = media?.type,
        favorite = favorite,
    )

    constructor(
        title: String?,
        mediaCompactWithTags: MediaCompactWithTags?,
        favorite: Boolean? = null,
        bannerImage: ImageState? = null,
        coverImage: ImageState?,
    ) : this(
        title = title,
        bannerImage = bannerImage,
        coverImage = coverImage,
        subtitleFormatRes = mediaCompactWithTags?.format.toTextRes(),
        subtitleStatusRes = null,
        subtitleSeason = mediaCompactWithTags?.season,
        subtitleSeasonYear = mediaCompactWithTags?.seasonYear,
        nextEpisode = mediaCompactWithTags?.nextAiringEpisode?.episode,
        nextEpisodeAiringAt = null,
        colorArgb = mediaCompactWithTags?.coverImage?.color?.let(ComposeColorUtils::hexToColor)
            ?.toArgb(),
        favorite = favorite,
        type = mediaCompactWithTags?.type,
    )

    constructor(
        title: String?,
        mediaWithListStatus: MediaWithListStatus?,
        favorite: Boolean? = null,
        bannerImage: ImageState? = null,
        coverImage: ImageState?,
    ) : this(
        title = title,
        coverImage = coverImage,
        bannerImage = bannerImage,
        subtitleFormatRes = null,
        subtitleStatusRes = null,
        subtitleSeason = null,
        subtitleSeasonYear = null,
        nextEpisode = mediaWithListStatus?.nextAiringEpisode?.episode,
        nextEpisodeAiringAt = null,
        colorArgb = null,
        type = mediaWithListStatus?.type,
        favorite = favorite,
    )
}

class MediaHeaderValues(
    private val params: MediaHeaderParams?,
    private val media: () -> MediaHeaderData?,
    private val favoriteUpdate: () -> Boolean?,
) : DetailsHeaderValues {
    override val coverImage
        get() = params?.coverImage.maybeOverride(media()?.coverImage?.extraLarge)
    override val bannerImage
        get() = params?.bannerImage.maybeOverride(media()?.bannerImage)
    override val defaultColor = media()?.coverImage?.color?.let(ComposeColorUtils::hexToColor)
        ?: params?.colorArgb?.let { Color(it) }
        ?: Color.Unspecified
    val nextEpisode
        get() = media()?.nextAiringEpisode?.episode
            ?: params?.nextEpisode
    val nextEpisodeAiringAt
        get() = media()?.nextAiringEpisode?.airingAt
            ?: params?.nextEpisodeAiringAt
    val favorite
        get() = favoriteUpdate() ?: media()?.isFavourite ?: params?.favorite
    val type
        get() = media()?.type ?: params?.type ?: MediaType.UNKNOWN__

    @Composable
    fun title() = media()?.title?.primaryTitle() ?: params?.title ?: ""

    @Composable
    fun subtitleText() = media()?.let {
        MediaUtils.formatSubtitle(
            format = it.format,
            status = it.status,
            season = it.season,
            seasonYear = it.seasonYear,
        )
    } ?: listOfNotNull(
        params?.subtitleFormatRes?.let { stringResource(it) },
        params?.subtitleStatusRes?.let { stringResource(it) },
        MediaUtils.formatSeasonYear(
            params?.subtitleSeason,
            params?.subtitleSeasonYear,
            withSeparator = true,
        ),
    )
        .joinToString(separator = " - ")
        .ifEmpty { null }
}
