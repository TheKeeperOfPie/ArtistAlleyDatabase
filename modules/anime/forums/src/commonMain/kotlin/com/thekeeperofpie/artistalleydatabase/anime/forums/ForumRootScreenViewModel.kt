package com.thekeeperofpie.artistalleydatabase.anime.forums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.forums.generated.resources.Res
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_root_error_loading
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.foldPreviousResult
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Inject
class ForumRootScreenViewModel(
    aniListApi: AuthedAniListApi,
) : ViewModel() {

    private val refresh = RefreshFlow()

    // TODO: Filter showAdult for forums in general
    val entry = flowForRefreshableContent(
        refresh = refresh,
        errorTextRes = Res.string.anime_forum_root_error_loading,
    ) { flowFromSuspend { aniListApi.forumRoot() } }
        .map {
            it.transformResult {
                ForumRootScreen.Entry(
                    stickied = it.stickied.threads.filterNotNull()
                        .filter { it.isSticky == true },
                    active = it.active.threads.filterNotNull(),
                    new = it.new.threads.filterNotNull(),
                    releases = it.releases.threads.filterNotNull(),
                )
            }
        }
        .foldPreviousResult()
        .flowOn(CustomDispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, LoadingResult.loading())

    fun refresh() = refresh.refresh()
}
