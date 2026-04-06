package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val id: String,
    val flags: ChannelFlags? = null,
    @SerialName("default_forum_layout")
    val defaultForumLayout: ForumLayout? = null,
)
