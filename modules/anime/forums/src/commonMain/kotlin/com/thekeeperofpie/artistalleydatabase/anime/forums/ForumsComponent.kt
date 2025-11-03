package com.thekeeperofpie.artistalleydatabase.anime.forums

import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.ForumThreadViewModel
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.comment.ForumCommentEntry
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.comment.ForumThreadCommentTreeViewModel
import dev.zacsweers.metro.Provider

interface ForumsComponent {
    val animeMediaDetailsForumThreadsViewModelFactory: AnimeMediaDetailsForumThreadsViewModel.Factory
    val forumRootScreenViewModel: Provider<ForumRootScreenViewModel>
    val forumSubsectionSortFilterViewModelFactory: ForumSubsectionSortFilterViewModel.Factory
    val forumSearchViewModelFactory: ForumSearchViewModel.Factory
    val forumThreadCommentTreeViewModelFactoryFactory: ForumThreadCommentTreeViewModel.TypedFactory.Factory
    val forumThreadViewModelFactory: ForumThreadViewModel.TypedFactory.Factory

    val forumCommentEntryProvider: ForumCommentEntry.Provider
}
