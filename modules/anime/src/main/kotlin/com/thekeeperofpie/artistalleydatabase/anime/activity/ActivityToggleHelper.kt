package com.thekeeperofpie.artistalleydatabase.anime.activity

import co.touchlab.kermit.Logger
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
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
                        Logger.e(TAG, e) { "Error toggling activity like" }
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
                        Logger.e(TAG, e) { "Error toggling activity subscribe" }
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
