package com.thekeeperofpie.artistalleydatabase.anime.media

import com.thekeeperofpie.artistalleydatabase.anime.utils.IncludeExcludeState

data class MediaFilterEntry<T>(
    val value: T,
    val state: IncludeExcludeState = IncludeExcludeState.DEFAULT
)