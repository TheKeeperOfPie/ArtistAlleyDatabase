package com.thekeeperofpie.artistalleydatabase.anime.forums.thread.comment

import com.anilist.data.fragment.UserNavigationData
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText

data class ForumCommentChild(
    val id: String,
    val createdAt: Int? = null,
    val likeCount: Int? = null,
    val user: User,
    val commentRaw: String? = null,
    val commentMarkdown: MarkdownText? = null,
    val liked: Boolean = false,
    val childComments: List<ForumCommentChild>,
) {

    data class User(
        override val id: Int,
        override val avatar: UserNavigationData.Avatar?,
        override val name: String,
    ) : UserNavigationData {

        data class Avatar(
            override val large: String?,
        ) : UserNavigationData.Avatar
    }
}
