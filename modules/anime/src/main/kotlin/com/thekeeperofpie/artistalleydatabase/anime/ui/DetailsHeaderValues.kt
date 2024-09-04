package com.thekeeperofpie.artistalleydatabase.anime.ui

import androidx.compose.ui.graphics.Color
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState

interface DetailsHeaderValues {
    val coverImage: ImageState?
    val bannerImage: ImageState?
    val defaultColor: Color
        get() = Color.Unspecified
}
