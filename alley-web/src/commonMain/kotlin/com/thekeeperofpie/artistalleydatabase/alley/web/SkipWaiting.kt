package com.thekeeperofpie.artistalleydatabase.alley.web

import kotlinx.coroutines.channels.Channel

external val globalSkipWaitingBridge: SkipWaitingBridge

external class SkipWaitingBridge {
    fun onComposeReady(callback: () -> Unit)
    fun skipWaiting()
}

val showWaitingChannel = Channel<Boolean>(1)

val updateShowWaiting: () -> Unit = {
    showWaitingChannel.trySend(true)
}
