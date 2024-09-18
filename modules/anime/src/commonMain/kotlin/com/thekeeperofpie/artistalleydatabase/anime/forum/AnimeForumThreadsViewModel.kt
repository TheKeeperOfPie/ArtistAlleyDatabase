package com.thekeeperofpie.artistalleydatabase.anime.forum

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_forum_threads_error_loading
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadEntry
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadStatusController
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AnimeForumThreadsViewModel(
    private val aniListApi: AuthedAniListApi,
    private val threadStatusController: ForumThreadStatusController,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted mediaDetailsViewModel: AnimeMediaDetailsViewModel,
) : ViewModel() {
    var forumThreads by mutableStateOf<LoadingResult<List<ForumThreadEntry>>>(LoadingResult.loading())
        private set

    val threadToggleHelper =
        ForumThreadToggleHelper(aniListApi, threadStatusController, viewModelScope)

    private val mediaId = savedStateHandle.get<String>("mediaId")!!
    private var barrier = MutableStateFlow(false)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { mediaDetailsViewModel.entry.result }
                .filterNotNull()
                .take(1)
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { barrier.filter { it } }
                .flatMapLatest {
                    mediaDetailsViewModel.refresh.updates.mapLatest {
                        aniListApi.forumThreadSearch(
                            null,
                            false,
                            null,
                            mediaCategoryId = mediaId,
                            sort = ForumThreadSortOption.REPLIED_AT.toApiValue(sortAscending = false),
                            page = 1,
                        ).page.threads?.filterNotNull().orEmpty()
                            .map { ForumThreadEntry(thread = it, bodyMarkdown = null) }
                    }
                }
                .flatMapLatest { threads ->
                    threadStatusController.allChanges(threads.map { it.thread.id.toString() }
                        .toSet())
                        .mapLatest { updates ->
                            threads.map {
                                val update = updates[it.thread.id.toString()]
                                val liked = update?.liked ?: it.liked
                                val subscribed = update?.subscribed ?: it.subscribed
                                it.transformIf(liked != it.liked || subscribed != it.subscribed) {
                                    copy(liked = liked, subscribed = subscribed)
                                }
                            }
                        }
                }
                .map(LoadingResult.Companion::success)
                .catch {
                    emit(
                        LoadingResult.error(
                            Res.string.anime_media_details_forum_threads_error_loading,
                            it
                        )
                    )
                }
                .collectLatest { forumThreads = it }
        }
    }

    fun requestLoad() = barrier.tryEmit(true)
}
