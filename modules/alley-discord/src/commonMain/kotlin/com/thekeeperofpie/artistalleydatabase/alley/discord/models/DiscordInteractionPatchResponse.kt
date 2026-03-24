package com.thekeeperofpie.artistalleydatabase.alley.discord.models

import kotlinx.serialization.Serializable

@Serializable
internal data class DiscordInteractionPatchResponse(
    val content: String,
)

