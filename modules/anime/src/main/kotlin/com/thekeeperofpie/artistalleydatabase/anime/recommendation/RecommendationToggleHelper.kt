package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import co.touchlab.kermit.Logger
import com.anilist.type.RecommendationRating
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import java.util.Collections

class RecommendationToggleHelper(
    private val aniListApi: AuthedAniListApi,
    private val statusController: RecommendationStatusController,
    private val scope: CoroutineScope,
) {
    companion object {
        private const val TAG = "RecommendationToggleHelper"
    }

    private val jobs = Collections.synchronizedMap(mutableMapOf<Pair<String, String>, Job>())

    fun toggle(
        data: RecommendationData,
        newRating: RecommendationRating,
    ) {
        val statusUpdate = RecommendationStatusController.Update(
            mediaId = data.mediaId,
            recommendationMediaId = data.recommendationMediaId,
            rating = newRating,
            pending = true,
        )
        statusController.onUpdate(statusUpdate)
        val key = data.mediaId to data.recommendationMediaId
        val job = jobs[key]
        jobs[key] = scope.launch(CustomDispatchers.IO) {
            job?.cancelAndJoin()
            try {
                statusController.onUpdate(
                    statusUpdate.copy(
                        rating = aniListApi.saveRecommendationRating(
                            mediaId = data.mediaId,
                            recommendationMediaId = data.recommendationMediaId,
                            rating = newRating,
                        ),
                        pending = false,
                    )
                )
            } catch (e: Throwable) {
                Logger.e(TAG, e) { "Error toggling recommendation rating" }
                statusController.onUpdate(
                    statusUpdate.copy(rating = data.userRating, pending = false, error = e)
                )
            }

            jobs.remove(key)
        }
    }
}

