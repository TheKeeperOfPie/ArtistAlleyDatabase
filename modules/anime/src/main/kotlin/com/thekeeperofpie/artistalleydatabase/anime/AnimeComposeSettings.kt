package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.runtime.staticCompositionLocalOf

interface AnimeComposeSettings {
    val screenshotMode: Boolean
}

val LocalAnimeComposeSettings =
    staticCompositionLocalOf<AnimeComposeSettings> { throw IllegalStateException() }
