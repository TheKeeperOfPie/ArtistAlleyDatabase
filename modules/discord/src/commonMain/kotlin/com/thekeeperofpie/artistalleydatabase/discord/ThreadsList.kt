package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThreadsList(
    val threads: List<Thread>,
    @SerialName("has_more")
    val hasMore: Boolean,
)
