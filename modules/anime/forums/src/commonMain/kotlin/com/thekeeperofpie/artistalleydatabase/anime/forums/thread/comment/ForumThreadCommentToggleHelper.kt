package com.thekeeperofpie.artistalleydatabase.anime.forums.thread.comment

import co.touchlab.kermit.Logger
import co.touchlab.stately.collections.ConcurrentMutableMap
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

class ForumThreadCommentToggleHelper(
    private val aniListApi: AuthedAniListApi,
    private val statusController: ForumThreadCommentStatusController,
    private val scope: CoroutineScope,
) {
    companion object {
        private const val TAG = "ForumThreadToggleHelper"
    }

    private val jobs = ConcurrentMutableMap<String, Job>()

    fun toggleLike(commentId: String, liked: Boolean) {
        val statusUpdate = ForumThreadCommentStatusController.Update(
            commentId = commentId,
            liked = liked,
            pending = true,
        )
        statusController.onUpdate(statusUpdate)
        val job = jobs[commentId]
        jobs[commentId] = scope.launch(CustomDispatchers.IO) {
            job?.cancelAndJoin()
            try {
                statusController.onUpdate(
                    statusUpdate.copy(
                        liked = aniListApi.toggleForumThreadCommentLike(commentId),
                        pending = false,
                    )
                )
            } catch (e: Throwable) {
                Logger.e(TAG, e) { "Error toggling comment like" }
                statusController.onUpdate(
                    statusUpdate.copy(liked = !liked, pending = false, error = e)
                )
            }

            jobs.remove(commentId)
        }
    }
}
