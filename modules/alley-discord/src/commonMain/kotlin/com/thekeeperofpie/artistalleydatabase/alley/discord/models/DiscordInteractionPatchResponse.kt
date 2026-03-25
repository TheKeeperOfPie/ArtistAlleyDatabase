package com.thekeeperofpie.artistalleydatabase.alley.discord.models

import com.thekeeperofpie.artistalleydatabase.alley.discord.MessageFlags
import kotlinx.serialization.Serializable

@Serializable
internal data class DiscordInteractionPatchResponse(
    val content: String,
    val flags: MessageFlags? = null,
    val components: List<MessageComponent>? = null,
)

