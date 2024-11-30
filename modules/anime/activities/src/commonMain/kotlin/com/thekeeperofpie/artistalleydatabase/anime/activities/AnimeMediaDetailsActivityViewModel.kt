package com.thekeeperofpie.artistalleydatabase.anime.activities

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.data.fragment.ListActivityMediaListActivityItem
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
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
    @Assisted mediaId: String,
) : ViewModel() {

    var activities by mutableStateOf<MediaActivitiesEntry?>(null)

    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            aniListApi.authedUser
                .mapLatest { viewer ->
                    val result = aniListApi.mediaDetailsActivity(
                        mediaId = mediaId,
                        includeFollowing = viewer != null,
                    )
                    MediaActivitiesEntry(
                        following = result.following?.activities
                            ?.filterIsInstance<ListActivityMediaListActivityItem>()
                            .orEmpty()
                            .map(::MediaActivityEntry),
                        global = result.global?.activities
                            ?.filterIsInstance<ListActivityMediaListActivityItem>()
                            .orEmpty()
                            .map(::MediaActivityEntry),
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
}
