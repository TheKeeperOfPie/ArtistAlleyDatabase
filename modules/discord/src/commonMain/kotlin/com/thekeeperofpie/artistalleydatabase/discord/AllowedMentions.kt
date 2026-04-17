package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable

@Serializable
data class AllowedMentions(
    val parse: List<String>? = null,
)
