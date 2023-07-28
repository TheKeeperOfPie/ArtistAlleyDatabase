package com.thekeeperofpie.artistalleydatabase.settings

import androidx.annotation.StringRes

enum class AppThemeSetting(@StringRes val textRes: Int) {
    AUTO(R.string.settings_subsection_theme_auto),
    DARK(R.string.settings_subsection_theme_dark),
    LIGHT(R.string.settings_subsection_theme_light),
    ;
}
