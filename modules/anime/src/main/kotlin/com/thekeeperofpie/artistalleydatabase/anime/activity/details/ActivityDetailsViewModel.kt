package com.thekeeperofpie.artistalleydatabase.anime.activity.details

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.anilist.ActivityDetailsQuery
import com.anilist.ActivityDetailsRepliesQuery
import com.anilist.type.MediaListStatus
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityReplyStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityReplyToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivityDetailsViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val statusController: ActivityStatusController,
    private val mediaListStatusController: MediaListStatusController,
    private val replyStatusController: ActivityReplyStatusController,
    private val ignoreList: AnimeMediaIgnoreList,
) : ViewModel() {

    lateinit var activityId: String

    val viewer = aniListApi.authedUser
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()
    val refresh = MutableStateFlow(-1L)
    var entry by mutableStateOf<LoadingResult<Entry>>(LoadingResult.loading())
    var replies = MutableStateFlow(PagingData.empty<Entry.ReplyEntry>())

    var replying by mutableStateOf(false)
    var deleting by mutableStateOf(false)

    val toggleHelper = ActivityToggleHelper(aniListApi, statusController, viewModelScope)
    val replyToggleHelper =
        ActivityReplyToggleHelper(aniListApi, replyStatusController, viewModelScope)

    fun initialize(id: String) {
        if (::activityId.isInitialized) return
        this.activityId = id

        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(refresh, R.string.anime_activity_details_error_loading) {
                flowFromSuspend {
                    aniListApi.activityDetails(activityId).activity
                }
            }
                .flatMapLatest { activity ->
                    combine(
                        statusController.allChanges(activityId),
                        if (activity.result is ActivityDetailsQuery.Data.ListActivityActivity) {
                            mediaListStatusController.allChanges(activityId)
                        } else {
                            flowOf(null)
                        },
                        ignoreList.updates,
                        ::Triple
                    )
                        .mapLatest { (activityUpdates, mediaListUpdate, ignoredIds) ->
                            activity.transformResult {
                                val liked = activityUpdates?.liked ?: when (it) {
                                    is ActivityDetailsQuery.Data.ListActivityActivity -> it.isLiked
                                    is ActivityDetailsQuery.Data.MessageActivityActivity -> it.isLiked
                                    is ActivityDetailsQuery.Data.TextActivityActivity -> it.isLiked
                                    is ActivityDetailsQuery.Data.OtherActivity -> false
                                } ?: false
                                val subscribed = activityUpdates?.subscribed ?: when (it) {
                                    is ActivityDetailsQuery.Data.ListActivityActivity -> it.isSubscribed
                                    is ActivityDetailsQuery.Data.MessageActivityActivity -> it.isSubscribed
                                    is ActivityDetailsQuery.Data.TextActivityActivity -> it.isSubscribed
                                    is ActivityDetailsQuery.Data.OtherActivity -> false
                                } ?: false
                                Entry(
                                    activity = it,
                                    liked = liked,
                                    subscribed = subscribed,
                                    mediaEntry = (it as? ActivityDetailsQuery.Data.ListActivityActivity)?.media?.let {
                                        if (mediaListUpdate == null) {
                                            Entry.MediaEntry(
                                                media = it,
                                                mediaListStatus = it.mediaListEntry?.status,
                                                progress = it.mediaListEntry?.progress,
                                                progressVolumes = it.mediaListEntry?.progressVolumes,
                                                ignored = ignoredIds.contains(it.id),
                                            )
                                        } else {
                                            Entry.MediaEntry(
                                                media = it,
                                                mediaListStatus = mediaListUpdate.entry?.status,
                                                progress = mediaListUpdate.entry?.progress,
                                                progressVolumes = mediaListUpdate.entry?.progressVolumes,
                                                ignored = ignoredIds.contains(it.id),
                                            )
                                        }
                                    }
                                )
                            }
                        }

                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry = it }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            refresh.flatMapLatest {
                Pager(config = PagingConfig(10)) {
                    AniListPagingSource {
                        val result = aniListApi.activityReplies(id = activityId, page = it)
                        result.page?.pageInfo to result.page?.activityReplies?.filterNotNull()
                            .orEmpty()
                    }
                }.flow
                    .cachedIn(viewModelScope)
            }
                .flatMapLatest { pagingData ->
                    replyStatusController.allChanges()
                        .mapLatest { updates ->
                            pagingData.map {
                                val liked = updates[it.id.toString()]?.liked ?: it.isLiked ?: false
                                Entry.ReplyEntry(reply = it, liked = liked)
                            }
                        }
                }
                .cachedIn(viewModelScope)
                .collect(replies::emit)
        }
    }

    fun refresh() {
        refresh.value = SystemClock.uptimeMillis()
    }

    fun delete(deletePromptData: Either<Unit, Entry.ReplyEntry>) {
        if (deleting) return
        deleting = true
        viewModelScope.launch(CustomDispatchers.IO) {
            if (deletePromptData is Either.Right) {
                aniListApi.deleteActivityReply(deletePromptData.value.reply.id.toString())
            } else {
                aniListApi.deleteActivity(activityId)
            }
            withContext(CustomDispatchers.Main) {
                refresh.emit(SystemClock.uptimeMillis())
                deleting = false
            }
        }
    }

    fun sendReply(reply: String) {
        if (replying) return
        replying = true
        viewModelScope.launch(CustomDispatchers.IO) {
            aniListApi.saveActivityReply(activityId = activityId, replyId = null, text = reply)
            withContext(CustomDispatchers.Main) {
                refresh.emit(SystemClock.uptimeMillis())
                replying = false
            }
        }
    }

    data class Entry(
        val activity: ActivityDetailsQuery.Data.Activity,
        val mediaEntry: MediaEntry?,
        override val liked: Boolean,
        override val subscribed: Boolean,
    ) : ActivityStatusAware {
        data class MediaEntry(
            val media: ActivityDetailsQuery.Data.ListActivityActivity.Media,
            override val mediaListStatus: MediaListStatus?,
            override val progress: Int?,
            override val progressVolumes: Int?,
            override val ignored: Boolean,
        ) : MediaStatusAware

        data class ReplyEntry(
            val reply: ActivityDetailsRepliesQuery.Data.Page.ActivityReply,
            val liked: Boolean,
        )
    }
}
