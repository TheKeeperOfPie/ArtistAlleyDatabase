package com.thekeeperofpie.artistalleydatabase.anime.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.NotificationMediaAndActivityQuery
import com.anilist.NotificationsQuery
import com.anilist.fragment.ActivityItem.Companion.asListActivity
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.activity.applyActivityFiltering
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadCommentStatusController
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadCommentToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment.ForumCommentEntry
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.filterOnIO
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import com.thekeeperofpie.artistalleydatabase.anime.utils.toStableMarkdown
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import dagger.hilt.android.lifecycle.HiltViewModel
import io.noties.markwon.Markwon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    private val activityStatusController: ActivityStatusController,
    private val mediaListStatusController: MediaListStatusController,
    commentStatusController: ForumThreadCommentStatusController,
    private val ignoreController: IgnoreController,
    markwon: Markwon,
    notificationsController: NotificationsController,
) : ViewModel() {

    val viewer = aniListApi.authedUser

    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    val commentToggleHelper =
        ForumThreadCommentToggleHelper(aniListApi, commentStatusController, viewModelScope)

    val content = MutableStateFlow(PagingData.empty<NotificationEntry>())

    private val refresh = MutableStateFlow(-1L)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                settings.showAdult,
                refresh,
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
                                NotificationEntry.ActivityEntry(
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
                            mediaEntry = media?.let { MediaCompactWithTagsEntry(it) }
                                ?: activity?.asListActivity()?.media
                                    ?.let { MediaCompactWithTagsEntry(it) },
                            commentEntry = it.comment?.let {
                                ForumCommentEntry(
                                    comment = it,
                                    commentMarkdown = it.comment?.let(markwon::toStableMarkdown),
                                    children = emptyList(),
                                )
                            }
                        )
                    }
                }
                    .map { it.filterOnIO { showAdult || it.mediaEntry?.media?.isAdult != true } }
            }
                .enforceUniqueIds { it.notificationId.scopedId }
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    combine(
                        mediaListStatusController.allChanges(),
                        activityStatusController.allChanges(),
                        commentStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.showAdult,
                        settings.showIgnored,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { mediaListStatuses, activityStatuses, commentUpdates, _, showAdult, showIgnored, showLessImportantTags, showSpoilerTags ->
                        pagingData.mapNotNull { entry ->
                            applyActivityFiltering(
                                mediaListStatuses = mediaListStatuses,
                                activityStatuses = activityStatuses,
                                ignoreController = ignoreController,
                                showAdult = showAdult,
                                showIgnored = showIgnored,
                                showLessImportantTags = showLessImportantTags,
                                showSpoilerTags = showSpoilerTags,
                                entry = entry,
                                activityId = entry.activityEntry?.id,
                                activityStatusAware = entry.activityEntry,
                                media = entry.mediaEntry?.media,
                                mediaStatusAware = entry.mediaEntry,
                                copyMedia = { status, progress, progressVolumes, ignored, showLessImportantTags, showSpoilerTags ->
                                    copy(
                                        mediaEntry = mediaEntry?.copy(
                                            mediaListStatus = status,
                                            progress = progress,
                                            progressVolumes = progressVolumes,
                                            ignored = ignored,
                                            showLessImportantTags = showLessImportantTags,
                                            showSpoilerTags = showSpoilerTags,
                                        )
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
                                val commentId = it.commentEntry?.comment?.id?.toString()
                                    ?: return@let it
                                val liked = commentUpdates[commentId]?.liked
                                    ?: it.commentEntry.comment.isLiked
                                    ?: false
                                it.copy(
                                    commentEntry = it.commentEntry.copy(
                                        liked = liked,
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

    data class NotificationEntry(
        val notificationId: EntryId,
        val notification: NotificationsQuery.Data.Page.Notification,
        val activityEntry: ActivityEntry?,
        val mediaEntry: MediaCompactWithTagsEntry?,
        val commentEntry: ForumCommentEntry?,
    ) {
        data class ActivityEntry(
            val id: String,
            val activity: NotificationMediaAndActivityQuery.Data.Activity.Activity,
            override val liked: Boolean,
            override val subscribed: Boolean,
        ) : ActivityStatusAware
    }
}
