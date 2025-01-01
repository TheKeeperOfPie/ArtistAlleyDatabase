package com.thekeeperofpie.artistalleydatabase.anime.characters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.characters.generated.resources.Res
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_filter_birthday_label
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_filter_setting_name_language
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_media_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterSortOption
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.filter.CharacterSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaDataSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.debounceState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@Inject
class CharacterSortFilterViewModel(
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    settings: CharacterSettings,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted initialParams: InitialParams,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = settings,
) {
    private val sortOption = savedStateHandle
        .getMutableStateFlow(json, "sortOption", CharacterSortOption.SEARCH_MATCH)
    private val sortAscending =
        savedStateHandle.getMutableStateFlow<Boolean>("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        enumClass = CharacterSortOption::class,
        defaultSort = CharacterSortOption.SEARCH_MATCH,
        headerText = Res.string.anime_character_media_filter_sort_label,
        sortOptions = MutableStateFlow(
            if (initialParams.allowRelevanceSort) {
                CharacterSortOption.entries
            } else {
                CharacterSortOption.entries.filter { it != CharacterSortOption.RELEVANCE }
            }
        ),
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private val birthday = savedStateHandle.getMutableStateFlow("birthday", false)
    private val birthdaySection = SortFilterSectionState.Switch(
        title = Res.string.anime_character_filter_birthday_label,
        defaultEnabled = false,
        enabled = birthday,
    )

    private val nameLanguageSection = SortFilterSectionState.Dropdown(
        labelText = Res.string.anime_character_filter_setting_name_language,
        values = AniListLanguageOption.entries,
        valueToText = { stringResource(it.textRes) },
        property = settings.languageOptionCharacters,
    )

    private val sections = listOf(
        sortSection,
        birthdaySection,
        nameLanguageSection,
        makeAdvancedSection(),
    )

    private val filterParams =
        combineStates(sortOption, sortAscending, birthday) { sortOption, sortAscending, birthday ->
            CharacterSortFilterParams(
                sort = sortOption,
                sortAscending = sortAscending,
                isBirthday = birthday,
            )
        }.debounceState(viewModelScope, 1.seconds)

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = settings.collapseAnimeFiltersOnClose,
    )

    data class InitialParams(
        val allowRelevanceSort: Boolean = false,
    )
}
