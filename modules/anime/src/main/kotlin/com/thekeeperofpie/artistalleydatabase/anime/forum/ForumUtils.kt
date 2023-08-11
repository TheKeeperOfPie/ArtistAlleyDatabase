package com.thekeeperofpie.artistalleydatabase.anime.forum

import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment.ForumCommentChild
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadCommentStatusController
import io.noties.markwon.Markwon

object ForumUtils {

    // TODO: This way of copying is incredibly messy and expensive
    fun copyUpdatedChild(
        child: ForumCommentChild,
        updates: Map<String, ForumThreadCommentStatusController.Update>,
    ): ForumCommentChild {
        val needsCopy = needsUpdate(updates, child)
        return child.transformIf(needsCopy) {
            val liked = updates[child.id]?.liked ?: child.liked
            copy(
                liked = liked,
                childComments = child.childComments.map { copyUpdatedChild(it, updates) },
            )
        }
    }

    private fun needsUpdate(
        updates: Map<String, ForumThreadCommentStatusController.Update>,
        child: ForumCommentChild,
    ): Boolean {
        val update = updates[child.id]
        if (update != null && update.liked != child.liked) return true
        return child.childComments.any { needsUpdate(updates, it) }
    }

    fun decodeChild(markwon: Markwon, value: Any): ForumCommentChild? {
        try {
            val map = (value as? Map<*, *>) ?: return null
            val id = map["id"] as? Int ?: return null
            val userMap = map["user"] as? Map<*, *> ?: return null
            val userId = userMap["id"] as? Int ?: return null
            val userName = userMap["name"] as? String ?: return null
            val userAvatarLarge = (userMap["avatar"] as? Map<*, *>)?.get("large") as? String
            val user = ForumCommentChild.User(
                id = userId,
                avatar = userAvatarLarge?.let(ForumCommentChild.User::Avatar),
                name = userName,
            )

            val createdAt = map["createdAt"] as? Int
            val likeCount = map["likeCount"] as? Int
            val comment = map["comment"] as? String
            val isLiked = map["isLiked"] as? Boolean ?: false
            val commentMarkdown = comment
                ?.let(markwon::parse)
                ?.let(markwon::render)

            val childComments = (map["childComments"] as? List<*>)
                ?.filterNotNull()
                ?.mapNotNull { decodeChild(markwon, it) }
                .orEmpty()

            return ForumCommentChild(
                id = id.toString(),
                createdAt = createdAt,
                likeCount = likeCount,
                user = user,
                commentMarkdown = commentMarkdown,
                liked = isLiked,
                childComments = childComments,
            )
        } catch (ignored: Throwable) {
            return null
        }
    }
}
