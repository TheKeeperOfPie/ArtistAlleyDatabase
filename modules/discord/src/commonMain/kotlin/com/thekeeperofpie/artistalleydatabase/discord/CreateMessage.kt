package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateMessage(
    val content: String? = null,
    val embeds: List<Embed>? = null,
    val attachments: List<Attachment>? = null,
    val components: List<MessageComponent>? = null,
    @SerialName("allowed_mentions")
    val allowedMentions: AllowedMentions? = null,
) {
    @Serializable
    data class Attachment(
        val id: Int,
        @SerialName("filename")
        val fileName: String,
        val ephemeral: Boolean? = true,
    )
}
