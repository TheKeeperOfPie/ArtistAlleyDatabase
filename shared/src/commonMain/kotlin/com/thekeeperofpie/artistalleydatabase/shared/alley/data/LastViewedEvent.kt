package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
sealed interface LastViewedEvent {

    @Serializable
    data class Debug(val message: String) : LastViewedEvent

    @Serializable
    data class Sync(val usersToVisits: Map<String, List<PageVisit>>) : LastViewedEvent {

        @Serializable
        data class PageVisit(val page: LastViewedPage, val lastUpdate: Instant)
    }

    @Serializable
    data class Update(val page: LastViewedPage) : LastViewedEvent
}
