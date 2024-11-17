package com.thekeeperofpie.artistalleydatabase.anime.activity.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_activity_details_error_loading
import artistalleydatabase.modules.anime.generated.resources.anime_activity_error_deleting
import artistalleydatabase.modules.anime.generated.resources.anime_activity_error_replying
import com.anilist.data.ActivityDetailsQuery
import com.anilist.data.ActivityDetailsRepliesQuery
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityReplyStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityReplyToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ActivityDetailsViewModel(
    private val aniListApi: AuthedAniListApi,
    private val statusController: ActivityStatusController,
    private val mediaListStatusController: MediaListStatusController,
    private val replyStatusController: ActivityReplyStatusController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    @Assisted savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<AnimeDestination.ActivityDetails>(navigationTypeMap)
    val activityId = destination.activityId

    val viewer = aniListApi.authedUser
    val refresh = RefreshFlow()
    var entry by mutableStateOf<LoadingResult<Entry>>(LoadingResult.loading())
    var replies = MutableStateFlow(PagingData.empty<Entry.ReplyEntry>())

    var replying by mutableStateOf(false)
    var deleting by mutableStateOf(false)
    var error by mutableStateOf<Pair<StringResource, Throwable?>?>(null)

    val toggleHelper = ActivityToggleHelper(aniListApi, statusController, viewModelScope)
    val replyToggleHelper =
        ActivityReplyToggleHelper(aniListApi, replyStatusController, viewModelScope)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(refresh.updates, Res.string.anime_activity_details_error_loading) {
                flowFromSuspend {
                    aniListApi.activityDetails(activityId).activity
                }
            }
                .flatMapLatest { activity ->
                    statusController.allChanges(activityId)
                        .mapLatest { activityUpdates ->
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
                                        MediaCompactWithTagsEntry(media = it)
                                    }
                                )
                            }
                        }
                        .flatMapLatest { result ->
                            combine(
                                if (activity.result is ActivityDetailsQuery.Data.ListActivityActivity) {
                                    mediaListStatusController.allChanges(activityId)
                                } else {
                                    flowOf(null)
                                },
                                ignoreController.updates(),
                                settings.mediaFilteringData(forceShowIgnored = true),
                            ) { statuses, _, filteringData ->
                                result.transformResult {
                                    if (it.mediaEntry == null) return@transformResult it
                                    val mediaId = it.mediaEntry.media?.id?.toString()
                                    applyMediaFiltering(
                                        statuses = if (mediaId == null || statuses == null) {
                                            emptyMap()
                                        } else {
                                            mapOf(mediaId to statuses)
                                        },
                                        ignoreController = ignoreController,
                                        filteringData = filteringData,
                                        entry = it,
                                        filterableData = it.mediaEntry.mediaFilterable,
                                        copy = { copy(mediaEntry = mediaEntry?.copy(mediaFilterable = it)) },
                                    )
                                }
                            }
                        }
                }
                .catch {
                    emit(
                        LoadingResult.error(
                            Res.string.anime_activity_details_error_loading,
                            it
                        )
                    )
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry = it }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            refresh.updates
                .flatMapLatest {
                AniListPager {
                    val result = aniListApi.activityReplies(id = activityId, page = it)
                    result.page?.pageInfo to result.page?.activityReplies?.filterNotNull()
                        .orEmpty()
                }
            }
                .enforceUniqueIntIds { it.id }
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    replyStatusController.allChanges()
                        .mapLatest { updates ->
                            pagingData.mapOnIO {
                                val liked = updates[it.id.toString()]?.liked ?: it.isLiked ?: false
                                Entry.ReplyEntry(reply = it, liked = liked)
                            }
                        }
                }
                .cachedIn(viewModelScope)
                .collect(replies::emit)
        }
    }

    fun refresh() = refresh.refresh()

    fun delete(deletePromptData: Either<Unit, Entry.ReplyEntry>) {
        if (deleting) return
        deleting = true
        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                if (deletePromptData is Either.Right) {
                    aniListApi.deleteActivityReply(deletePromptData.value.reply.id.toString())
                } else {
                    aniListApi.deleteActivity(activityId)
                }
                withContext(CustomDispatchers.Main) {
                    refresh.refresh()
                    deleting = false
                }
            } catch (t: Throwable) {
                withContext(CustomDispatchers.Main) {
                    error = Res.string.anime_activity_error_deleting to t
                    deleting = false
                }
            }
        }
    }

    fun sendReply(reply: String) {
        if (replying) return
        replying = true
        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                aniListApi.saveActivityReply(activityId = activityId, replyId = null, text = reply)
                withContext(CustomDispatchers.Main) {
                    refresh.refresh()
                    replying = false
                }
            } catch (t: Throwable) {
                withContext(CustomDispatchers.Main) {
                    error = Res.string.anime_activity_error_replying to t
                    replying = false
                }
            }
        }
    }

    data class Entry(
        val activity: ActivityDetailsQuery.Data.Activity,
        val mediaEntry: MediaCompactWithTagsEntry?,
        override val liked: Boolean,
        override val subscribed: Boolean,
    ) : ActivityStatusAware {

        data class ReplyEntry(
            val reply: ActivityDetailsRepliesQuery.Data.Page.ActivityReply,
            val liked: Boolean,
        )
    }
}
