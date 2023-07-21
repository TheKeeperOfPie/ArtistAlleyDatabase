package com.thekeeperofpie.artistalleydatabase.anime.media

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.anilist.fragment.MediaHeaderData
import com.anilist.type.MediaSeason
import com.anilist.type.MediaType
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@Composable
fun MediaHeader(
    screenKey: String,
    upIconOption: UpIconOption,
    mediaId: String?,
    titles: List<String>?,
    averageScore: Int?,
    popularity: Int?,
    progress: Float,
    headerValues: MediaHeaderValues,
    onFavoriteChanged: (Boolean) -> Unit,
    colorCalculationState: ColorCalculationState,
    onImageWidthToHeightRatioAvailable: (Float) -> Unit = {},
    enableCoverImageSharedElement: Boolean = true,
    menuContent: (@Composable () -> Unit)? = null,
) {
    var preferredTitle by remember { mutableStateOf<Int?>(null) }
    SharedElement(
        key = "anime_media_${mediaId}_header",
        screenKey = screenKey,
    ) {
        CoverAndBannerHeader(
            screenKey = screenKey,
            upIconOption = upIconOption,
            entryId = if (enableCoverImageSharedElement) {
                EntryId("anime_media", mediaId ?: "unknown")
            } else {
                EntryId("unknown", "disabled")
            },
            progress = progress,
            color = { headerValues.color },
            coverImage = { headerValues.coverImage },
            coverImageAllowHardware = colorCalculationState.hasColor(mediaId),
            coverImageWidthToHeightRatio = headerValues.coverImageWidthToHeightRatio,
            bannerImage = { headerValues.bannerImage },
            onClickEnabled = (titles?.size ?: 0) > 1,
            onClick = {
                preferredTitle =
                    ((preferredTitle ?: 0) + 1) % (titles?.size ?: 1)
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
        ) {
            Column {
                Row(modifier = Modifier.weight(1f)) {
                    AutoResizeHeightText(
                        text = when (val index = preferredTitle) {
                            null -> null
                            else -> titles?.get(index)
                        } ?: headerValues.titleText,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                    )
                    AnimatedVisibility(
                        visible = progress > 0.8f,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally(),
                        modifier = Modifier.align(Alignment.Top)
                    ) {
                        MediaRatingIconsSection(
                            rating = averageScore,
                            popularity = popularity,
                            modifier = Modifier
                                .alpha(if (progress > 0.8f) 1f - ((1f - progress) / 0.2f) else 0f)
                                .padding(start = 8.dp, end = 8.dp, top = 4.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Column(modifier = Modifier.weight(1f)) {
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
                                val context = LocalContext.current
                                val airingAt = remember {
                                    MediaUtils.formatAiringAt(
                                        context,
                                        nextEpisodeAiringAt * 1000L
                                    )
                                }

                                val remainingTime = remember {
                                    MediaUtils.formatRemainingTime(nextEpisodeAiringAt * 1000L)
                                }

                                Text(
                                    text = stringResource(
                                        R.string.anime_media_next_airing_episode,
                                        nextEpisode,
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

                    if (menuContent != null) {
                        menuContent()
                    }
                }
            }
        }
    }
}

class MediaHeaderValues(
    arguments: Bundle,
    val coverImageWidthToHeightRatio: Float =
        arguments.getString("coverImageWidthToHeightRatio")
            ?.toFloatOrNull() ?: 1f,
    private val _title: String? = arguments.getString("title"),
    private val _coverImage: String? = arguments.getString("coverImage"),
    private val _bannerImage: String? = arguments.getString("bannerImage"),
    private val _subtitleFormatRes: Int? =
        arguments.getString("subtitleFormatRes")?.toIntOrNull(),
    private val _subtitleStatusRes: Int? =
        arguments.getString("subtitleStatusRes")?.toIntOrNull(),
    private val _subtitleSeason: MediaSeason? = arguments.getString("subtitleSeason")
        ?.let { season ->
            MediaSeason.values().find { it.rawValue == season }
        },
    private val _subtitleSeasonYear: Int? =
        arguments.getString("subtitleSeasonYear")?.toIntOrNull(),
    private val _nextEpisode: Int? = arguments.getString("nextEpisode")?.toIntOrNull(),
    private val _nextEpisodeAiringAt: Int? =
        arguments.getString("nextEpisodeAiringAt")?.toIntOrNull(),
    private val _color: Color? = arguments.getString("color")
        ?.toIntOrNull()
        ?.let(::Color),
    private val _favorite: Boolean? = arguments.getString("favorite")?.toBooleanStrictOrNull(),
    private val _type: MediaType = MediaType.safeValueOf(arguments.getString("type").orEmpty()),
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

        @Composable
        inline fun <reified ViewModelType : ViewModel> createViewModel(
            arguments: Bundle,
            crossinline media: ViewModelType.() -> MediaHeaderData?,
            crossinline favoriteUpdate: ViewModelType.() -> Boolean?,
        ): Pair<ViewModelType, MediaHeaderValues> {
            val viewModel = hiltViewModel<ViewModelType>()
            val headerValues = MediaHeaderValues(
                arguments = arguments,
                media = { viewModel.media() },
                favoriteUpdate = { viewModel.favoriteUpdate() },
            )
            return viewModel to headerValues
        }

        fun routeSuffix(
            media: MediaHeaderData?,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
        ) =
            if (media == null) {
                ""
            } else {
                "&title=${media.title?.userPreferred}" +
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

        fun navArguments() = listOf(
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
        ).map {
            navArgument(it) {
                type = NavType.StringType
                nullable = true
            }
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
    val titleText
        get() = media()?.title?.userPreferred ?: _title ?: ""
    val nextEpisode
        get() = media()?.nextAiringEpisode?.episode
            ?: _nextEpisode
    val nextEpisodeAiringAt
        get() = media()?.nextAiringEpisode?.airingAt
            ?: _nextEpisodeAiringAt
    val favorite
        get() = favoriteUpdate() ?: media()?.isFavourite ?: _favorite
    val type
        get() = media()?.type ?: _type

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
