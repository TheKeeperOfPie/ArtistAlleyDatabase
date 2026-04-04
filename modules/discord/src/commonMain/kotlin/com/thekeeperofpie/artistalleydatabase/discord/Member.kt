package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val user: User,
) {
}
