package com.thekeeperofpie.artistalleydatabase.anime.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.data.NotificationMediaAndActivityQuery
import com.anilist.data.fragment.ActivityItem.Companion.asListActivity
import com.anilist.data.fragment.ForumThreadComment
import com.anilist.data.fragment.MediaCompactWithTags
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.applyActivityFiltering
import com.thekeeperofpie.artistalleydatabase.anime.forums.data.ForumCommentEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.forums.data.ForumThreadCommentStatusController
import com.thekeeperofpie.artistalleydatabase.anime.forums.data.ForumThreadCommentToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.filterOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class NotificationsViewModel<MediaEntry, ForumCommentEntry>(
    private val aniListApi: AuthedAniListApi,
    settings: MediaDataSettings,
    private val activityStatusController: ActivityStatusController,
    private val mediaListStatusController: MediaListStatusController,
    commentStatusController: ForumThreadCommentStatusController,
    private val ignoreController: IgnoreController,
    notificationsController: NotificationsController,
    @Assisted mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
    @Assisted forumCommentEntryProvider: ForumCommentEntryProvider<ForumThreadComment, ForumCommentEntry>,
) : ViewModel() {

    val viewer = aniListApi.authedUser

    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    val commentToggleHelper =
        ForumThreadCommentToggleHelper(aniListApi, commentStatusController, viewModelScope)

    val content = MutableStateFlow(PagingData.empty<NotificationEntry<MediaEntry, ForumCommentEntry>>())

    private val refresh = RefreshFlow()

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                settings.showAdult,
                refresh.updates,
                ::Pair,
            ).flatMapLatest { (showAdult) ->
                AniListPager {
                    val result =
                        aniListApi.notifications(it, resetNotificationCount = it == 1)

                    if (it == 1) {
                        notificationsController.clear()
                    }

                    val notifications = result.page?.notifications?.filterNotNull().orEmpty()
                    val mediaToFetch = mutableListOf<String>()
                    val activityToFetch = mutableListOf<String>()
                    notifications.forEach {
                        it.mediaId?.let { mediaToFetch += it }
                        it.activityId?.let { activityToFetch += it }
                    }

                    val mediaAndActivity = aniListApi.notificationMediaAndActivity(
                        mediaIds = mediaToFetch,
                        activityIds = activityToFetch,
                    )

                    val mediasById = mediaAndActivity.media?.media
                        ?.associateBy { it?.id?.toString() }
                        .orEmpty()

                    val activityById = mediaAndActivity.activity?.activities
                        ?.associateBy {
                            when (it) {
                                is NotificationMediaAndActivityQuery.Data.Activity.ListActivityActivity -> it.id.toString()
                                is NotificationMediaAndActivityQuery.Data.Activity.MessageActivityActivity -> it.id.toString()
                                is NotificationMediaAndActivityQuery.Data.Activity.TextActivityActivity -> it.id.toString()
                                is NotificationMediaAndActivityQuery.Data.Activity.OtherActivity,
                                null,
                                    -> null
                            }
                        }
                        .orEmpty()

                    result.page?.pageInfo to notifications.map {
                        val activity = activityById[it.activityId]
                        val media = mediasById[it.mediaId]
                        NotificationEntry(
                            notificationId = EntryId(it.__typename, it.id.toString()),
                            notification = it,
                            activityEntry = activity?.let {
                                NotificationActivityEntry(
                                    id = when (activity) {
                                        is NotificationMediaAndActivityQuery.Data.Activity.ListActivityActivity -> activity.id.toString()
                                        is NotificationMediaAndActivityQuery.Data.Activity.MessageActivityActivity -> activity.id.toString()
                                        is NotificationMediaAndActivityQuery.Data.Activity.TextActivityActivity -> activity.id.toString()
                                        is NotificationMediaAndActivityQuery.Data.Activity.OtherActivity -> ""
                                    },
                                    activity = activity,
                                    liked = when (activity) {
                                        is NotificationMediaAndActivityQuery.Data.Activity.ListActivityActivity -> activity.isLiked
                                        is NotificationMediaAndActivityQuery.Data.Activity.MessageActivityActivity -> activity.isLiked
                                        is NotificationMediaAndActivityQuery.Data.Activity.TextActivityActivity -> activity.isLiked
                                        is NotificationMediaAndActivityQuery.Data.Activity.OtherActivity -> false
                                    } ?: false,
                                    subscribed = when (activity) {
                                        is NotificationMediaAndActivityQuery.Data.Activity.ListActivityActivity -> activity.isSubscribed
                                        is NotificationMediaAndActivityQuery.Data.Activity.MessageActivityActivity -> activity.isSubscribed
                                        is NotificationMediaAndActivityQuery.Data.Activity.TextActivityActivity -> activity.isSubscribed
                                        is NotificationMediaAndActivityQuery.Data.Activity.OtherActivity -> false
                                    } ?: false,
                                )
                            },
                            mediaEntry = media?.let(mediaEntryProvider::mediaEntry)
                                ?: activity?.asListActivity()?.media
                                    ?.let(mediaEntryProvider::mediaEntry),
                            commentEntry = it.comment?.let {
                                forumCommentEntryProvider.commentEntry(it)
                            }
                        )
                    }
                }
                    .map {
                        it.filterOnIO {
                            showAdult || it.mediaEntry
                                ?.let(mediaEntryProvider::mediaFilterable)?.isAdult != true
                        }
                    }
            }
                .enforceUniqueIds { it.notificationId.scopedId }
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    combine(
                        mediaListStatusController.allChanges(),
                        activityStatusController.allChanges(),
                        commentStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { mediaListStatuses, activityStatuses, commentUpdates, _, filteringData ->
                        pagingData.mapNotNull { entry ->
                            applyActivityFiltering(
                                mediaListStatuses = mediaListStatuses,
                                activityStatuses = activityStatuses,
                                ignoreController = ignoreController,
                                filteringData = filteringData,
                                entry = entry,
                                activityId = entry.activityEntry?.id,
                                activityStatusAware = entry.activityEntry,
                                mediaFilterable = entry.mediaEntry?.let(mediaEntryProvider::mediaFilterable),
                                copyMedia = { mediaFilterable ->
                                    copy(
                                        mediaEntry = mediaEntry?.let {
                                            mediaEntryProvider
                                                .copyMediaEntry(it, mediaFilterable)
                                        }
                                    )
                                },
                                copyActivity = { liked, subscribed ->
                                    copy(
                                        activityEntry = activityEntry?.copy(
                                            liked = liked,
                                            subscribed = subscribed
                                        )
                                    )
                                }
                            )?.let {
                                val commentEntry = it.commentEntry
                                val commentId = commentEntry?.let(forumCommentEntryProvider::id)
                                    ?: return@let it
                                val liked = commentUpdates[commentId]?.liked
                                    ?: forumCommentEntryProvider.liked(commentEntry)
                                it.copy(
                                    commentEntry = forumCommentEntryProvider
                                        .copyCommentEntry(
                                            entry = commentEntry,
                                            liked = liked == true,
                                        )
                                )
                            }
                        }
                    }
                }
                .cachedIn(viewModelScope)
                .collectLatest(content::emit)
        }
    }

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val settings: MediaDataSettings,
        private val activityStatusController: ActivityStatusController,
        private val mediaListStatusController: MediaListStatusController,
        private val commentStatusController: ForumThreadCommentStatusController,
        private val ignoreController: IgnoreController,
        private val notificationsController: NotificationsController,
    ) {
        fun <MediaEntry, ForumCommentEntry> create(
            @Assisted mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
            @Assisted forumCommentEntryProvider: ForumCommentEntryProvider<ForumThreadComment, ForumCommentEntry>,
        ) = NotificationsViewModel(
            aniListApi = aniListApi,
            settings = settings,
            activityStatusController = activityStatusController,
            mediaListStatusController = mediaListStatusController,
            commentStatusController = commentStatusController,
            ignoreController = ignoreController,
            notificationsController = notificationsController,
            mediaEntryProvider = mediaEntryProvider,
            forumCommentEntryProvider = forumCommentEntryProvider,
        )
    }
}
