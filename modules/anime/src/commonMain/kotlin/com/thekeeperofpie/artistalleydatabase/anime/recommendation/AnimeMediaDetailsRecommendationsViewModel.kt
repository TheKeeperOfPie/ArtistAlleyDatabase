package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.type.RecommendationRating
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AnimeMediaDetailsRecommendationsViewModel(
    private val aniListApi: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    private val recommendationStatusController: RecommendationStatusController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted mediaDetailsViewModel: AnimeMediaDetailsViewModel,
) : ViewModel() {

    var recommendations by mutableStateOf<RecommendationsEntry?>(null)
        private set

    val recommendationToggleHelper =
        RecommendationToggleHelper(aniListApi, recommendationStatusController, viewModelScope)

    private val mediaId = savedStateHandle.get<String>("mediaId")!!

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { mediaDetailsViewModel.entry2.result }
                .filterNotNull()
                .take(1)
                .flatMapLatest {
                    val media = it.media ?: return@flatMapLatest flowOf(null)
                    val recommendations = media.recommendations?.edges?.filterNotNull()
                        ?.mapNotNull {
                            val node = it.node ?: return@mapNotNull null
                            val mediaRecommendation = node.mediaRecommendation
                            Recommendation(
                                node.id.toString(),
                                RecommendationData(
                                    mediaId = mediaId,
                                    recommendationMediaId = mediaRecommendation.id.toString(),
                                    rating = node.rating ?: 0,
                                    userRating = node.userRating
                                        ?: RecommendationRating.NO_RATING,
                                ),
                                MediaPreviewEntry(mediaRecommendation)
                            )
                        }
                        .orEmpty()

                    val recommendationMediaIds =
                        recommendations.map { it.entry.media.id.toString() }.toSet()

                    combine(
                        mediaListStatusController.allChanges(recommendationMediaIds),
                        recommendationStatusController.allChanges(
                            mediaId,
                            recommendationMediaIds,
                        ),
                        ignoreController.updates(),
                        settings.mediaFilteringData(forceShowIgnored = true),
                    ) { mediaListUpdates, recommendationUpdates, _, filteringData ->
                        RecommendationsEntry(
                            recommendations = recommendations.mapNotNull {
                                val filtered = it.copy(
                                    entry = applyMediaFiltering(
                                        statuses = mediaListUpdates,
                                        ignoreController = ignoreController,
                                        filteringData = filteringData,
                                        entry = it.entry,
                                        filterableData = it.entry.mediaFilterable,
                                        copy = { copy(mediaFilterable = it) },
                                    ) ?: return@mapNotNull null
                                )
                                val recommendationUpdate =
                                    recommendationUpdates[it.data.mediaId to it.data.recommendationMediaId]
                                val userRating = recommendationUpdate?.rating
                                    ?: it.data.userRating
                                filtered.transformIf(userRating != it.data.userRating) {
                                    copy(
                                        data = data.copy(
                                            userRating = userRating
                                        )
                                    )
                                }
                            },
                            hasMore = media.recommendations?.pageInfo?.hasNextPage
                                ?: true,
                        )
                    }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { recommendations = it }
        }
    }


    data class RecommendationsEntry(
        val recommendations: List<Recommendation>,
        val hasMore: Boolean,
    )

    data class Recommendation(
        val id: String,
        val data: RecommendationData,
        val entry: MediaPreviewEntry,
    )
}
