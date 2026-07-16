package com.thekeeperofpie.artistalleydatabase.alley.edit.lastviewed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hoc081098.flowext.retryWithExponentialBackoff
import com.thekeeperofpie.artistalleydatabase.alley.edit.AlleyEditDestination
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditRemoteDatabase
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LastViewedEvent
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LastViewedPage
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PageVisibility
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
class LastViewedConnection(
    appScope: ApplicationScope,
    remoteDatabase: AlleyEditRemoteDatabase,
    pageVisibility: PageVisibility,
) {
    private val instanceId = Uuid.random()

    var usersToVisits by mutableStateOf(emptyMap<String, List<LastViewedEvent.Sync.PageVisit>>())
        private set

    private val events = MutableSharedFlow<LastViewedEvent>(
        replay = 1,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        appScope.launch {
            pageVisibility.isVisible.flatMapLatest {
                ConsoleLogger.log("Page visibility changed $it")
                if (!it) return@flatMapLatest emptyFlow()
                channelFlow<Unit> {
                    try {
                        remoteDatabase.lastViewedUpdates(
                            instanceId = instanceId,
                            events = events,
                            onEvent = {
                                ConsoleLogger.log("Received LastViewedEvent: $it")
                                when (it) {
                                    is LastViewedEvent.Sync -> usersToVisits =
                                        it.usersToVisits
                                    is LastViewedEvent.Debug,
                                    is LastViewedEvent.Update,
                                        -> Unit
                                }
                            },
                        )
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        close(t)
                    } finally {
                        ConsoleLogger.log("Socket closed")
                    }
                }
                    .retryWithExponentialBackoff(5.seconds, 2.0, 10)
                    .catch {}
            }.collect()
        }
    }

    fun onPageView(destination: AlleyEditDestination) {
        ConsoleLogger.log("onPageView: $destination")
        val page = when (destination) {
            is AlleyEditDestination.ArtistEdit ->
                LastViewedPage.ArtistEdit(destination.artistId)
            is AlleyEditDestination.ArtistFormMerge ->
                LastViewedPage.ArtistFormMerge(destination.artistId)
            AlleyEditDestination.Admin,
            is AlleyEditDestination.ArtistAdd,
            is AlleyEditDestination.ArtistCatalogs,
            is AlleyEditDestination.ArtistFormHistory,
            AlleyEditDestination.ArtistFormQueue,
            is AlleyEditDestination.ArtistHistory,
            AlleyEditDestination.Home,
            is AlleyEditDestination.ImagesEdit,
            AlleyEditDestination.Merch,
            is AlleyEditDestination.MerchAdd,
            is AlleyEditDestination.MerchEdit,
            is AlleyEditDestination.MerchResolution,
            is AlleyEditDestination.RemoteArtistDataHistoryMerge,
            is AlleyEditDestination.RemoteArtistDataMerge,
            AlleyEditDestination.RemoteArtistDataQueue,
            AlleyEditDestination.Series,
            is AlleyEditDestination.SeriesAdd,
            is AlleyEditDestination.SeriesEdit,
            is AlleyEditDestination.SeriesResolution,
            AlleyEditDestination.StampRallies,
            is AlleyEditDestination.StampRalliesQueue,
            is AlleyEditDestination.StampRallyAdd,
            is AlleyEditDestination.StampRallyEdit,
            is AlleyEditDestination.StampRallyFormHistory,
            is AlleyEditDestination.StampRallyFormMerge,
            AlleyEditDestination.StampRallyFormQueue,
            is AlleyEditDestination.StampRallyHistory,
            AlleyEditDestination.TagResolution,
                -> return
        }
        events.tryEmit(LastViewedEvent.Update(page))
    }
}
