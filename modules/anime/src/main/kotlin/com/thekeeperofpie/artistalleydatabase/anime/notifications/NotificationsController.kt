package com.thekeeperofpie.artistalleydatabase.anime.notifications

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.hoc081098.flowext.throttleTime
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsController(scopedApplication: ScopedApplication, aniListApi: AuthedAniListApi) {

    var unreadCount by mutableIntStateOf(0)
        private set

    private val refresh = MutableStateFlow(-1L)
    private val forceRefresh = MutableStateFlow(-1L)

    init {
        scopedApplication.scope.launch(CustomDispatchers.Main) {
            combine(
                aniListApi.authedUser,
                refresh.throttleTime(15.minutes),
                forceRefresh,
            ) { authedUser, _, _ ->
                if (authedUser == null) 0 else {
                    aniListApi.unreadNotificationCount()
                }
            }
                .catch {}
                .flowOn(CustomDispatchers.IO)
                .collectLatest { unreadCount = it }
        }
    }

    fun refresh() {
        refresh.value = SystemClock.uptimeMillis()
    }

    fun forceRefresh() {
        forceRefresh.value = SystemClock.uptimeMillis()
    }

    suspend fun clear() {
        withContext(CustomDispatchers.Main) {
            unreadCount = 0
        }
    }
}
