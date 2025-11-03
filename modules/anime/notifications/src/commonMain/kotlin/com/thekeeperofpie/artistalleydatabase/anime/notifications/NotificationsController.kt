package com.thekeeperofpie.artistalleydatabase.anime.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
@SingleIn(AppScope::class)
@Inject
class NotificationsController(scope: ApplicationScope, aniListApi: AuthedAniListApi) {

    var unreadCount by mutableIntStateOf(0)
        private set

    private val refresh = RefreshFlow(throttle = 15.minutes)

    init {
        scope.launch(CustomDispatchers.Main) {
            combine(aniListApi.authedUser, refresh.updates) { authedUser, _ ->
                if (authedUser == null) 0 else {
                    aniListApi.unreadNotificationCount()
                }
            }
                .catch {}
                .flowOn(CustomDispatchers.IO)
                .collectLatest { unreadCount = it }
        }
    }

    fun refresh() = refresh.refresh()

    fun forceRefresh() = refresh.forceRefresh()

    suspend fun clear() {
        withContext(CustomDispatchers.Main) {
            unreadCount = 0
        }
    }
}
