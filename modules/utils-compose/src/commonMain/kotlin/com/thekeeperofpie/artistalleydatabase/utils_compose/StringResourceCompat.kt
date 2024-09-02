package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource

interface StringResourceCompat {

    @Composable
    fun text(): String
}


data class StringResourceCompose(private val stringResource: StringResource) : StringResourceCompat {
    @Composable
    override fun text() = ComposeResourceUtils.stringResource(stringResource)
}

expect class StringResourceId(id :Int) : StringResourceCompat
