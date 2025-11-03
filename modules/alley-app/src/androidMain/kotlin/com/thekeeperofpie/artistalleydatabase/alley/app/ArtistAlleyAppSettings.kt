package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@SingleIn(AppScope::class)
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
    override val displayType = initialize(
        key = "displayType",
        initialValue = {
            getString(it, null)
                ?.let { value -> SearchScreen.DisplayType.entries.find { it.name == value } }
                ?: SearchScreen.DisplayType.CARD
        },
        setValue = { key, value -> putString(key, value.name) },
    )
    override val artistsSortOption = initialize(
        key = "artistsSortOption",
        initialValue = {
            getString(it, null)
                ?.let { value -> ArtistSearchSortOption.entries.find { it.name == value } }
                ?: ArtistSearchSortOption.RANDOM
        },
        setValue = { key, value -> putString(key, value.name) },
    )
    override val artistsSortAscending = boolean("artistsSortAscending", true)
    override val stampRalliesSortOption = initialize(
        key = "stampRalliesSortOption",
        initialValue = {
            getString(it, null)
                ?.let { value -> StampRallySearchSortOption.entries.find { it.name == value } }
                ?: StampRallySearchSortOption.RANDOM
        },
        setValue = { key, value -> putString(key, value.name) },
    )
    override val stampRalliesSortAscending = boolean("stampRalliesSortAscending", true)
    override val seriesSortOption = initialize(
        key = "seriesSortOption",
        initialValue = {
            getString(it, null)
                ?.let { value -> SeriesSearchSortOption.entries.find { it.name == value } }
                ?: SeriesSearchSortOption.RANDOM
        },
        setValue = { key, value -> putString(key, value.name) },
    )
    override val seriesSortAscending = boolean("seriesSortAscending", true)
    override val showGridByDefault = boolean("showGridByDefault", false)
    override val showRandomCatalogImage = boolean("showRandomCatalogImage", false)
    override val showOnlyConfirmedTags = boolean("showOnlyConfirmedTags", false)
    override val showOnlyWithCatalog = boolean("showOnlyWithCatalog", false)
    override val forceOneDisplayColumn = boolean("forceOneDisplayColumn", false)
    override val dataYear = initialize(
        key = "dataYear",
        initialValue = {
            getString(it, null)
                ?.let { serializedName -> DataYear.entries.find { it.serializedName == serializedName } }
                ?: DataYear.LATEST
        },
        setValue = { key, value -> putString(key, value.serializedName) },
    )
    override val languageOption = initialize(
        key = "languageOption",
        initialValue = {
            getString(it, null)
                ?.let { option -> AniListLanguageOption.entries.find { it.name == option } }
                ?: AniListLanguageOption.DEFAULT
        },
        setValue = { key, value -> putString(key, value.name) },
    )
    override val showOutdatedCatalogs = boolean("showOutdatedCatalogs", false)

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
