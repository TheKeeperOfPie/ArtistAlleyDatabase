package com.thekeeperofpie.artistalleydatabase.anime.forums.thread.comment

import com.anilist.data.fragment.ForumThreadComment
import com.anilist.data.fragment.UserNavigationData
import com.thekeeperofpie.artistalleydatabase.anime.forums.data.ForumCommentEntryProvider
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import me.tatarka.inject.annotations.Inject

data class ForumCommentEntry(
    val comment: ForumThreadComment,
    val commentMarkdown: MarkdownText?,
    val liked: Boolean? = comment.isLiked,
    val children: List<ForumCommentChild>,
    val user: UserNavigationData? = comment.user?.let {
        ForumCommentChild.User(
            id = it.id,
            avatar = it.avatar?.large?.let(ForumCommentChild.User::Avatar),
            name = it.name,
        )
    },
) {

    @Inject
    class Provider(private val markdown: Markdown) :
        ForumCommentEntryProvider<ForumThreadComment, ForumCommentEntry> {
        override fun commentEntry(comment: ForumThreadComment) = ForumCommentEntry(
            comment = comment,
            commentMarkdown = comment.comment?.let(markdown::convertMarkdownText),
            children = emptyList(),
        )

        override fun copyCommentEntry(
            entry: ForumCommentEntry,
            liked: Boolean,
        ) = entry.copy(liked = liked)

        override fun id(entry: ForumCommentEntry) = entry.comment.id.toString()
        override fun liked(entry: ForumCommentEntry) = entry.liked
    }
}
