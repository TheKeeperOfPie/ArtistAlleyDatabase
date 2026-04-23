package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModifyThread(
    val name: String? = null,
    val archived: Boolean? = null,
    @SerialName("applied_tags")
    val appliedTags: List<Long>? = null,
)
