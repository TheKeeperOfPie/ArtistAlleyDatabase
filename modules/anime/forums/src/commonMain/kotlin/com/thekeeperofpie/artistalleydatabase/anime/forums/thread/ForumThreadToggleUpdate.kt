package com.thekeeperofpie.artistalleydatabase.anime.forums.thread

sealed class ForumThreadToggleUpdate {
    abstract val id: String
    abstract val subscribed: Boolean
    abstract val liked: Boolean

    data class Subscribe(
        override val id: String,
        override val subscribed: Boolean,
        override val liked: Boolean,
    ) : ForumThreadToggleUpdate()

    data class Liked(
        override val id: String,
        override val liked: Boolean,
        override val subscribed: Boolean,
    ) : ForumThreadToggleUpdate()
}
