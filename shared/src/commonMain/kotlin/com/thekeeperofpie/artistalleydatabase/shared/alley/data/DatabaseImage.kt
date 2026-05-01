package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import androidx.annotation.ColorInt
import kotlinx.serialization.Serializable

@Serializable
data class DatabaseImage(
    val name: String,
    val width: Int?,
    val height: Int?,
    @ColorInt
    val color: Int? = null,
)
