package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable

@Serializable
data class CreateThread(
    val name: String,
    val message: CreateMessage,
    val attachments: List<CreateMessage.Attachment>? = null,
)
