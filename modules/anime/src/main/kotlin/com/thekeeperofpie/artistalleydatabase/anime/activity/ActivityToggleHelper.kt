package com.thekeeperofpie.artistalleydatabase.anime.activity

import android.util.Log
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import java.util.Collections

class ActivityToggleHelper(
    private val aniListApi: AuthedAniListApi,
    private val statusController: ActivityStatusController,
    private val scope: CoroutineScope,
) {
    companion object {
        private const val TAG = "ActivityLikeToggleHelper"
    }

    private val jobs = Collections.synchronizedMap(mutableMapOf<String, Job>())

    fun toggle(update: ActivityToggleUpdate) {
        val activityId = update.id
        val statusUpdate = ActivityStatusController.Update(
            activityId = activityId,
            liked = update.liked,
            update.subscribed,
            pending = true
        )
        statusController.onUpdate(statusUpdate)
        val job = jobs[activityId]
        jobs[activityId] = scope.launch(CustomDispatchers.IO) {
            job?.cancelAndJoin()
            when (update) {
                is ActivityToggleUpdate.Liked -> {
                    try {
                        statusController.onUpdate(
                            statusUpdate.copy(
                                liked = aniListApi.toggleActivityLike(activityId),
                                pending = false,
                            )
                        )
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error toggling activity like", e)
                        statusController.onUpdate(
                            statusUpdate.copy(liked = !update.liked, pending = false, error = e)
                        )
                    }
                }
                is ActivityToggleUpdate.Subscribe -> {
                    try {
                        statusController.onUpdate(
                            statusUpdate.copy(
                                subscribed = aniListApi.toggleActivitySubscribe(
                                    activityId,
                                    update.subscribed,
                                ),
                                pending = false,
                            )
                        )
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error toggling activity subscribe", e)
                        statusController.onUpdate(
                            statusUpdate.copy(
                                subscribed = !update.subscribed,
                                pending = false,
                                error = e
                            )
                        )
                    }
                }
            }

            jobs.remove(activityId)
        }
    }
}
