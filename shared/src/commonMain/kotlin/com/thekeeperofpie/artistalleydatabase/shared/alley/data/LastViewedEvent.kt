package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface LastViewedEvent {

    @Serializable
    data class Debug(val message: String) : LastViewedEvent

    @Serializable
    data class Sync(val usersToViewedPages: Map<String, List<String>>) : LastViewedEvent

    @Serializable
    data class Update(val page: LastViewedPage) : LastViewedEvent
}
