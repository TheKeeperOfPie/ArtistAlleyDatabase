package com.thekeeperofpie.artistalleydatabase.anime.forum.thread

import co.touchlab.kermit.Logger
import co.touchlab.stately.collections.ConcurrentMutableMap
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

class ForumThreadToggleHelper(
    private val aniListApi: AuthedAniListApi,
    private val statusController: ForumThreadStatusController,
    private val scope: CoroutineScope,
) {
    companion object {
        private const val TAG = "ForumThreadToggleHelper"
    }

    private val jobs = ConcurrentMutableMap<String, Job>()

    fun toggle(update: ForumThreadToggleUpdate) {
        val threadId = update.id
        val statusUpdate = ForumThreadStatusController.Update(
            threadId = threadId,
            liked = update.liked,
            update.subscribed,
            pending = true
        )
        statusController.onUpdate(statusUpdate)
        val job = jobs[threadId]
        jobs[threadId] = scope.launch(CustomDispatchers.IO) {
            job?.cancelAndJoin()
            when (update) {
                is ForumThreadToggleUpdate.Liked -> {
                    try {
                        statusController.onUpdate(
                            statusUpdate.copy(
                                liked = aniListApi.toggleForumThreadLike(threadId),
                                pending = false,
                            )
                        )
                    } catch (e: Throwable) {
                        Logger.e(TAG, e) { "Error toggling thread like" }
                        statusController.onUpdate(
                            statusUpdate.copy(liked = !update.liked, pending = false, error = e)
                        )
                    }
                }
                is ForumThreadToggleUpdate.Subscribe -> {
                    try {
                        statusController.onUpdate(
                            statusUpdate.copy(
                                subscribed = aniListApi.toggleForumThreadSubscribe(
                                    threadId,
                                    update.subscribed,
                                ),
                                pending = false,
                            )
                        )
                    } catch (e: Throwable) {
                        Logger.e(TAG, e) { "Error toggling thread subscribe" }
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

            jobs.remove(threadId)
        }
    }
}
