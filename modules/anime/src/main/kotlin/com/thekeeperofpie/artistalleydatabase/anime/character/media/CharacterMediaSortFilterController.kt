package com.thekeeperofpie.artistalleydatabase.anime.character.media

import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class CharacterMediaSortFilterController(
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val allowRelevanceSort: Boolean = false,
) : SortFilterController<CharacterMediaSortFilterController.FilterParams>(
    settings,
    featureOverrideProvider
) {

    private val sortSection = SortFilterSection.Sort(
        enumClass = MediaSortOption::class,
        defaultEnabled = MediaSortOption.TRENDING,
        headerTextRes = R.string.anime_character_media_filter_sort_label,
    ).apply {
        if (!allowRelevanceSort) {
            sortOptions = sortOptions.filter { it.value != MediaSortOption.SEARCH_MATCH }
        }
    }

    private val onListSection = SortFilterSection.TriStateBoolean(
        titleRes = R.string.anime_character_media_filter_on_list_label,
        defaultEnabled = null,
    )

    private val titleLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = R.string.anime_character_media_filter_setting_title_language,
        values = AniListLanguageOption.entries,
        valueToText = { stringResource(it.textRes) },
        property = settings.languageOptionMedia,
    )

    override var sections = listOf(
        sortSection,
        onListSection,
        titleLanguageSection,
        advancedSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    override fun filterParams() = snapshotFlow {
        FilterParams(
            sort = sortSection.sortOptions,
            sortAscending = sortSection.sortAscending,
            onList = onListSection.enabled,
        )
    }.debounce(500.milliseconds)

    data class FilterParams(
        val sort: List<SortEntry<MediaSortOption>>,
        val sortAscending: Boolean,
        val onList: Boolean?,
    )
}
