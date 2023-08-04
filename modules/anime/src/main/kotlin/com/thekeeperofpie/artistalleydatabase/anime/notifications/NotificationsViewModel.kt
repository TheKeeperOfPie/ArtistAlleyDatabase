package com.thekeeperofpie.artistalleydatabase.anime.notifications

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.anilist.NotificationMediaAndActivityQuery
import com.anilist.NotificationsQuery
import com.anilist.fragment.ActivityItem.Companion.asListActivity
import com.anilist.fragment.MediaCompactWithTags
import com.anilist.fragment.MediaWithListStatus
import com.anilist.type.MediaListStatus
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.activity.applyActivityFiltering
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val ignoreList: AnimeMediaIgnoreList,
) : ViewModel() {

    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()
    val viewer = aniListApi.authedUser

    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    val content = MutableStateFlow(PagingData.empty<NotificationEntry>())

    private val refresh = MutableStateFlow(-1L)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                settings.showAdult,
                refresh,
                ::Pair,
            ).flatMapLatest { (showAdult) ->
                Pager(config = PagingConfig(10)) {
                    AniListPagingSource {
                        val result =
                            aniListApi.notifications(it, resetNotificationCount = it == 1)

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
                                mediaEntry = media?.let { NotificationEntry.MediaEntry(it, it) }
                                    ?: activity?.asListActivity()?.media
                                        ?.let { NotificationEntry.MediaEntry(it, it) },
                            )
                        }
                    }
                }.flow
                    .map { it.filter { showAdult || it.mediaEntry?.mediaCompactWithTags?.isAdult != true } }
            }
                .enforceUniqueIds { it.notificationId.scopedId }
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    combine(
                        mediaListStatusController.allChanges(),
                        activityStatusController.allChanges(),
                        ignoreList.updates,
                        settings.showAdult,
                        settings.showIgnored,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { mediaListStatuses, activityStatuses, ignoredIds, showAdult, showIgnored, showLessImportantTags, showSpoilerTags ->
                        pagingData.mapNotNull {
                            applyActivityFiltering(
                                mediaListStatuses = mediaListStatuses,
                                activityStatuses = activityStatuses,
                                ignoredIds = ignoredIds,
                                showAdult = showAdult,
                                showIgnored = showIgnored,
                                showLessImportantTags = showLessImportantTags,
                                showSpoilerTags = showSpoilerTags,
                                entry = it,
                                activityId = it.activityEntry?.id,
                                activityStatusAware = it.activityEntry,
                                media = it.mediaEntry?.mediaWithListStatus,
                                mediaStatusAware = it.mediaEntry,
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
                                        activityEntry = it.activityEntry?.copy(
                                            liked = liked,
                                            subscribed = subscribed
                                        )
                                    )
                                }
                            )
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
        val mediaEntry: MediaEntry?,
    ) {
        data class ActivityEntry(
            val id: String,
            val activity: NotificationMediaAndActivityQuery.Data.Activity.Activity,
            override val liked: Boolean,
            override val subscribed: Boolean,
        ) : ActivityStatusAware

        data class MediaEntry(
            val mediaCompactWithTags: MediaCompactWithTags,
            val mediaWithListStatus: MediaWithListStatus,
            val id: String = mediaCompactWithTags.id.toString(),
            override val mediaListStatus: MediaListStatus? = mediaWithListStatus.mediaListEntry?.status,
            override val progress: Int? = mediaWithListStatus.mediaListEntry?.progress,
            override val progressVolumes: Int? = mediaWithListStatus.mediaListEntry?.progressVolumes,
            override val ignored: Boolean = false,
            override val showLessImportantTags: Boolean = false,
            override val showSpoilerTags: Boolean = false,
        ) : MediaStatusAware {
            val rowEntry = AnimeMediaCompactListRow.Entry(
                media = mediaCompactWithTags,
                ignored = ignored,
                showLessImportantTags = showLessImportantTags,
                showSpoilerTags = showSpoilerTags,
            )
        }
    }
}
