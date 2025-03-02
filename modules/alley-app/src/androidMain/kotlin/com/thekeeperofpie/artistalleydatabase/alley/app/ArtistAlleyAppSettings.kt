package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class ArtistAlleyAppSettings(
    private val application: Application,
    private val appScope: ApplicationScope,
) : ArtistAlleySettings {

    private val sharedPrefs =
        application.getSharedPreferences("shared_prefs", Context.MODE_PRIVATE)

    override val appTheme = initialize(
        key = "appTheme",
        initialValue = {
            getString(it, null)
                ?.let { theme -> AppThemeSetting.entries.find { it.name == theme } }
                ?: AppThemeSetting.AUTO
        },
        setValue = { key, value -> putString(key, value.name) },
    )
    override val lastKnownArtistsCsvSize = long("lastKnownArtistsCsvSize")
    override val lastKnownStampRalliesCsvSize = long("lastKnownStampRalliesCsvSize")
    override val displayType = string("displayType")
    override val artistsSortOption = string("artistsSortOption")
    override val artistsSortAscending = boolean("artistsSortAscending", true)
    override val stampRalliesSortOption = string("stampRalliesSortOption")
    override val stampRalliesSortAscending = boolean("stampRalliesSortAscending", true)
    override val showGridByDefault = boolean("showGridByDefault", false)
    override val showRandomCatalogImage = boolean("showRandomCatalogImage", false)
    override val showOnlyConfirmedTags = boolean("showOnlyConfirmedTags", false)
    override val showOnlyFavorites = boolean("showOnlyFavorites", false)
    override val showOnlyHasCommissions = boolean("showOnlyHasCommissions", false)
    override val forceOneDisplayColumn = boolean("forceOneDisplayColumn", false)
    override val dataYear = initialize(
        key = "dataYear",
        initialValue = {
            getString(it, null)?.toIntOrNull()
                ?.let { year -> DataYear.entries.find { it.year == year } } ?: DataYear.YEAR_2025
        },
        setValue = { key, value -> putString(key, value.year.toString()) },
    )

    private fun long(key: String, default: Long = -1L) = initialize(
        key,
        { getLong(it, -default) },
        SharedPreferences.Editor::putLong,
    )

    private fun string(key: String, default: String = "") = initialize(
        key,
        { getString(it, default).orEmpty() },
        SharedPreferences.Editor::putString,
    )

    private fun boolean(key: String, default: Boolean = false) = initialize(
        key,
        { getBoolean(it, default) },
        SharedPreferences.Editor::putBoolean,
    )

    private fun <T> initialize(
        key: String,
        initialValue: SharedPreferences.(String) -> T,
        setValue: SharedPreferences.Editor.(key: String, value: T) -> SharedPreferences.Editor,
    ): MutableStateFlow<T> {
        val flow = MutableStateFlow(sharedPrefs.initialValue(key))
        appScope.launch(CustomDispatchers.IO) {
            flow.drop(1).collectLatest {
                sharedPrefs.edit { setValue(key, it) }
            }
        }
        return flow
    }
}
