package com.thekeeperofpie.artistalleydatabase.anime.media

data class MediaTagEntry(
    val id: String,
    val category: String?,
    val name: String?,
    val description: String?,
    val adult: Boolean,
    val generalSpoiler : Boolean,
    val state: State = State.DEFAULT,
) {
    enum class State {
        DEFAULT, INCLUDE, EXCLUDE
    }
}