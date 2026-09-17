package com.thekeeperofpie.artistalleydatabase.utils_compose.transform

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.TimeSource

// Copied from https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:appstate/transform/transform/src/commonMain/kotlin/androidx/appstate/transform/Transform.kt;drc=f2565e217827ef68cfc80160661f0aa6cd38cd2f
// since that's not released yet. Edited slightly to avoid providing an initialValue, instead just
// synchronously generating the first value.

fun <T> transform(
    scope: CoroutineScope,
    context: CoroutineContext = Dispatchers.Main,
    onUpdate: @Composable () -> T,
): State<T> {
    var state: MutableState<T>? = null
    transformInternal(scope, context, onUpdate) {
        if (state == null) {
            state = mutableStateOf(it)
        } else {
            state.value = it
        }
    }
    return state!!
}

fun <T> transformFlow(
    scope: CoroutineScope,
    context: CoroutineContext = Dispatchers.Main,
    onUpdate: @Composable () -> T,
): StateFlow<T> {
    var state: MutableStateFlow<T>? = null
    transformInternal(scope, context, onUpdate) {
        if (state == null) {
            state = MutableStateFlow(it)
        } else {
            state.value = it
        }
    }
    return state!!
}

fun <T> transformInternal(
    scope: CoroutineScope,
    context: CoroutineContext = Dispatchers.Main,
    onUpdate: @Composable () -> T,
    onValue: (T) -> Unit,
) {
    GlobalSnapshotManager.ensureStarted()
    val clockContext = GatedFrameClock(scope, context)
    val finalContext = context + clockContext

    val recomposer = Recomposer(finalContext)
    val composition = Composition(UnitApplier, recomposer)
    var snapshotHandle: ObserverHandle? = null
    scope.launch(finalContext, start = CoroutineStart.UNDISPATCHED) {
        try {
            recomposer.runRecomposeAndApplyChanges()
        } finally {
            composition.dispose()
            snapshotHandle?.dispose()
        }
    }

    var applyScheduled = false
    snapshotHandle = Snapshot.registerGlobalWriteObserver {
        if (!applyScheduled) {
            applyScheduled = true
            scope.launch(finalContext) {
                applyScheduled = false
                Snapshot.sendApplyNotifications()
            }
        }
    }

    composition.setContent {
        onValue(onUpdate())
    }
}

private object UnitApplier : AbstractApplier<Unit>(Unit) {
    override fun insertBottomUp(index: Int, instance: Unit) {}

    override fun insertTopDown(index: Int, instance: Unit) {}

    override fun move(from: Int, to: Int, count: Int) {}

    override fun remove(index: Int, count: Int) {}

    override fun onClear() {}
}

@OptIn(ExperimentalAtomicApi::class)
internal object GlobalSnapshotManager {
    private val started = AtomicBoolean(false)
    private val sent = AtomicBoolean(false)

    internal fun ensureStarted() {
        if (started.compareAndSet(expectedValue = false, newValue = true)) {
            val channel = Channel<Unit>(1)
            CoroutineScope(EmptyCoroutineContext).launch {
                channel.consumeEach {
                    sent.store(false)
                    Snapshot.sendApplyNotifications()
                }
            }
            Snapshot.registerGlobalWriteObserver {
                if (sent.compareAndSet(expectedValue = false, newValue = true)) {
                    channel.trySend(Unit)
                }
            }
        }
    }
}

internal class GatedFrameClock(scope: CoroutineScope, context: CoroutineContext) :
    MonotonicFrameClock {

    var isRunning: Boolean = true
        set(value) {
            val started = value && !field
            field = value
            if (started) {
                sendFrame()
            }
        }

    private val markedTime = TimeSource.Monotonic.markNow()
    private var lastNanos = 0L
    private var lastOffset = 0

    private fun sendFrame() {
        val timeNanos = markedTime.elapsedNow().inWholeNanoseconds
        val offset =
            if (timeNanos == lastNanos) {
                lastOffset + 1
            } else {
                lastNanos = timeNanos
                0
            }
        lastOffset = offset

        clock.sendFrame(timeNanos + offset)
    }

    private val clock = BroadcastFrameClock {
        if (isRunning) {
            scope.launch(context) { sendFrame() }
        }
    }

    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R) =
        clock.withFrameNanos(onFrame)
}
