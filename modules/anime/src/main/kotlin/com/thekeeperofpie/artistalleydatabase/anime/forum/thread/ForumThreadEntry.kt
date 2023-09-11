package com.thekeeperofpie.artistalleydatabase.anime.forum.thread

import android.text.Spanned
import com.anilist.fragment.ForumThread
import com.thekeeperofpie.artistalleydatabase.compose.StableSpanned

data class ForumThreadEntry(
    val thread: ForumThread,
    val bodyMarkdown: StableSpanned?,
    val liked: Boolean = thread.isLiked ?: false,
    val subscribed: Boolean = thread.isSubscribed ?: false,
)
