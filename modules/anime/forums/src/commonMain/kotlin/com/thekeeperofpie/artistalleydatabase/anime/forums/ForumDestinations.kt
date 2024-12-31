package com.thekeeperofpie.artistalleydatabase.anime.forums

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import artistalleydatabase.modules.anime.forums.generated.resources.Res
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_root_active_title
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_root_new_title
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_root_releases_title
import com.anilist.data.fragment.MediaCompactWithTags
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.ForumThreadScreen
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.comment.ForumThreadCommentTreeScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.ui.ForumThreadCommentRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.ForumThreadRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.createSavedStateHandle
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

object ForumDestinations {

    @Serializable
    data object Forum : NavDestination

    @Serializable
    data class ForumSearch(
        val title: Title? = null,
        val sort: ForumThreadSortOption? = null,
        val categoryId: String? = null,
        val mediaCategoryId: String? = null,
    ) : NavDestination {
        @Serializable
        sealed interface Title {
            @Composable
            fun text(): String

            @Serializable
            data object Active : Title {
                @Composable
                override fun text() = stringResource(Res.string.anime_forum_root_active_title)
            }

            @Serializable
            data object New : Title {
                @Composable
                override fun text() = stringResource(Res.string.anime_forum_root_new_title)
            }

            @Serializable
            data object Releases : Title {
                @Composable
                override fun text() = stringResource(Res.string.anime_forum_root_releases_title)
            }

            @Serializable
            data class Custom(val title: String) : Title {
                @Composable
                override fun text() = title
            }
        }
    }

    @Serializable
    data class ForumThread(
        val threadId: String,
        val title: String? = null,
    ) : NavDestination {
        companion object {
            val route: ForumThreadRoute = { threadId, title ->
                ForumThread(threadId = threadId, title = title)
            }
        }
    }

    @Serializable
    data class ForumThreadComment(
        val threadId: String,
        val commentId: String,
        val title: String? = null,
    ) : NavDestination {
        companion object {
            val route: ForumThreadCommentRoute = { threadId, commentId, title ->
                ForumThreadComment(
                    threadId = threadId,
                    commentId = commentId,
                    title = title,
                )
            }
        }
    }

    fun <MediaEntry> addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: ForumsComponent,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        mediaDetailsRoute: MediaDetailsRoute,
        userRoute: UserRoute,
        mediaRow: @Composable (
            MediaEntry?,
            AniListViewer?,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
        mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
    ) {
        navGraphBuilder.sharedElementComposable<Forum>(navigationTypeMap) {
            val viewModel = viewModel { component.forumRootScreenViewModel() }
            ForumRootScreen(
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                onRefresh = viewModel::refresh,
                entry = viewModel.entry,
                userRoute = userRoute,
            )
        }

        navGraphBuilder.sharedElementComposable<ForumSearch>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern =
                        "${AniListUtils.ANILIST_BASE_URL}/forum/recent?category={categoryId}"
                },
                navDeepLink {
                    uriPattern =
                        "${AniListUtils.ANILIST_BASE_URL}/forum/recent?media={mediaCategoryId}"
                },
            ),
        ) {
            val destination = it.toRoute<ForumSearch>()
            val forumSubsectionSortFilterViewModel = viewModel {
                component.forumSubsectionSortFilterViewModel(
                    createSavedStateHandle("forumSubsectionSortFilter"),
                    mediaDetailsRoute, ForumSubsectionSortFilterViewModel.InitialParams(
                        defaultSort = destination.sort,
                        categoryId = destination.categoryId,
                        mediaCategoryId = destination.mediaCategoryId,
                    )
                )
            }
            val viewModel = viewModel {
                component.forumSearchViewModel(
                    createSavedStateHandle(),
                    forumSubsectionSortFilterViewModel,
                )
            }
            ForumSearchScreen(
                sortFilterState = forumSubsectionSortFilterViewModel.state,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                title = destination.title,
                query = { viewModel.query },
                onQueryChanged = { viewModel.query = it },
                content = viewModel.content,
                userRoute = userRoute,
            )
        }


        // TODO: Forum deep links
        navGraphBuilder.sharedElementComposable<ForumDestinations.ForumThread>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/forum/thread/{threadId}"
                },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/forum/thread/{threadId}/.*"
                },
            ),
        ) {
            val viewModel = viewModel {
                component.forumThreadViewModel(createSavedStateHandle())
                    .create(mediaEntryProvider)
            }
            val viewer by viewModel.viewer.collectAsState()
            ForumThreadScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                viewer = { viewer },
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                refresh = viewModel.refresh,
                onRefresh = viewModel::refresh,
                threadId = viewModel.threadId,
                title = it.arguments?.getString("title"),
                state = viewModel.state,
                comments = viewModel.comments,
                userRoute = userRoute,
                onSendReply = viewModel::sendReply,
                onClickReplyComment = { commentId, commentMarkdown ->
                    viewModel.onClickReplyComment(
                        commentId,
                        commentMarkdown,
                    )
                },
                onConfirmDelete = viewModel::deleteComment,
                onStatusUpdate = viewModel.threadToggleHelper::toggle,
                onCommentLikeStatusUpdate = viewModel.commentToggleHelper::toggleLike,
                mediaItemKey = mediaEntryProvider::id,
                mediaRow = { entry, onClickListEdit, modifier ->
                    mediaRow(entry, viewer, onClickListEdit, modifier)
                },
            )
        }

        navGraphBuilder.sharedElementComposable<ForumDestinations.ForumThreadComment>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern =
                        "${AniListUtils.ANILIST_BASE_URL}/forum/thread/{threadId}/comment/{commentId}"
                },
                navDeepLink {
                    uriPattern =
                        "${AniListUtils.ANILIST_BASE_URL}/forum/thread/{threadId}/comment/{commentId}/.*"
                },
            ),
        ) {
            val viewModel = viewModel {
                component.forumThreadCommentTreeViewModelFactory(createSavedStateHandle())
                    .create(mediaEntryProvider)
            }
            val viewer by viewModel.viewer.collectAsState()
            ForumThreadCommentTreeScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                viewer = { viewer },
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                refresh = viewModel.refresh,
                onRefresh = viewModel::refresh,
                threadId = viewModel.threadId,
                title = it.arguments?.getString("title"),
                state = viewModel.state,
                comments = { viewModel.comments },
                userRoute = userRoute,
                onSendReply = viewModel::sendReply,
                onClickReplyComment = { commentId, commentMarkdown ->
                    viewModel.onClickReplyComment(
                        commentId,
                        commentMarkdown,
                    )
                },
                onConfirmDelete = viewModel::deleteComment,
                onStatusUpdate = viewModel.threadToggleHelper::toggle,
                onCommentLikeStatusUpdate = viewModel.commentToggleHelper::toggleLike,
                mediaItemKey = mediaEntryProvider::id,
                mediaRow = { entry, onClickListEdit, modifier ->
                    mediaRow(entry, viewer, onClickListEdit, modifier)
                },
            )
        }
    }
}
