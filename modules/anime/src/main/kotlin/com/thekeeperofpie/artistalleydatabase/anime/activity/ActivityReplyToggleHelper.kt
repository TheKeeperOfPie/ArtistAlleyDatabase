package com.thekeeperofpie.artistalleydatabase.anime.activity

import co.touchlab.kermit.Logger
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import java.util.Collections

class ActivityReplyToggleHelper(
    private val aniListApi: AuthedAniListApi,
    private val statusController: ActivityReplyStatusController,
    private val scope: CoroutineScope,
) {
    companion object {
        private const val TAG = "ActivityLikeToggleHelper"
    }

    private val jobs = Collections.synchronizedMap(mutableMapOf<String, Job>())

    fun toggleLike(activityReplyId: String, liked: Boolean) {
        val statusUpdate = ActivityReplyStatusController.Update(
            activityReplyId = activityReplyId,
            liked = liked,
            pending = true,
        )
        statusController.onUpdate(statusUpdate)
        val job = jobs[activityReplyId]
        jobs[activityReplyId] = scope.launch(CustomDispatchers.IO) {
            job?.cancelAndJoin()
            try {
                statusController.onUpdate(
                    statusUpdate.copy(
                        liked = aniListApi.toggleActivityReplyLike(activityReplyId),
                        pending = false,
                    )
                )
            } catch (e: Throwable) {
                Logger.e(TAG, e) { "Error toggling activity like" }
                statusController.onUpdate(
                    statusUpdate.copy(liked = !liked, pending = false, error = e)
                )
            }

            jobs.remove(activityReplyId)
        }
    }
}
