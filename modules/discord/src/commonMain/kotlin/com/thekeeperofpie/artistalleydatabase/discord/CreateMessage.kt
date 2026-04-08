package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable

@Serializable
data class CreateMessage(
    val content: String,
    val embeds: List<Embed>? = null,
)
