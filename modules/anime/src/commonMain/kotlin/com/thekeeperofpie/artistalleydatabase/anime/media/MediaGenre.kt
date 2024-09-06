package com.thekeeperofpie.artistalleydatabase.anime.media

import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_action_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_adventure_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_comedy_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_drama_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_fantasy_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_horror_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_mahou_shoujo_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_mecha_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_music_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_mystery_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_psychological_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_romance_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_sci_fi_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_sports_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_supernatural_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_thriller_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_slice_of_life_action_description
import org.jetbrains.compose.resources.StringResource

// These come from the AniList Submission Manual
// https://submission-manual.anilist.co/Genres-75d362dc3b374838bb262afc496aa3ae
enum class MediaGenre(val id: String, val textRes: StringResource) {
    ACTION("Action", Res.string.anime_media_genre_action_description),
    ADVENTURE("Adventure", Res.string.anime_media_genre_adventure_description),
    COMEDY("Comedy", Res.string.anime_media_genre_comedy_description),
    DRAMA("Drama", Res.string.anime_media_genre_drama_description),
    FANTASY("Fantasy", Res.string.anime_media_genre_fantasy_description),
    HORROR("Horror", Res.string.anime_media_genre_horror_description),
    MECHA("Mecha", Res.string.anime_media_genre_mecha_description),
    MAHOU_SHOUJO("Majou Shoujo", Res.string.anime_media_genre_mahou_shoujo_description),
    MUSIC("Music", Res.string.anime_media_genre_music_description),
    MYSTERY("Mystery", Res.string.anime_media_genre_mystery_description),
    PSYCHOLOGICAL("Psychological", Res.string.anime_media_genre_psychological_description),
    ROMANCE("Romance", Res.string.anime_media_genre_romance_description),
    SCI_FI("Sci-Fi", Res.string.anime_media_genre_sci_fi_description),
    SLICE_OF_LIFE("Slice of Life", Res.string.anime_media_slice_of_life_action_description),
    SPORTS("Sports", Res.string.anime_media_genre_sports_description),
    SUPERNATURAL("Supernatural", Res.string.anime_media_genre_supernatural_description),
    THRILLER("Thriller", Res.string.anime_media_genre_thriller_description),
}
