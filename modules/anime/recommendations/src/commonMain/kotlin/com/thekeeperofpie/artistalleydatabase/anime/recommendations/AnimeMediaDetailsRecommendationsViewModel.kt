package com.thekeeperofpie.artistalleydatabase.anime.recommendations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.data.MediaDetailsQuery
import com.anilist.data.fragment.MediaPreview
import com.anilist.data.type.RecommendationRating
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaFilterableData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AnimeMediaDetailsRecommendationsViewModel<MediaEntry>(
    private val aniListApi: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    private val recommendationStatusController: RecommendationStatusController,
    private val ignoreController: IgnoreController,
    private val settings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted recommendationsProvider: RecommendationsProvider<MediaEntry>,
) : ViewModel() {

    var recommendations by mutableStateOf<RecommendationsEntry<MediaEntry>?>(null)
        private set

    val recommendationToggleHelper =
        RecommendationToggleHelper(aniListApi, recommendationStatusController, viewModelScope)

    private val mediaId = savedStateHandle.get<String>("mediaId")!!

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            recommendationsProvider.recommendations()
                .flatMapLatest {
                    val pageInfo = it?.pageInfo
                    val recommendations = it?.edges?.filterNotNull()
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
                                recommendationsProvider.mediaEntry(mediaRecommendation)
                            )
                        }
                        .orEmpty()

                    val recommendationMediaIds = recommendations
                        .map { recommendationsProvider.mediaFilterable(it.entry).mediaId }
                        .toSet()

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
                                        filterableData =
                                            recommendationsProvider.mediaFilterable(it.entry),
                                        copy = { recommendationsProvider.copyMediaEntry(this, it) },
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
                            hasMore = pageInfo?.hasNextPage != false,
                        )
                    }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { recommendations = it }
        }
    }


    data class RecommendationsEntry<MediaEntry>(
        val recommendations: List<Recommendation<MediaEntry>>,
        val hasMore: Boolean,
    )

    data class Recommendation<MediaEntry>(
        val id: String,
        val data: RecommendationData,
        val entry: MediaEntry,
    )

    interface RecommendationsProvider<MediaEntry> {
        fun recommendations(): Flow<MediaDetailsQuery.Data.Media.Recommendations?>

        /** Proxies to a real type to decouple the media data class from recommendations */
        fun mediaEntry(media: MediaPreview): MediaEntry
        fun mediaFilterable(entry: MediaEntry): MediaFilterableData
        fun copyMediaEntry(entry: MediaEntry, data: MediaFilterableData): MediaEntry
    }

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val mediaListStatusController: MediaListStatusController,
        private val recommendationStatusController: RecommendationStatusController,
        private val ignoreController: IgnoreController,
        private val settings: MediaDataSettings,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) {
        fun <MediaEntry> create(
            recommendationsProvider: RecommendationsProvider<MediaEntry>,
        ) = AnimeMediaDetailsRecommendationsViewModel(
            aniListApi = aniListApi,
            mediaListStatusController = mediaListStatusController,
            recommendationStatusController = recommendationStatusController,
            ignoreController = ignoreController,
            settings = settings,
            savedStateHandle = savedStateHandle,
            recommendationsProvider = recommendationsProvider,
        )
    }
}
