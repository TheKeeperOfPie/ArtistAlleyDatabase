package com.thekeeperofpie.artistalleydatabase.anime.forum.thread

import com.anilist.fragment.ForumThread
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText

data class ForumThreadEntry(
    val thread: ForumThread,
    val bodyMarkdown: MarkdownText?,
    val liked: Boolean = thread.isLiked ?: false,
    val subscribed: Boolean = thread.isSubscribed ?: false,
)
