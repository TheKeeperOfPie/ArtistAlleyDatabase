package com.thekeeperofpie.artistalleydatabase.alley.settings

import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_settings_series_language
import artistalleydatabase.modules.alley.generated.resources.alley_settings_theme
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleyExporter
import com.thekeeperofpie.artistalleydatabase.alley.tags.textWithExplanation
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.settings.ui.SettingsSection
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlinx.io.writeString
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource

@Inject
class AlleySettingsViewModel(
    private val dispatchers: CustomDispatchers,
    settings: ArtistAlleySettings,
    private val exporter: AlleyExporter,
) : ViewModel() {

    private val themeSection = SettingsSection.Dropdown(
        labelTextRes = Res.string.alley_settings_theme,
        options = AppThemeSetting.entries,
        optionToText = { stringResource(it.textRes) },
        property = settings.appTheme,
    )

    private val seriesLanguageSection = SettingsSection.Dropdown(
        labelTextRes = Res.string.alley_settings_series_language,
        options = AniListLanguageOption.entries,
        optionToText = { stringResource(it.textWithExplanation) },
        property = settings.languageOption,
    )

    val state =
        AlleySettingsScreen.State(
            sections = listOf(
                SettingsSection.Placeholder("header"),
                themeSection,
                seriesLanguageSection,
                SettingsSection.Placeholder("export"),
                SettingsSection.HorizontalDivider("exportDivider", horizontalPadding = 16.dp),
                SettingsSection.Placeholder("import"),
                SettingsSection.Placeholder("faq"),
                SettingsSection.Spacer("bottomSpacer", 80.dp),
            )
        )

    private var importJob: Job? = null

    fun onEvent(event: AlleySettingsScreen.Event) = when (event) {
        AlleySettingsScreen.Event.ExportPartial -> viewModelScope.launch(dispatchers.io) {
            Buffer().use {
                exporter.exportPartial(it)
                state.exportPartialText = it.readString()
            }
        }
        AlleySettingsScreen.Event.ExportFull -> viewModelScope.launch(dispatchers.io) {
            Buffer().use {
                // TODO: Export to a user chosen file instead
                exporter.exportFull(it)
                state.exportPartialText = it.readString()
            }
        }
        is AlleySettingsScreen.Event.Import -> {
            val previousJob = importJob
            previousJob?.cancel()
            importJob = viewModelScope.launch(dispatchers.io) {
                state.importState = LoadingResult.loading<Unit>()
                previousJob?.join()
                Buffer().use {
                    it.writeString(event.data)
                    state.importState = exporter.import(it)
                }
            }
        }
    }
}
