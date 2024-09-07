package com.thekeeperofpie.artistalleydatabase.settings

import com.thekeeperofpie.artistalleydatabase.anime.AnimeComposeSettings

data class ComposeSettingsData(
    override val screenshotMode: Boolean = false,
) : AnimeComposeSettings
