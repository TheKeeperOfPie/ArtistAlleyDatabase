package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import kotlinx.serialization.Serializable

@Serializable
data class MerchInfo(
    val name: String,
    val uuid: String,
    val notes: String?,
)
