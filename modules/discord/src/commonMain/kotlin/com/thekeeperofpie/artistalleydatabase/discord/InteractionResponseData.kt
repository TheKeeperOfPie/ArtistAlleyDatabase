package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable

@Serializable
data class InteractionResponseData(
    val flags: MessageFlags? = null,
)
