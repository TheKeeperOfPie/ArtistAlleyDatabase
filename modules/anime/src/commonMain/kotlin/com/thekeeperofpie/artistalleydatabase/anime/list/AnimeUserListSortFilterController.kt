package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_list_their_status_chip_state_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_list_their_status_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_list_their_status_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_user_my_score_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_user_my_score_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_user_their_score_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_user_their_score_label
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaGenresController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaLicensorsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeUserListSortFilterController(
    scope: CoroutineScope,
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    mediaTagsController: MediaTagsController,
    mediaGenresController: MediaGenresController,
    mediaLicensorsController: MediaLicensorsController,
    private val targetUserId: String?,
) : AnimeSortFilterController<MediaListSortOption>(
    sortTypeEnumClass = MediaListSortOption::class,
    scope = scope,
    aniListApi = aniListApi,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
    mediaTagsController = mediaTagsController,
    mediaGenresController = mediaGenresController,
    mediaLicensorsController = mediaLicensorsController,
) {
    private val theirListStatusSection = if (targetUserId == null) {
        null
    } else {
        SortFilterSection.Filter(
            titleRes = Res.string.anime_media_filter_list_their_status_label,
            titleDropdownContentDescriptionRes = Res.string.anime_media_filter_list_their_status_content_description,
            includeExcludeIconContentDescriptionRes = Res.string.anime_media_filter_list_their_status_chip_state_content_description,
            values = listOf(
                MediaListStatus.CURRENT,
                MediaListStatus.PLANNING,
                MediaListStatus.COMPLETED,
                MediaListStatus.DROPPED,
                MediaListStatus.PAUSED,
                MediaListStatus.REPEATING,
            ),
            valueToText = { stringResource(it.value.toTextRes(MediaType.ANIME)) },
        )
    }

    // TODO: ScoreFormat support
    private val myScoreSection = SortFilterSection.Range(
        titleRes = Res.string.anime_media_filter_user_my_score_label,
        titleDropdownContentDescriptionRes = Res.string.anime_media_filter_user_my_score_expand_content_description,
        initialData = RangeData(100, hardMax = true),
    )

    private val theirScoreSection = if (targetUserId == null) {
        null
    } else {
        SortFilterSection.Range(
            titleRes = Res.string.anime_media_filter_user_their_score_label,
            titleDropdownContentDescriptionRes = Res.string.anime_media_filter_user_their_score_expand_content_description,
            initialData = RangeData(100, hardMax = true),
        )
    }

    override fun initialize(initialParams: InitialParams<MediaListSortOption>) {
        super.initialize(initialParams)
        scope.launch(CustomDispatchers.Main) {
            aniListApi.authedUser
                .mapLatest { viewer ->
                    listOfNotNull(
                        sortSection.apply {
                            if (initialParams.defaultSort != null) {
                                changeDefault(
                                    initialParams.defaultSort,
                                    sortAscending = false,
                                    lockSort = initialParams.lockSort,
                                )
                            }
                        },
                        statusSection,
                        formatSection,
                        genreSection,
                        tagSection,
                        airingDateSection.takeIf { initialParams.airingDateEnabled },
                        myListStatusSection.takeIf { viewer != null }?.apply {
                            if (initialParams.onListEnabled) {
                                if (filterOptions.none { it.value == null }) {
                                    filterOptions =
                                        filterOptions + FilterEntry.FilterEntryImpl(null)
                                }
                            } else {
                                if (filterOptions.any { it.value == null }) {
                                    filterOptions = filterOptions.filter { it.value != null }
                                }
                            }

                            if (initialParams.mediaListStatus != null) {
                                setIncluded(
                                    initialParams.mediaListStatus,
                                    initialParams.lockMediaListStatus,
                                )
                            }
                        },
                        theirListStatusSection.takeIf { viewer != null && targetUserId != null },
                        myScoreSection.takeIf { viewer != null },
                        theirScoreSection.takeIf { viewer != null && targetUserId != null },
                        episodesSection,
                        sourceSection,
                        licensedBySection,
                        titleLanguageSection,
                        suggestionsSection,
                        advancedSection.apply {
                            children = listOfNotNull(
                                showAdultSection,
                                collapseOnCloseSection,
                                hideIgnoredSection.takeIf { initialParams.showIgnoredEnabled },
                                showLessImportantTagsSection,
                                showSpoilerTagsSection,
                            )
                        },
                        SortFilterSection.Spacer(height = 32.dp),
                    )
                }
                .collectLatest { internalSections = it }
        }
    }

    @Composable
    override fun filterParams() = super.filterParams().copy(
        theirListStatuses = theirListStatusSection?.filterOptions as? List<FilterEntry<MediaListStatus>>,
        myScore = myScoreSection.data,
        theirScore = theirScoreSection?.data,
    )
}
