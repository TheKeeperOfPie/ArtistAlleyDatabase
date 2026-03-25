package com.thekeeperofpie.artistalleydatabase.alley.discord.models

import com.thekeeperofpie.artistalleydatabase.alley.discord.MessageFlags
import kotlinx.serialization.Serializable

@Serializable
internal data class InteractionResponseData(
    val flags: MessageFlags? = null,
)
