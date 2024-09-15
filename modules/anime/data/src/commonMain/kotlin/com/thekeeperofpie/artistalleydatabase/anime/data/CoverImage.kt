package com.thekeeperofpie.artistalleydatabase.anime.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class CoverImage(
    val url: String?,
    val color: Color?,
)
