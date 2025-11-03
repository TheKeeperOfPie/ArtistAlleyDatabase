package com.thekeeperofpie.artistalleydatabase.settings

import dev.zacsweers.metro.Provider

interface SettingsComponent {

    val settingsViewModel: Provider<SettingsViewModel>
}
