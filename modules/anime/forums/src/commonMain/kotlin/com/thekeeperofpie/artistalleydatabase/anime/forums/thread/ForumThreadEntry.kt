package com.thekeeperofpie.artistalleydatabase.anime.forums.thread

import com.anilist.data.fragment.ForumThread
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText

data class ForumThreadEntry(
    val thread: ForumThread,
    val bodyMarkdown: MarkdownText?,
    val liked: Boolean = thread.isLiked == true,
    val subscribed: Boolean = thread.isSubscribed == true,
)
