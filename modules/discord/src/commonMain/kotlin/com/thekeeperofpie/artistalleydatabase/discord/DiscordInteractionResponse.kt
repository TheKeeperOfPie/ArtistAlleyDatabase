package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable

@Serializable
data class DiscordInteractionResponse(
    val type: InteractionCallbackType,
    val data: InteractionResponseData? = null,
)

