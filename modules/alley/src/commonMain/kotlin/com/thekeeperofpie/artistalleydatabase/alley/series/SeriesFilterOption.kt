package com.thekeeperofpie.artistalleydatabase.alley.series

import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_all
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_anime_manga
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_books
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_games
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_movies
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_multimedia
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_music
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_other
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_tv
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_visual_novels
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_web_series
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
enum class SeriesFilterOption(val title: StringResource) {
    ALL(Res.string.alley_series_filter_all),
    ANIME_MANGA(Res.string.alley_series_filter_anime_manga),
    GAMES(Res.string.alley_series_filter_games),
    TV(Res.string.alley_series_filter_tv),
    MOVIES(Res.string.alley_series_filter_movies),
    BOOKS(Res.string.alley_series_filter_books),
    WEB_SERIES(Res.string.alley_series_filter_web_series),
    VISUAL_NOVELS(Res.string.alley_series_filter_visual_novels),
    MUSIC(Res.string.alley_series_filter_music),
    MULTIMEDIA(Res.string.alley_series_filter_multimedia),
    OTHER(Res.string.alley_series_filter_other),
}
