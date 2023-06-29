package com.thekeeperofpie.artistalleydatabase.compose.filter

import androidx.compose.ui.graphics.vector.ImageVector

interface FilterEntry<T> {
    val value: T
    val state: FilterIncludeExcludeState
    val leadingIconVector: ImageVector? get() = null
    val leadingIconContentDescription: Int? get() = null
}
