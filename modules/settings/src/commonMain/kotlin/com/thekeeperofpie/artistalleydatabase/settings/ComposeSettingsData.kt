package com.thekeeperofpie.artistalleydatabase.settings

import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeSettings

data class ComposeSettingsData(
    override val screenshotMode: Boolean = false,
) : ComposeSettings
