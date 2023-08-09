package com.thekeeperofpie.artistalleydatabase.anime.forum.thread

import android.util.Log
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import java.util.Collections

class ForumThreadCommentToggleHelper(
    private val aniListApi: AuthedAniListApi,
    private val statusController: ForumThreadCommentStatusController,
    private val scope: CoroutineScope,
) {
    companion object {
        private const val TAG = "ForumThreadToggleHelper"
    }

    private val jobs = Collections.synchronizedMap(mutableMapOf<String, Job>())

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
                Log.e(TAG, "Error toggling comment like", e)
                statusController.onUpdate(
                    statusUpdate.copy(liked = !liked, pending = false, error = e)
                )
            }

            jobs.remove(commentId)
        }
    }
}
