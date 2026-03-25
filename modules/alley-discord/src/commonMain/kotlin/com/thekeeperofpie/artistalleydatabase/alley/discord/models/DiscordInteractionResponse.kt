package com.thekeeperofpie.artistalleydatabase.alley.discord.models

import kotlinx.serialization.Serializable

@Serializable
internal data class DiscordInteractionResponse(
    val type: InteractionCallbackType,
    val data: InteractionResponseData? = null,
)

