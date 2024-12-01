package com.thekeeperofpie.artistalleydatabase.anime.media.data

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.runtime.Composable
import artistalleydatabase.modules.anime.media.data.generated.resources.Res
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_default
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_fall
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_spring
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_summer
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_winter
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_type_anime_icon_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_type_manga_icon_content_description
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaTitleFragment
import com.anilist.data.type.MediaSeason
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoriteType
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import org.jetbrains.compose.resources.stringResource

@Composable
fun MediaTitleFragment.primaryTitle() = primaryTitle(LocalLanguageOptionMedia.current)

fun MediaTitleFragment.primaryTitle(languageOption: AniListLanguageOption) =
    when (languageOption) {
        AniListLanguageOption.DEFAULT -> userPreferred
        AniListLanguageOption.ENGLISH -> english
        AniListLanguageOption.NATIVE -> native
        AniListLanguageOption.ROMAJI -> romaji
    } ?: userPreferred ?: romaji ?: english ?: native

fun MediaType?.toIcon() = if (this == MediaType.ANIME) {
    Icons.Filled.Monitor
} else {
    Icons.Filled.MenuBook
}

@Composable
fun MediaType?.toIconContentDescription() = stringResource(
    if (this == MediaType.ANIME) {
        Res.string.anime_media_type_anime_icon_content_description
    } else {
        Res.string.anime_media_type_manga_icon_content_description
    }
)

fun MediaType?.toFavoriteType() = if (this == MediaType.ANIME) {
    FavoriteType.ANIME
} else {
    FavoriteType.MANGA
}

fun MediaSeason.toTextRes() = when (this) {
    MediaSeason.UNKNOWN__ -> Res.string.anime_media_filter_airing_date_season_default
    MediaSeason.WINTER -> Res.string.anime_media_filter_airing_date_season_winter
    MediaSeason.SPRING -> Res.string.anime_media_filter_airing_date_season_spring
    MediaSeason.SUMMER -> Res.string.anime_media_filter_airing_date_season_summer
    MediaSeason.FALL -> Res.string.anime_media_filter_airing_date_season_fall
}

/** Decouples MediaDetails from this module */
typealias MediaDetailsRoute = (
    mediaNavigationData: MediaNavigationData,
    coverImage: ImageState?,
    languageOptionMedia: AniListLanguageOption,
    sharedTransitionKey: SharedTransitionKey?,
) -> Unit

/** Decouples MediaDetails from this module */
typealias MediaEditBottomSheetScaffoldComposable = @Composable (
    @Composable (
        PaddingValues,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) -> Unit,
) -> Unit