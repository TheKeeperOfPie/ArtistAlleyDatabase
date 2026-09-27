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
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
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
    transformInternal(
        scope = scope,
        context = context,
        applyCompositionContent = {
            it.setContent {
                val newValue = onUpdate()
                val mutableState = state
                if (mutableState == null) {
                    state = mutableStateOf(newValue)
                } else {
                    mutableState.value = newValue
                }
            }
        },
    )
    return state!!
}

fun <T> transform(
    scope: CoroutineScope,
    initialValue: T,
    context: CoroutineContext = Dispatchers.Main,
    onUpdate: @Composable (T) -> T,
): State<T> {
    val state = mutableStateOf(initialValue)
    transformInternal(
        scope = scope,
        context = context,
        applyCompositionContent = {
            it.setContent {
                state.value = onUpdate(state.value)
            }
        },
    )
    return state
}

fun <T> transformFlow(
    scope: CoroutineScope,
    context: CoroutineContext = Dispatchers.Main,
    onUpdate: @Composable () -> T,
): StateFlow<T> {
    var state: MutableStateFlow<T>? = null
    transformInternal(
        scope = scope,
        context = context,
        applyCompositionContent = {
            it.setContent {
                val newValue = onUpdate()
                val stateFlow = state
                if (stateFlow == null) {
                    state = MutableStateFlow(newValue)
                } else {
                    @Suppress("StateFlowValueCalledInComposition")
                    stateFlow.value = newValue
                }
            }
        },
    )
    return state!!
}

fun transformInternal(
    scope: CoroutineScope,
    context: CoroutineContext = Dispatchers.Main,
    applyCompositionContent: (Composition) -> Unit,
) {
    GlobalSnapshotManager.ensureStarted()
    val clock = HeadlessTransformClock()
    val finalContext = context + clock

    val recomposer = Recomposer(finalContext)
    val composition = Composition(UnitApplier, recomposer)
    var snapshotHandle: ObserverHandle? = null
    scope.launch(context, start = CoroutineStart.UNDISPATCHED) { clock.runClock() }
    scope.launch(finalContext, start = CoroutineStart.UNDISPATCHED) {
        try {
            recomposer.runRecomposeAndApplyChanges()
        } finally {
            composition.dispose()
            snapshotHandle?.dispose()
            clock.cancel()
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

    applyCompositionContent(composition)
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

private val NotStarted = Any()
private val NotStartedFrameRequested = Any()
private val Idle = Any()
private val FrameRequested = Any()
private val Cancelled = Any()

@OptIn(ExperimentalAtomicApi::class)
private class HeadlessTransformClock(timeSource: TimeSource = TimeSource.Monotonic) :
    MonotonicFrameClock {

    private val state = AtomicReference(NotStarted)
    private val startMark = timeSource.markNow()
    private val broadcast = BroadcastFrameClock(::requestFrame)

    private var lastFrameTimeNanos = 0L
    private var lastOffsetNanos = 0

    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R =
        broadcast.withFrameNanos(onFrame)

    suspend fun runClock() {
        if (!claimPump()) return
        try {
            pumpFrames()
        } finally {
            cancel()
        }
    }

    fun cancel() {
        val previous = state.exchange(Cancelled)
        if (previous === Cancelled) return
        broadcast.cancel(CancellationException("HeadlessTransformClock was cancelled"))
        @Suppress("UNCHECKED_CAST")
        (previous as? CancellableContinuation<Unit>)?.resume(Unit)
    }

    private fun claimPump(): Boolean {
        while (true) {
            when (val current = state.load()) {
                NotStarted -> if (state.compareAndSet(current, Idle)) return true
                NotStartedFrameRequested ->
                    if (state.compareAndSet(current, FrameRequested)) return true
                Cancelled -> return false
                else ->
                    error(
                        "runClock() is already running; one clock per composition should be used."
                    )
            }
        }
    }

    private suspend fun pumpFrames(): Nothing {
        while (true) {
            currentCoroutineContext().ensureActive()
            when (val current = state.load()) {
                FrameRequested ->
                    if (state.compareAndSet(current, Idle)) {
                        broadcast.sendFrame(nextFrameTimeNanos())
                    }
                Idle -> park()
                Cancelled -> throw CancellationException("HeadlessTransformClock was cancelled")
                else -> error("Unexpected state while pumping frames: $current")
            }
        }
    }

    private suspend fun park() {
        suspendCancellableCoroutine { continuation ->
            if (state.compareAndSet(Idle, continuation)) {
                continuation.invokeOnCancellation { state.compareAndSet(continuation, Cancelled) }
            } else {
                continuation.resume(Unit)
            }
        }
    }

    private fun nextFrameTimeNanos(): Long {
        val elapsedNanos = startMark.elapsedNow().inWholeNanoseconds
        val offset =
            if (elapsedNanos == lastFrameTimeNanos) {
                lastOffsetNanos + 1
            } else {
                lastFrameTimeNanos = elapsedNanos
                0
            }
        lastOffsetNanos = offset

        return elapsedNanos + offset
    }

    private fun requestFrame() {
        while (true) {
            when (val current = state.load()) {
                FrameRequested,
                NotStartedFrameRequested,
                Cancelled,
                    -> return
                NotStarted -> if (state.compareAndSet(current, NotStartedFrameRequested)) return
                Idle -> if (state.compareAndSet(current, FrameRequested)) return
                else ->
                    if (state.compareAndSet(current, FrameRequested)) {
                        @Suppress("UNCHECKED_CAST")
                        (current as CancellableContinuation<Unit>).resume(Unit)
                        return
                    }
            }
        }
    }
}
