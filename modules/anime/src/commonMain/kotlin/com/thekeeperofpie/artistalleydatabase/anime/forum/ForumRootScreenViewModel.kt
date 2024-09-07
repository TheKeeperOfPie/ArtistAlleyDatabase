package com.thekeeperofpie.artistalleydatabase.anime.forum

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_forum_root_error_loading
import com.anilist.fragment.ForumThread
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.flowForRefreshableContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

@Inject
class ForumRootScreenViewModel(
    aniListApi: AuthedAniListApi,
) : ViewModel() {

    private val refresh = MutableStateFlow(-1L)

    var content by mutableStateOf(LoadingResult.loading<Entry>())

    init {
        // TODO: Filter showAdult for forums in general
        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(
                refresh = refresh,
                errorTextRes = Res.string.anime_forum_root_error_loading,
            ) { flowFromSuspend { aniListApi.forumRoot() } }
                .map {
                    it.transformResult {
                        Entry(
                            stickied = it.stickied.threads.filterNotNull()
                                .filter { it.isSticky == true },
                            active = it.active.threads.filterNotNull(),
                            new = it.new.threads.filterNotNull(),
                            releases = it.releases.threads.filterNotNull(),
                        )
                    }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { content = it }
        }
    }

    fun refresh() {
        refresh.value = Clock.System.now().toEpochMilliseconds()
    }

    data class Entry(
        val stickied: List<ForumThread>,
        val active: List<ForumThread>,
        val new: List<ForumThread>,
        val releases: List<ForumThread>,
    )
}
