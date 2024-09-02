package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

actual data class StringResourceId actual constructor(private val id: Int) : StringResourceCompat {
    @Composable
    override fun text() = stringResource(id)
}
