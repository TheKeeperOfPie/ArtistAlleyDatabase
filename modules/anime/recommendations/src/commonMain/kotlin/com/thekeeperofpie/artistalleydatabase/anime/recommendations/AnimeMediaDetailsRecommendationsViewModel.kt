package com.thekeeperofpie.artistalleydatabase.anime.recommendations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.data.MediaDetailsQuery
import com.anilist.data.fragment.MediaPreview
import com.anilist.data.type.RecommendationRating
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AnimeMediaDetailsRecommendationsViewModel<MediaEntry>(
    private val aniListApi: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    private val recommendationStatusController: RecommendationStatusController,
    private val ignoreController: IgnoreController,
    private val settings: MediaDataSettings,
    @Assisted mediaId: String,
    @Assisted mediaEntryProvider: MediaEntryProvider<MediaPreview, MediaEntry>,
    @Assisted recommendations: Flow<MediaDetailsQuery.Data.Media.Recommendations?>,
) : ViewModel() {

    var recommendations = recommendations.flatMapLatest {
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
                    mediaEntryProvider.mediaEntry(mediaRecommendation)
                )
            }
            .orEmpty()

        val recommendationMediaIds = recommendations
            .map { mediaEntryProvider.id(it.entry) }
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
                                mediaEntryProvider.mediaFilterable(it.entry),
                            copy = { mediaEntryProvider.copyMediaEntry(this, it) },
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
        .stateIn(viewModelScope, started = SharingStarted.Eagerly, null)

    val recommendationToggleHelper =
        RecommendationToggleHelper(aniListApi, recommendationStatusController, viewModelScope)

    data class RecommendationsEntry<MediaEntry>(
        val recommendations: List<Recommendation<MediaEntry>>,
        val hasMore: Boolean,
    )

    data class Recommendation<MediaEntry>(
        val id: String,
        val data: RecommendationData,
        val entry: MediaEntry,
    )

    @AssistedInject
    class TypedFactory(
        private val aniListApi: AuthedAniListApi,
        private val mediaListStatusController: MediaListStatusController,
        private val recommendationStatusController: RecommendationStatusController,
        private val ignoreController: IgnoreController,
        private val settings: MediaDataSettings,
        @Assisted private val mediaId: String,
    ) {
        fun <MediaEntry> create(
            recommendations: Flow<MediaDetailsQuery.Data.Media.Recommendations?>,
            mediaEntryProvider: MediaEntryProvider<MediaPreview, MediaEntry>,
        ) = AnimeMediaDetailsRecommendationsViewModel(
            aniListApi = aniListApi,
            mediaListStatusController = mediaListStatusController,
            recommendationStatusController = recommendationStatusController,
            ignoreController = ignoreController,
            settings = settings,
            mediaId = mediaId,
            recommendations = recommendations,
            mediaEntryProvider = mediaEntryProvider,
        )

        @AssistedFactory
        interface Factory {
            fun create(mediaId: String): TypedFactory
        }
    }
}
