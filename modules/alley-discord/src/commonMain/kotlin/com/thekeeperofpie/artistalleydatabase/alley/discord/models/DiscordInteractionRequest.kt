package com.thekeeperofpie.artistalleydatabase.alley.discord.models

import kotlinx.serialization.Serializable

@Serializable
internal data class DiscordInteractionRequest(
    val type: InteractionType? = null,
    val id: String,
    val token: String,
)

