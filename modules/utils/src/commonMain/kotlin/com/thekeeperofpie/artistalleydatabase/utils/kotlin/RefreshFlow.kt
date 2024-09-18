package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

// TODO: Default throttle can only be used once this is aware of success/failure,
//  or otherwise it would block retries on network failures
class RefreshFlow(val throttle: Duration? = null) {

    val updates get() = events
    private val events = MutableStateFlow(Event(Instant.DISTANT_PAST, false))

    fun refresh(fromUser: Boolean = true) {
        refresh(force = false, fromUser = fromUser)
    }

    fun forceRefresh() {
        refresh(force = true, fromUser = true)
    }

    private fun refresh(force: Boolean, fromUser: Boolean) {
        val refreshTime = Clock.System.now()
        events.update {
            // If the latest event is from after this method was called, drop the update
            if (it.timestamp >= refreshTime) return@update it

            // If not enough time has passed between refreshes, ignore the event
            if (!force && throttle != null && it.timestamp.plus(throttle) > refreshTime) {
                return@update it
            }

            Event(refreshTime, fromUser)
        }

    }

    data class Event(
        val timestamp: Instant,
        /**
         * Poorly named, but whether this event is the 2nd or later refresh. Basically not the
         * initial load of the screen. May not literally be from the user, but should act as a
         * fresh refresh that skips any network caching.
         */
        val fromUser: Boolean,
    )
}
