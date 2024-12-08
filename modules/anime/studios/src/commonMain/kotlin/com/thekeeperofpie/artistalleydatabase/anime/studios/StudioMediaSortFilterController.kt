package com.thekeeperofpie.artistalleydatabase.anime.studios

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.studios.generated.resources.Res
import artistalleydatabase.modules.anime.studios.generated.resources.anime_studio_media_filter_main_label
import artistalleydatabase.modules.anime.studios.generated.resources.anime_studio_media_filter_setting_title_language
import artistalleydatabase.modules.anime.studios.generated.resources.anime_studio_media_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import org.jetbrains.compose.resources.stringResource

@OptIn(FlowPreview::class)
class StudioMediaSortFilterController(
    scope: CoroutineScope,
    settings: MediaDataSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : MediaDataSettingsSortFilterController<StudioMediaSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {
    private val sortSection = SortFilterSection.Sort(
        enumClass = MediaSortOption::class,
        defaultEnabled = MediaSortOption.TRENDING,
        headerTextRes = Res.string.anime_studio_media_filter_sort_label,
    ).apply {
        setOptions(MediaSortOption.entries.filter { it != MediaSortOption.SEARCH_MATCH })
    }

    private val mainSection = SortFilterSection.TriStateBoolean(
        titleRes = Res.string.anime_studio_media_filter_main_label,
        defaultEnabled = false,
    )

    private val titleLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = Res.string.anime_studio_media_filter_setting_title_language,
        values = AniListLanguageOption.entries,
        valueToText = { stringResource(it.textRes) },
        property = settings.languageOptionMedia,
    )

    override var sections = listOf(
        sortSection,
        mainSection,
        titleLanguageSection,
        advancedSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    @Composable
    override fun filterParams() = FilterParams(
        sort = sortSection.sortOptions,
        sortAscending = sortSection.sortAscending,
        main = mainSection.enabled,
    )

    data class FilterParams(
        val sort: List<SortEntry<MediaSortOption>>,
        val sortAscending: Boolean,
        val main: Boolean?,
    )
}
