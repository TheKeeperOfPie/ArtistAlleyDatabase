package com.thekeeperofpie.artistalleydatabase.anime.forum

import com.anilist.ForumThread_CommentsQuery
import com.anilist.fragment.ForumThreadComment
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadCommentStatusController
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment.ForumCommentChild
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf

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

    fun decodeChild(markdown: Markdown, value: Any): ForumCommentChild? {
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
            val commentMarkdown = comment?.let(markdown::convertMarkdownText)

            val childComments = (map["childComments"] as? List<*>)
                ?.filterNotNull()
                ?.mapNotNull { decodeChild(markdown, it) }
                .orEmpty()

            return ForumCommentChild(
                id = id.toString(),
                createdAt = createdAt,
                likeCount = likeCount,
                user = user,
                commentRaw = comment,
                commentMarkdown = commentMarkdown,
                liked = isLiked,
                childComments = childComments,
            )
        } catch (ignored: Throwable) {
            return null
        }
    }
}

fun ForumThread_CommentsQuery.Data.Page.ThreadComment.toForumThreadComment() = let {
    object : ForumThreadComment {
        override val id = it.id
        override val comment = it.comment
        override val isLocked = it.isLocked
        override val likeCount = it.likeCount
        override val isLiked = it.isLiked
        override val createdAt = it.createdAt
        override val updatedAt = it.updatedAt
        override val user = it.user?.let {
            object : ForumThreadComment.User {
                override val id = it.id
                override val avatar = it.avatar?.let {
                    object : ForumThreadComment.User.Avatar {
                        override val large = it.large
                    }
                }
                override val name = it.name
            }
        }
    }
}
