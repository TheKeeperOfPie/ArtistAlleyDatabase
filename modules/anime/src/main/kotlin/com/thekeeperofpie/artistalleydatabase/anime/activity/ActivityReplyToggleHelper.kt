package com.thekeeperofpie.artistalleydatabase.anime.activity

import android.util.Log
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
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
                Log.e(TAG, "Error toggling activity like", e)
                statusController.onUpdate(
                    statusUpdate.copy(liked = !liked, pending = false, error = e)
                )
            }

            jobs.remove(activityReplyId)
        }
    }
}
