package com.thekeeperofpie.artistalleydatabase.anime.media.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.anilist.data.fragment.AniListDate
import com.anilist.data.fragment.MediaCompactWithTags
import com.anilist.data.fragment.MediaHeaderData
import com.anilist.data.fragment.MediaWithListStatus
import com.anilist.data.type.MediaFormat
import com.anilist.data.type.MediaSeason
import com.anilist.data.type.MediaStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListDateSerializer
import com.thekeeperofpie.artistalleydatabase.anime.data.NextAiringEpisode
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsHeaderValues
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.maybeOverride
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MediaHeaderParams(
    val title: String? = null,
    val bannerImage: ImageState? = null,
    val coverImage: ImageState? = null,
    val subtitleFormat: MediaFormat? = null,
    val subtitleStatus: MediaStatus? = null,
    val subtitleSeason: MediaSeason? = null,
    val subtitleSeasonYear: Int? = null,
    @Serializable(with = AniListDateSerializer::class)
    val subtitleStartDate: AniListDate? = null,
    val nextAiringEpisode: NextAiringEpisode? = null,
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
        subtitleFormat = media?.format,
        subtitleStatus = media?.status,
        subtitleSeason = media?.season,
        subtitleSeasonYear = media?.seasonYear,
        subtitleStartDate = media?.startDate,
        nextAiringEpisode = media?.nextAiringEpisode?.let {
            NextAiringEpisode(
                episode = it.episode,
                airingAt = Instant.fromEpochSeconds(it.airingAt.toLong()),
            )
        },
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
        subtitleFormat = mediaCompactWithTags?.format,
        subtitleStatus = null,
        subtitleSeason = mediaCompactWithTags?.season,
        subtitleSeasonYear = mediaCompactWithTags?.seasonYear,
        subtitleStartDate = mediaCompactWithTags?.startDate,
        nextAiringEpisode = mediaCompactWithTags?.nextAiringEpisode?.let {
            NextAiringEpisode(
                episode = it.episode,
                airingAt = Instant.fromEpochSeconds(it.airingAt.toLong()),
            )
        },
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
        subtitleFormat = null,
        subtitleStatus = null,
        subtitleSeason = null,
        subtitleSeasonYear = null,
        subtitleStartDate = null,
        nextAiringEpisode = mediaWithListStatus?.nextAiringEpisode?.let {
            NextAiringEpisode(
                episode = it.episode,
                airingAt = Instant.fromEpochSeconds(it.airingAt.toLong()),
            )
        },
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
    val nextAiringEpisode
        get() = media()?.nextAiringEpisode?.let {
            NextAiringEpisode(
                episode = it.episode,
                airingAt = Instant.fromEpochSeconds(it.airingAt.toLong())
            )
        } ?: params?.nextAiringEpisode
    val favorite
        get() = favoriteUpdate() ?: media()?.isFavourite ?: params?.favorite
    val type
        get() = media()?.type ?: params?.type ?: MediaType.UNKNOWN__

    @Composable
    fun title() = media()?.title?.primaryTitle() ?: params?.title

    @Composable
    fun subtitleText() = media()?.let {
        MediaDataUtils.formatSubtitle(
            format = it.format,
            status = it.status,
            season = it.season,
            seasonYear = it.seasonYear,
            startDate = it.startDate,
        )
    } ?: MediaDataUtils.formatSubtitle(
        format = params?.subtitleFormat,
        status = params?.subtitleStatus,
        season = params?.subtitleSeason,
        seasonYear = params?.subtitleSeasonYear,
        startDate = params?.subtitleStartDate,
    )
}
