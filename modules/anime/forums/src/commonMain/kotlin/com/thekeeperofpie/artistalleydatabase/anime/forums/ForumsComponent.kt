package com.thekeeperofpie.artistalleydatabase.anime.forums

import androidx.lifecycle.SavedStateHandle
import com.anilist.data.MediaDetailsQuery
import com.thekeeperofpie.artistalleydatabase.anime.forums.ForumSubsectionSortFilterViewModel.InitialParams
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.ForumThreadViewModel
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.comment.ForumCommentEntry
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.comment.ForumThreadCommentTreeViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.ScopedSavedStateHandle
import kotlinx.coroutines.flow.Flow

interface ForumsComponent {
    val animeMediaDetailsForumThreadsViewModel: (Flow<MediaDetailsQuery.Data.Media?>) -> AnimeMediaDetailsForumThreadsViewModel
    val forumRootScreenViewModel: () -> ForumRootScreenViewModel
    val forumSubsectionSortFilterViewModel: (ScopedSavedStateHandle, MediaDetailsRoute, InitialParams) -> ForumSubsectionSortFilterViewModel
    val forumSearchViewModel: (SavedStateHandle, ForumSubsectionSortFilterViewModel) -> ForumSearchViewModel
    val forumThreadCommentTreeViewModelFactory: (SavedStateHandle) -> ForumThreadCommentTreeViewModel.Factory
    val forumThreadViewModel: (SavedStateHandle) -> ForumThreadViewModel.Factory

    val forumCommentEntryProvider: ForumCommentEntry.Provider
}
