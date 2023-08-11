package com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment

import android.text.Spanned
import com.anilist.fragment.UserNavigationData

data class ForumCommentChild(
    val id: String,
    val createdAt: Int? = null,
    val likeCount: Int? = null,
    val user: User,
    val commentMarkdown: Spanned? = null,
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
