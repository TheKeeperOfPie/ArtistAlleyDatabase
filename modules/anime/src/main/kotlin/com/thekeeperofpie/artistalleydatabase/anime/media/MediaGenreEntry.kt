package com.thekeeperofpie.artistalleydatabase.anime.media

data class MediaGenreEntry(
    val name: String,
    val state: State = State.DEFAULT,
) {
    enum class State {
        DEFAULT, INCLUDE, EXCLUDE
    }
}