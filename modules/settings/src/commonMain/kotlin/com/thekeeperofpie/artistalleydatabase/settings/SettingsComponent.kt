package com.thekeeperofpie.artistalleydatabase.settings

interface SettingsComponent {

    val settingsViewModel: () -> SettingsViewModel
}
