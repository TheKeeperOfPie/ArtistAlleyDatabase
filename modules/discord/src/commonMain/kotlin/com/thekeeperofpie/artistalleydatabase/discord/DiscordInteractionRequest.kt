package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordInteractionRequest(
    val type: InteractionType? = null,
    val id: String,
    val token: String,
    val data: InteractionRequestData? = null,
    val member: Member? = null,
    @SerialName("guild_id")
    val guildId: String? = null,
)

