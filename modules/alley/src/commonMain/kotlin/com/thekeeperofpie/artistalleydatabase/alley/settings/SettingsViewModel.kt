package com.thekeeperofpie.artistalleydatabase.alley.settings

import androidx.lifecycle.ViewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_settings_theme
import com.thekeeperofpie.artistalleydatabase.settings.ui.SettingsSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource

@Inject
class SettingsViewModel(settings: ArtistAlleySettings) : ViewModel() {

    private val themeSection = SettingsSection.Dropdown(
        labelTextRes = Res.string.alley_settings_theme,
        options = AppThemeSetting.entries,
        optionToText = { stringResource(it.textRes) },
        property = settings.appTheme,
    )

    val sections = listOf(themeSection)
}
