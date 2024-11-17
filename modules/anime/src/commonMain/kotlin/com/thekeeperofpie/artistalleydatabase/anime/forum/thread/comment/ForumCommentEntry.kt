package com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment

import com.anilist.data.fragment.ForumThreadComment
import com.anilist.data.fragment.UserNavigationData
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText

data class ForumCommentEntry(
    val comment: ForumThreadComment,
    val commentMarkdown: MarkdownText?,
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
