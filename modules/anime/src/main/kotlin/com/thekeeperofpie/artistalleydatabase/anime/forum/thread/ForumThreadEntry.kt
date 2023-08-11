package com.thekeeperofpie.artistalleydatabase.anime.forum.thread

import android.text.Spanned
import com.anilist.fragment.ForumThread

data class ForumThreadEntry(
    val thread: ForumThread,
    val bodyMarkdown: Spanned?,
    val liked: Boolean,
    val subscribed: Boolean,
)
