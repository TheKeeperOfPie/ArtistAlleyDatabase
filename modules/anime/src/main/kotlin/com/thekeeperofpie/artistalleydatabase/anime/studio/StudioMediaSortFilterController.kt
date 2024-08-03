package com.thekeeperofpie.artistalleydatabase.anime.studio

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.AnimeSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class StudioMediaSortFilterController(
    scope: CoroutineScope,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : AnimeSettingsSortFilterController<StudioMediaSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {
    private val sortSection = SortFilterSection.Sort(
        enumClass = MediaSortOption::class,
        defaultEnabled = MediaSortOption.TRENDING,
        headerTextRes = R.string.anime_studio_media_filter_sort_label,
    ).apply {
        setOptions(MediaSortOption.entries.filter { it != MediaSortOption.SEARCH_MATCH })
    }

    private val mainSection = SortFilterSection.TriStateBoolean(
        titleRes = R.string.anime_studio_media_filter_main_label,
        defaultEnabled = false,
    )

    private val titleLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = R.string.anime_studio_media_filter_setting_title_language,
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
