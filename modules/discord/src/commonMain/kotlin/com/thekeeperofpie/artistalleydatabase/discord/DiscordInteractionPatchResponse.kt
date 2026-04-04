package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable

@Serializable
data class DiscordInteractionPatchResponse(
    val content: String,
    val flags: MessageFlags? = null,
    val components: List<MessageComponent>? = null,
)

