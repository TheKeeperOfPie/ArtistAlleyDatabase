package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R

// These come from the AniList Submission Manual
// https://submission-manual.anilist.co/Genres-75d362dc3b374838bb262afc496aa3ae
enum class MediaGenre(val id: String, @StringRes val textRes: Int) {
    ACTION("Action", R.string.anime_media_genre_action_description),
    ADVENTURE("Adventure", R.string.anime_media_genre_adventure_description),
    COMEDY("Comedy", R.string.anime_media_genre_comedy_description),
    DRAMA("Drama", R.string.anime_media_genre_drama_description),
    FANTASY("Fantasy", R.string.anime_media_genre_fantasy_description),
    HORROR("Horror", R.string.anime_media_genre_horror_description),
    MECHA("Mecha", R.string.anime_media_genre_mecha_description),
    MAHOU_SHOUJO("Majou Shoujo", R.string.anime_media_genre_mahou_shoujo_description),
    MUSIC("Music", R.string.anime_media_genre_music_description),
    MYSTERY("Mystery", R.string.anime_media_genre_mystery_description),
    PSYCHOLOGICAL("Psychological", R.string.anime_media_genre_psychological_description),
    ROMANCE("Romance", R.string.anime_media_genre_romance_description),
    SCI_FI("Sci-Fi", R.string.anime_media_genre_sci_fi_description),
    SLICE_OF_LIFE("Slice of Life", R.string.anime_media_slice_of_life_action_description),
    SPORTS("Sports", R.string.anime_media_genre_sports_description),
    SUPERNATURAL("Supernatural", R.string.anime_media_genre_supernatural_description),
    THRILLER("Thriller", R.string.anime_media_genre_thriller_description),
}
