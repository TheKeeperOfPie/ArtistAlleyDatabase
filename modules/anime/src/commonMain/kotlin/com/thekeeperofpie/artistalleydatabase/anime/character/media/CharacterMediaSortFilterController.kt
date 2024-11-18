package com.thekeeperofpie.artistalleydatabase.anime.character.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_character_media_filter_on_list_label
import artistalleydatabase.modules.anime.generated.resources.anime_character_media_filter_setting_title_language
import artistalleydatabase.modules.anime.generated.resources.anime_character_media_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import org.jetbrains.compose.resources.stringResource

@OptIn(FlowPreview::class)
class CharacterMediaSortFilterController(
    scope: CoroutineScope,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val allowRelevanceSort: Boolean = false,
) : MediaDataSettingsSortFilterController<CharacterMediaSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider
) {

    private val sortSection = SortFilterSection.Sort(
        enumClass = MediaSortOption::class,
        defaultEnabled = MediaSortOption.TRENDING,
        headerTextRes = Res.string.anime_character_media_filter_sort_label,
    ).apply {
        if (!allowRelevanceSort) {
            setOptions(MediaSortOption.entries.filter { it != MediaSortOption.SEARCH_MATCH })
        }
    }

    private val onListSection = SortFilterSection.TriStateBoolean(
        titleRes = Res.string.anime_character_media_filter_on_list_label,
        defaultEnabled = null,
    )

    private val titleLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = Res.string.anime_character_media_filter_setting_title_language,
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

    @Composable
    override fun filterParams() = FilterParams(
        sort = sortSection.sortOptions,
        sortAscending = sortSection.sortAscending,
        onList = onListSection.enabled,
    )

    data class FilterParams(
        val sort: List<SortEntry<MediaSortOption>>,
        val sortAscending: Boolean,
        val onList: Boolean?,
    )
}
