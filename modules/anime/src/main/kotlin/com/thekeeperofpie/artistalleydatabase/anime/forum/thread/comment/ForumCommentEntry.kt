package com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment

import com.anilist.fragment.ForumThreadComment
import com.anilist.fragment.UserNavigationData
import com.thekeeperofpie.artistalleydatabase.compose.StableSpanned

data class ForumCommentEntry(
    val comment: ForumThreadComment,
    val commentMarkdown: StableSpanned?,
    val liked: Boolean = comment.isLiked ?: false,
    val children: List<ForumCommentChild>,
    val user: UserNavigationData? = comment.user?.let {
        ForumCommentChild.User(
            id = it.id,
            avatar = it.avatar?.large?.let(ForumCommentChild.User::Avatar),
            name = it.name,
        )
    },
)
