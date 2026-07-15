package com.thekeeperofpie.artistalleydatabase.alley.edit.lastviewed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hoc081098.flowext.interval
import com.thekeeperofpie.artistalleydatabase.alley.edit.AlleyEditDestination
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditRemoteDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LastViewedEvent
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LastViewedPage
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@SingleIn(AppScope::class)
@Inject
class LastViewedConnection(
    appScope: ApplicationScope,
    remoteDatabase: AlleyEditRemoteDatabase,
) {
    var usersToViewedPages by mutableStateOf(emptyMap<String, List<String>>())
        private set

    private val events = Channel<LastViewedEvent>(capacity = 10)

    init {
        appScope.launch {
            try {
                remoteDatabase.lastViewedUpdates(
                    events = events,
                    onEvent = {
                        when (it) {
                            is LastViewedEvent.Debug -> ConsoleLogger.log("Received LastViewedEvent.Debug: ${it.message}")
                            is LastViewedEvent.Sync -> usersToViewedPages = it.usersToViewedPages
                            is LastViewedEvent.Update,
                            is LastViewedEvent.Ping,
                                -> Unit
                        }
                    },
                )
                ConsoleLogger.log("Closing socket")
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
        appScope.launch {
            // TODO: Only send ping 60 seconds after any last event
            val initialDelay = if (BuildKonfig.isWasmDebug) 5.seconds else 60.seconds
            val period = if (BuildKonfig.isWasmDebug) 15.seconds else 60.seconds
            interval(initialDelay, period)
                .collectLatest {
                    events.send(LastViewedEvent.Debug("Pinging"))
                }
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
        events.trySend(LastViewedEvent.Update(page))
    }
}
