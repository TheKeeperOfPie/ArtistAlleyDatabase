package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.type.RecommendationRating
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnimeMediaDetailsRecommendationsViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    private val recommendationStatusController: RecommendationStatusController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var recommendations by mutableStateOf<RecommendationsEntry?>(null)
        private set

    val recommendationToggleHelper =
        RecommendationToggleHelper(aniListApi, recommendationStatusController, viewModelScope)

    private val mediaId = savedStateHandle.get<String>("mediaId")!!
    private var initialized = false

    fun initialize(mediaDetailsViewModel: AnimeMediaDetailsViewModel) {
        if (initialized) return
        initialized = true

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

                    Log.d("RecommendationsDebug", "recommendations = $recommendations")
                    combine(
                        mediaListStatusController.allChanges(recommendationMediaIds),
                        recommendationStatusController.allChanges(
                            mediaId,
                            recommendationMediaIds,
                        ),
                        ignoreController.updates(),
                        settings.showAdult,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { mediaListUpdates, recommendationUpdates, _, showAdult, showLessImportantTags, showSpoilerTags ->
                        RecommendationsEntry(
                            recommendations = recommendations.mapNotNull {
                                applyMediaFiltering(
                                    statuses = mediaListUpdates,
                                    ignoreController = ignoreController,
                                    showAdult = showAdult,
                                    showIgnored = true,
                                    showLessImportantTags = showLessImportantTags,
                                    showSpoilerTags = showSpoilerTags,
                                    entry = it,
                                    transform = { it.entry },
                                    media = it.entry.media,
                                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                                        copy(
                                            entry = this.entry.copy(
                                                media = it.entry.media,
                                                mediaListStatus = mediaListStatus,
                                                progress = progress,
                                                progressVolumes = progressVolumes,
                                                scoreRaw = scoreRaw,
                                                ignored = ignored,
                                                showLessImportantTags = showLessImportantTags,
                                                showSpoilerTags = showSpoilerTags,
                                            )
                                        )
                                    }
                                )?.let {
                                    val recommendationUpdate =
                                        recommendationUpdates[it.data.mediaId to it.data.recommendationMediaId]
                                    val userRating = recommendationUpdate?.rating
                                        ?: it.data.userRating
                                    it.transformIf(userRating != it.data.userRating) {
                                        copy(
                                            data = data.copy(
                                                userRating = userRating
                                            )
                                        )
                                    }
                                }
                            }.toImmutableList(),
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
        val recommendations: ImmutableList<Recommendation>,
        val hasMore: Boolean,
    )

    data class Recommendation(
        val id: String,
        val data: RecommendationData,
        val entry: MediaPreviewEntry,
    )
}
