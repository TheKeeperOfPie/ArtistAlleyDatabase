package com.thekeeperofpie.artistalleydatabase.alley.series

import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_anime
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_book
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_comic
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_game
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_light_novel
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_manga
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_movie
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_multimedia_project
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_music
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_none
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_novel
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_other
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_tv
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_video_game
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_visual_novel
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_vtuber
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_web_novel
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_web_series
import artistalleydatabase.modules.alley.generated.resources.alley_series_source_webtoon
import com.thekeeperofpie.artistalleydatabase.alley.GetSeries
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesById
import com.thekeeperofpie.artistalleydatabase.alley.models.AniListType
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesRowId
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import kotlin.uuid.Uuid

val SeriesSource.textRes
    get() = when (this) {
        SeriesSource.ANIME -> Res.string.alley_series_source_anime
        SeriesSource.BOOK -> Res.string.alley_series_source_book
        SeriesSource.COMIC -> Res.string.alley_series_source_comic
        SeriesSource.GAME -> Res.string.alley_series_source_game
        SeriesSource.LIGHT_NOVEL -> Res.string.alley_series_source_light_novel
        SeriesSource.MANGA -> Res.string.alley_series_source_manga
        SeriesSource.MOVIE -> Res.string.alley_series_source_movie
        SeriesSource.MULTIMEDIA_PROJECT -> Res.string.alley_series_source_multimedia_project
        SeriesSource.MUSIC -> Res.string.alley_series_source_music
        SeriesSource.NOVEL -> Res.string.alley_series_source_novel
        SeriesSource.OTHER -> Res.string.alley_series_source_other
        SeriesSource.TV -> Res.string.alley_series_source_tv
        SeriesSource.VIDEO_GAME -> Res.string.alley_series_source_video_game
        SeriesSource.VISUAL_NOVEL -> Res.string.alley_series_source_visual_novel
        SeriesSource.VTUBER -> Res.string.alley_series_source_vtuber
        SeriesSource.WEB_NOVEL -> Res.string.alley_series_source_web_novel
        SeriesSource.WEB_SERIES -> Res.string.alley_series_source_web_series
        SeriesSource.WEBTOON -> Res.string.alley_series_source_webtoon
        SeriesSource.NONE -> Res.string.alley_series_source_none
    }

internal fun GetSeries.toSeriesInfo() = SeriesInfo(
    rowid = SeriesRowId(rowid),
    id = id,
    uuid = Uuid.parse(uuid),
    notes = notes,
    aniListId = aniListId,
    aniListType = AniListType.parse(aniListType),
    wikipediaId = wikipediaId,
    tmdbId = tmdbId,
    tmdbType = tmdbType,
    steamId = steamId,
    steamImagePath = steamImagePath,
    openLibraryId = openLibraryId,
    source = source ?: SeriesSource.NONE,
    titlePreferred = titlePreferred,
    titleEnglish = titleEnglish,
    titleRomaji = titleRomaji,
    titleNative = titleNative,
    synonyms = synonyms.orEmpty(),
    link = link,
)

internal fun GetSeriesById.toSeriesInfo() = SeriesInfo(
    rowid = SeriesRowId(rowid),
    id = id,
    uuid = Uuid.parse(uuid),
    notes = notes,
    aniListId = aniListId,
    aniListType = AniListType.parse(aniListType),
    wikipediaId = wikipediaId,
    tmdbId = tmdbId,
    tmdbType = tmdbType,
    steamId = steamId,
    steamImagePath = steamImagePath,
    openLibraryId = openLibraryId,
    source = source ?: SeriesSource.NONE,
    titlePreferred = titlePreferred,
    titleEnglish = titleEnglish,
    titleRomaji = titleRomaji,
    titleNative = titleNative,
    synonyms = synonyms.orEmpty(),
    link = link,
)

