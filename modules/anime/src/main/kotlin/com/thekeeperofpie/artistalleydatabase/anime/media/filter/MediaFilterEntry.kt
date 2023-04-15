package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.anime.utils.IncludeExcludeState

interface MediaFilterEntry<T> {
    val value: T
    val state: IncludeExcludeState
    val leadingIconVector: ImageVector? get() = null
    val leadingIconContentDescription: Int? get() = null
}