package com.thekeeperofpie.artistalleydatabase.anime.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import com.anilist.data.NotificationMediaAndActivityQuery
import com.anilist.data.fragment.ForumThread
import com.anilist.data.fragment.ForumThreadComment
import com.anilist.data.fragment.MediaCompactWithTags
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityToggleUpdate
import com.thekeeperofpie.artistalleydatabase.anime.forums.data.ForumCommentEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsByIdRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.ui.ActivityDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.ForumThreadCommentRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.ForumThreadRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted

object NotificationDestinations {

    @Serializable
    data object Notifications : NavDestination


    fun <MediaEntry, ForumCommentEntry> addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: NotificationsComponent,
        activityDetailsRoute: ActivityDetailsRoute,
        forumThreadRoute: ForumThreadRoute,
        forumThreadCommentRoute: ForumThreadCommentRoute,
        mediaDetailsByIdRoute: MediaDetailsByIdRoute,
        userRoute: UserRoute,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
        @Assisted forumCommentEntryProvider: ForumCommentEntryProvider<ForumThreadComment, ForumCommentEntry>,
        activityRow: @Composable (
            AniListViewer?,
            ActivityStatusAware,
            NotificationMediaAndActivityQuery.Data.Activity.Activity,
            MediaEntry?,
            onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        mediaRow: @Composable (
            AniListViewer?,
            MediaEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
        threadRow: @Composable (ForumThread?, Modifier) -> Unit,
        commentRow: @Composable (
            AniListViewer?,
            threadId: String,
            ForumCommentEntry,
            onStatusUpdate: (String, Boolean) -> Unit,
        ) -> Unit,
    ) {
        navGraphBuilder.sharedElementComposable<Notifications>(
            navigationTypeMap
        ) {
            val viewModel = viewModel {
                component.notificationsViewModelFactory()
                    .create(mediaEntryProvider, forumCommentEntryProvider)
            }
            val viewer by viewModel.viewer.collectAsState()
            NotificationsScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                viewer = { viewer },
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                content = viewModel.content.collectAsLazyPagingItems(),
                activityDetailsRoute = activityDetailsRoute,
                activityRow = { activityEntry, activity, mediaEntry, onClickListEdit ->
                    activityRow(
                        viewer,
                        activityEntry,
                        activity,
                        mediaEntry,
                        viewModel.activityToggleHelper::toggle,
                        onClickListEdit
                    )
                },
                mediaDetailsByIdRoute = mediaDetailsByIdRoute,
                forumThreadRoute = forumThreadRoute,
                forumThreadCommentRoute = forumThreadCommentRoute,
                threadRow = threadRow,
                commentId = forumCommentEntryProvider::id,
                commentRow = { threadId, commentEntry ->
                    commentRow(
                        viewer,
                        threadId,
                        commentEntry,
                        viewModel.commentToggleHelper::toggleLike,
                    )
                },
                mediaRow = mediaRow,
                userRoute = userRoute,
            )
        }
    }
}
