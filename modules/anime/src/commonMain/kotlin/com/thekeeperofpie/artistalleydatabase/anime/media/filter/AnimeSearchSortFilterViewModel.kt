package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_suggestions_current_season
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_suggestions_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_suggestions_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_suggestions_last_season
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_suggestions_next_season
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_suggestions_recent_finished
import com.anilist.data.type.MediaFormat
import com.anilist.data.type.MediaStatus
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.AiringDate
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.debounceState
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

class AnimeSearchSortFilterViewModel(
    aniListApi: AuthedAniListApi,
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaGenresController: MediaGenresController,
    mediaLicensorsController: MediaLicensorsController,
    mediaTagsController: MediaTagsController,
    mediaDataSettings: MediaDataSettings,
    @Assisted initialParams: InitialParams<MediaSortOption>,
    @Assisted savedStateHandle: SavedStateHandle,
) : AnimeSortFilterViewModel<MediaSortOption>(
    aniListApi = aniListApi,
    featureOverrideProvider = featureOverrideProvider,
    json = json,
    mediaGenresController = mediaGenresController,
    mediaLicensorsController = mediaLicensorsController,
    mediaTagsController = mediaTagsController,
    mediaDataSettings = mediaDataSettings,
    sortOptions = MutableStateFlow(MediaSortOption.entries
        .filter { it != MediaSortOption.VOLUMES && it != MediaSortOption.CHAPTERS }),
    initialParams = initialParams,
    savedStateHandle = savedStateHandle,
) {
    val suggestionsSection = SortFilterSectionState.Suggestions(
        title = Res.string.anime_media_filter_suggestions_label,
        titleDropdownContentDescription = Res.string.anime_media_filter_suggestions_expand_content_description,
        suggestions = AnimeFilterSuggestion.entries.toList(),
        onSuggestionClick = {
            when (it) {
                AnimeFilterSuggestion.RECENT_FINISHED -> {
                    sortOptionEnabled.value = MediaSortOption.END_DATE
                    sortAscending.value = false
                    statusIn.value = setOf(MediaStatus.FINISHED)
                    statusNotIn.value = emptySet()
                    formatIn.value = setOf(MediaFormat.TV)
                    formatNotIn.value = emptySet()
                    myListStatusIn.value = emptySet()
                    myListStatusNotIn.value = setOf(null)
                    airingDate.value = AiringDate.Basic() to airingDate.value.second
                    airingDateIsAdvanced.value = false
                }
                AnimeFilterSuggestion.LAST_SEASON -> {
                    sortOptionEnabled.value = MediaSortOption.POPULARITY
                    sortAscending.value = false
                    statusIn.value = emptySet()
                    statusNotIn.value = emptySet()
                    formatIn.value = setOf(MediaFormat.TV)
                    formatNotIn.value = emptySet()
                    myListStatusIn.value = emptySet()
                    myListStatusNotIn.value = emptySet()
                    val (_, year) = AniListUtils.getPreviousSeasonYear()
                    airingDate.value = AiringDate.Basic(
                        AiringDate.SeasonOption.PREVIOUS,
                        year.toString(),
                    ) to airingDate.value.second
                    airingDateIsAdvanced.value = false
                }
                AnimeFilterSuggestion.CURRENT_SEASON -> {
                    sortOptionEnabled.value = MediaSortOption.POPULARITY
                    sortAscending.value = false
                    statusIn.value = emptySet()
                    statusNotIn.value = emptySet()
                    formatIn.value = setOf(MediaFormat.TV)
                    formatNotIn.value = emptySet()
                    myListStatusIn.value = emptySet()
                    myListStatusNotIn.value = emptySet()
                    val (_, year) = AniListUtils.getCurrentSeasonYear()
                    airingDate.value = AiringDate.Basic(
                        AiringDate.SeasonOption.CURRENT,
                        year.toString(),
                    ) to airingDate.value.second
                    airingDateIsAdvanced.value = false
                }
                AnimeFilterSuggestion.NEXT_SEASON -> {
                    sortOptionEnabled.value = MediaSortOption.POPULARITY
                    sortAscending.value = false
                    statusIn.value = emptySet()
                    statusNotIn.value = emptySet()
                    formatIn.value = setOf(MediaFormat.TV)
                    formatNotIn.value = emptySet()
                    myListStatusIn.value = emptySet()
                    myListStatusNotIn.value = emptySet()
                    val (_, year) = AniListUtils.getNextSeasonYear()
                    airingDate.value = AiringDate.Basic(
                        AiringDate.SeasonOption.NEXT,
                        year.toString(),
                    ) to airingDate.value.second
                    airingDateIsAdvanced.value = false
                }
            }
        }
    )

    @Suppress("UNCHECKED_CAST")
    override val filterParams = animeFilterParams.debounceState(viewModelScope, 1.seconds)

    override val sections = aniListApi.authedUser
        .mapState(viewModelScope) { viewer ->
            listOfNotNull(
                sortSection,
                statusSection,
                formatSection,
                genreSection,
                tagSection,
                airingDateSection.takeIf { initialParams.airingDateEnabled },
                myListStatusSection.takeIf { viewer != null },
                averageScoreSection,
                episodesSection,
                sourceSection,
                licensedBySection,
                titleLanguageSection,
                suggestionsSection,
                advancedSection,
            )
        }

    override val state by lazy {
        SortFilterState(
            sections = sections,
            filterParams = filterParams,
            collapseOnClose = collapseOnClose,
        )
    }

    enum class AnimeFilterSuggestion(private val textRes: StringResource) :
        SortFilterSectionState.Suggestions.Suggestion {
        RECENT_FINISHED(Res.string.anime_media_filter_suggestions_recent_finished),
        LAST_SEASON(Res.string.anime_media_filter_suggestions_last_season),
        CURRENT_SEASON(Res.string.anime_media_filter_suggestions_current_season),
        NEXT_SEASON(Res.string.anime_media_filter_suggestions_next_season),
        ;

        @Composable
        override fun text() = stringResource(textRes)
    }

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val featureOverrideProvider: FeatureOverrideProvider,
        private val json: Json,
        private val mediaGenresController: MediaGenresController,
        private val mediaLicensorsController: MediaLicensorsController,
        private val mediaTagsController: MediaTagsController,
        private val mediaDataSettings: MediaDataSettings,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) {
        fun create(@Assisted initialParams: InitialParams<MediaSortOption>) =
            AnimeSearchSortFilterViewModel(
                aniListApi = aniListApi,
                featureOverrideProvider = featureOverrideProvider,
                json = json,
                mediaGenresController = mediaGenresController,
                mediaLicensorsController = mediaLicensorsController,
                mediaTagsController = mediaTagsController,
                mediaDataSettings = mediaDataSettings,
                initialParams = initialParams,
                savedStateHandle = savedStateHandle,
            )
    }
}
