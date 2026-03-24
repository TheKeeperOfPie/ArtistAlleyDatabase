package com.thekeeperofpie.artistalleydatabase.alley.discord.models

import kotlinx.serialization.Serializable

@Serializable
data class InteractionResponseData(
    val content: String? = null,
)
