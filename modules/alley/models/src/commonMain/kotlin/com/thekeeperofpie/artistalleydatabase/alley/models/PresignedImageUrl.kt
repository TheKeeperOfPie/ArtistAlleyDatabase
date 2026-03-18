package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable

@Serializable
data class PresignedImageUrl(
    val key: String,
    val url: String,
)
