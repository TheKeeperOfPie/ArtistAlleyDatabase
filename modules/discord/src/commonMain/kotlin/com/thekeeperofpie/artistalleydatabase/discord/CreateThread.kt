package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateThread(
    val name: String,
    val message: CreateMessage,
    @SerialName("applied_tags")
    val appliedTags: List<Long>? = null,
    val attachments: List<CreateMessage.Attachment>? = null,
)
