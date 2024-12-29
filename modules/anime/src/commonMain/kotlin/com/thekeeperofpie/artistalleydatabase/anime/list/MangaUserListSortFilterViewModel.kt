package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_list_their_status_chip_state_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_list_their_status_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_list_their_status_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_user_my_score_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_user_my_score_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_user_their_score_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_user_their_score_label
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MangaSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaGenresController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaLicensorsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource

class MangaUserListSortFilterViewModel(
    aniListApi: AuthedAniListApi,
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaGenresController: MediaGenresController,
    mediaLicensorsController: MediaLicensorsController,
    mediaTagsController: MediaTagsController,
    mediaDataSettings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted targetUserId: String?,
    @Assisted initialParams: InitialParams<MediaListSortOption>,
) : MangaSortFilterViewModel<MediaListSortOption>(
    aniListApi = aniListApi,
    featureOverrideProvider = featureOverrideProvider,
    json = json,
    mediaGenresController = mediaGenresController,
    mediaLicensorsController = mediaLicensorsController,
    mediaTagsController = mediaTagsController,
    mediaDataSettings = mediaDataSettings,
    initialParams = initialParams,
    savedStateHandle = savedStateHandle,
) {
    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            aniListApi.authedUser.collectLatest { viewer ->
                sortSection.sortOptions.value =
                    MediaListSortOption.entries.filter {
                        when (it.forDifferentUser) {
                            true -> targetUserId != null
                            false -> viewer != null
                            null -> true
                        }
                    }
            }
        }
    }

    private val theirListStatusIn =
        savedStateHandle.getMutableStateFlow<String, Set<MediaListStatus>>(
            key = "theirListStatusIn",
            initialValue = { emptySet() },
            serialize = json::encodeToString,
            deserialize = json::decodeFromString,
        )
    private val theirListStatusNotIn =
        savedStateHandle.getMutableStateFlow<String, Set<MediaListStatus>>(
            key = "theirListStatusNotIn",
            initialValue = { emptySet() },
            serialize = json::encodeToString,
            deserialize = json::decodeFromString,
        )
    private val theirListStatusSection = if (targetUserId == null) {
        null
    } else {
        SortFilterSectionState.Filter(
            title = Res.string.anime_media_filter_list_their_status_label,
            titleDropdownContentDescription = Res.string.anime_media_filter_list_their_status_content_description,
            includeExcludeIconContentDescription = Res.string.anime_media_filter_list_their_status_chip_state_content_description,
            options = MutableStateFlow(
                listOf(
                    MediaListStatus.CURRENT,
                    MediaListStatus.PLANNING,
                    MediaListStatus.COMPLETED,
                    MediaListStatus.DROPPED,
                    MediaListStatus.PAUSED,
                    MediaListStatus.REPEATING,
                )
            ),
            filterIn = theirListStatusIn,
            filterNotIn = theirListStatusNotIn,
            valueToText = { stringResource(it.toTextRes(MediaType.ANIME)) },
        )
    }

    private val initialScoreRangeData = RangeData(100, hardMax = true)
    // TODO: ScoreFormat support
    private val myScore = savedStateHandle.getMutableStateFlow<String, RangeData>(
        key = "myScore",
        initialValue = { initialScoreRangeData },
        serialize = json::encodeToString,
        deserialize = json::decodeFromString,
    )
    private val myScoreSection = SortFilterSectionState.Range(
        title = Res.string.anime_media_filter_user_my_score_label,
        titleDropdownContentDescription = Res.string.anime_media_filter_user_my_score_expand_content_description,
        data = myScore,
        initialData = initialScoreRangeData,
    )

    private val theirScore = savedStateHandle.getMutableStateFlow<String, RangeData>(
        key = "theirScore",
        initialValue = { initialScoreRangeData },
        serialize = json::encodeToString,
        deserialize = json::decodeFromString,
    )
    private val theirScoreSection = if (targetUserId == null) {
        null
    } else {
        SortFilterSectionState.Range(
            title = Res.string.anime_media_filter_user_their_score_label,
            titleDropdownContentDescription = Res.string.anime_media_filter_user_their_score_expand_content_description,
            data = theirScore,
            initialData = initialScoreRangeData,
        )
    }

    @Suppress("UNCHECKED_CAST")
    val animeUserListFilterParams = combineStates(
        mangaFilterParams,
        theirListStatusIn,
        theirListStatusNotIn,
        myScore,
        theirScore,
    ) {
        val baseParams = it[0] as MediaSearchFilterParams<MediaListSortOption>
        val theirListStatusIn = it[1] as Set<MediaListStatus>
        val theirListStatusNotIn = it[2] as Set<MediaListStatus>
        val myScore = it[3] as RangeData
        val theirScore = it[4] as RangeData
        baseParams.copy(
            theirListStatusIn = theirListStatusIn.toList(),
            theirListStatusNotIn = theirListStatusNotIn.toList(),
            myScore = myScore.takeUnless { it == initialScoreRangeData },
            theirScore = theirScore.takeUnless { it == initialScoreRangeData },
        )
    }

    override val filterParams = animeUserListFilterParams

    override val sections = aniListApi.authedUser
        .mapState(viewModelScope) { viewer ->
            listOfNotNull(
                sortSection,
                statusSection,
                formatSection,
                genreSection,
                tagSection,
                releaseDateSection.takeIf { initialParams.airingDateEnabled },
                myListStatusSection.takeIf { viewer != null },
                theirListStatusSection.takeIf { viewer != null && targetUserId != null },
                myScoreSection.takeIf { viewer != null },
                theirScoreSection.takeIf { viewer != null && targetUserId != null },
                averageScoreSection,
                volumesSection,
                chaptersSection,
                sourceSection,
                licensedBySection,
                titleLanguageSection,
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
        @Assisted private val targetUserId: String?,
    ) {
        fun create(@Assisted initialParams: InitialParams<MediaListSortOption>) =
            MangaUserListSortFilterViewModel(
                aniListApi = aniListApi,
                featureOverrideProvider = featureOverrideProvider,
                json = json,
                mediaGenresController = mediaGenresController,
                mediaLicensorsController = mediaLicensorsController,
                mediaTagsController = mediaTagsController,
                mediaDataSettings = mediaDataSettings,
                savedStateHandle = savedStateHandle,
                targetUserId = targetUserId,
                initialParams = initialParams,
            )
    }
}
