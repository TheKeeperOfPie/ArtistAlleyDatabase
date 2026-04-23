package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Thread(
    val id: String,
    val name: String?,
    @SerialName("thread_metadata")
    val metadata: Metadata?,
    val flags: ChannelFlags? = null,
    @SerialName("applied_tags")
    val appliedTags: List<Long>? = null,
) {
    @Serializable
    data class Metadata(
        val archived: Boolean,
        val locked: Boolean,
    )
}
