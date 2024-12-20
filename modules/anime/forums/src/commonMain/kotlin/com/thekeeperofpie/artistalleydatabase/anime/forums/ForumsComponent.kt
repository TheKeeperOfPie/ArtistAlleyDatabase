package com.thekeeperofpie.artistalleydatabase.anime.forums

import androidx.lifecycle.SavedStateHandle
import com.anilist.data.MediaDetailsQuery
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.ForumThreadViewModel
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.comment.ForumCommentEntry
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.comment.ForumThreadCommentTreeViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import kotlinx.coroutines.flow.Flow

interface ForumsComponent {
    val animeMediaDetailsForumThreadsViewModel: (Flow<MediaDetailsQuery.Data.Media?>) -> AnimeMediaDetailsForumThreadsViewModel
    val forumRootScreenViewModel: () -> ForumRootScreenViewModel
    val forumSearchViewModel: (SavedStateHandle, MediaDetailsRoute) -> ForumSearchViewModel
    val forumThreadCommentTreeViewModelFactory: (SavedStateHandle) -> ForumThreadCommentTreeViewModel.Factory
    val forumThreadViewModel: (SavedStateHandle) -> ForumThreadViewModel.Factory

    val forumCommentEntryProvider: ForumCommentEntry.Provider
}
