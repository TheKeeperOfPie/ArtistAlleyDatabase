package com.thekeeperofpie.artistalleydatabase.anime.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.fragment.ListActivityMediaListActivityItem
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AnimeMediaDetailsActivityViewModel(
    private val aniListApi: AuthedAniListApi,
    private val activityStatusController: ActivityStatusController,
    @Assisted mediaDetailsViewModel: AnimeMediaDetailsViewModel,
) : ViewModel() {

    var activities by mutableStateOf<ActivitiesEntry?>(null)

    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            combine(
                aniListApi.authedUser,
                snapshotFlow { mediaDetailsViewModel.entry.result }.flowOn(CustomDispatchers.Main)
                    .filterNotNull(),
            ) { viewer, entry ->
                val result = aniListApi.mediaDetailsActivity(
                    mediaId = entry.mediaId,
                    includeFollowing = viewer != null,
                )
                ActivitiesEntry(
                    following = result.following?.activities
                        ?.filterIsInstance<ListActivityMediaListActivityItem>()
                        .orEmpty()
                        .map(::ActivityEntry),
                    global = result.global?.activities
                        ?.filterIsInstance<ListActivityMediaListActivityItem>()
                        .orEmpty()
                        .map(::ActivityEntry),
                )
            }
                .flatMapLatest { activities ->
                    activityStatusController.allChanges(
                        (activities.following.map { it.activityId }
                                + activities.global.map { it.activityId })
                            .toSet()
                    )
                        .mapLatest { updates ->
                            activities.copy(
                                following = activities.following.map {
                                    it.copy(
                                        liked = updates[it.activityId]?.liked ?: it.liked,
                                        subscribed = updates[it.activityId]?.subscribed
                                            ?: it.subscribed,
                                    )
                                },
                                global = activities.global.map {
                                    it.copy(
                                        liked = updates[it.activityId]?.liked ?: it.liked,
                                        subscribed = updates[it.activityId]?.subscribed
                                            ?: it.subscribed,
                                    )
                                },
                            )
                        }
                }
                .catch {}
                .flowOn(CustomDispatchers.IO)
                .collectLatest { activities = it }
        }
    }

    enum class ActivityTab {
        FOLLOWING, GLOBAL
    }

    data class ActivitiesEntry(
        val following: List<ActivityEntry>,
        val global: List<ActivityEntry>,
    )

    data class ActivityEntry(
        val activity: ListActivityMediaListActivityItem,
        val activityId: String = activity.id.toString(),
        override val liked: Boolean = activity.isLiked ?: false,
        override val subscribed: Boolean = activity.isSubscribed ?: false,
    ) : ActivityStatusAware
}
