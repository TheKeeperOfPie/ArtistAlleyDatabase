package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable

@Serializable
data class ModifyThread(
    val name: String? = null,
    val archived: Boolean? = null,
)
