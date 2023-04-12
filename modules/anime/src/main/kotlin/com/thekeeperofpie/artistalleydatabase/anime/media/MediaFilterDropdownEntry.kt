package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.thekeeperofpie.artistalleydatabase.android_utils.Either

interface MediaFilterDropdownEntry<T> {
    val value: T
    val text: Either<String, Int>
    val dropdownContentDescriptionRes: Int

    @Composable
    fun toText() = when (val text = text) {
        is Either.Left -> text.value
        is Either.Right -> stringResource(text.value)
    }
}