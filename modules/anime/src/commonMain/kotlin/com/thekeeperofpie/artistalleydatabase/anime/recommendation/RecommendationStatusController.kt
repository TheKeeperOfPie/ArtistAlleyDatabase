package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import com.anilist.type.RecommendationRating
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.runningFold
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class RecommendationStatusController {

    private val updates = MutableSharedFlow<Update>(replay = 0, extraBufferCapacity = 5)

    fun onUpdate(update: Update) = updates.tryEmit(update)

    fun allChanges() = updates.runningFold(emptyMap<Pair<String, String>, Update>()) { acc, value ->
        acc + ((value.mediaId to value.recommendationMediaId) to value)
    }

    fun allChanges(mediaId: String, recommendationMediaIds: Set<String>) =
        updates.runningFold(emptyMap<Pair<String, String>, Update>()) { acc, value ->
            if (mediaId == value.mediaId && recommendationMediaIds.contains(value.recommendationMediaId)) {
                acc + ((value.mediaId to value.recommendationMediaId) to value)
            } else {
                acc
            }
        }

    fun allChanges(filterRecommendationMediaId: String) = updates
        .filter { it.recommendationMediaId == filterRecommendationMediaId }
        .startWith(null)

    data class Update(
        val mediaId: String,
        val recommendationMediaId: String,
        val rating: RecommendationRating,
        val pending: Boolean = false,
        val error: Throwable? = null,
    )
}
