package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Stable
class TaskState<Result> {
    var isActive by mutableStateOf(false)
        internal set
    var isManualTrigger by mutableStateOf(false)
        internal set
    var lastError: Throwable? by mutableStateOf(null)
        internal set
    var lastResult: Pair<Boolean, Result>? by mutableStateOf(null)
        internal set

    val showBlockingLoadingIndicator: Boolean get() = isActive && isManualTrigger

    fun clearError() {
        lastError = null
    }

    fun clearResult() {
        lastResult = null
    }
}

@Composable
fun <T> GenericTaskErrorEffect(taskState: TaskState<T>, snackbarHostState: SnackbarHostState) {
    LaunchedEffect(taskState) {
        snapshotFlow { taskState.lastError }
            .collectLatest {
                val message = it?.message ?: return@collectLatest
                snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Long)
            }
    }
}

class ExclusiveTask<Captured, Result>(
    private val scope: CoroutineScope,
    private val taskAction: suspend (Captured) -> Result,
) {
    // This isn't persisted since usage is scoped to an active screen, unlike a background worker
    val state = TaskState<Result>()

    private var manualJob: Job? = null

    private var autoJob: Job? = null

    private val jobMutex = Mutex()

    fun triggerManual(captureState: () -> Captured) {
        if (manualJob?.isActive == true) return

        val captured = captureState()
        manualJob = scope.launch {
            jobMutex.withLock {
                autoJob?.cancelAndJoin()
                runTask(
                    isManual = true,
                    captured = captured,
                )
            }
        }
    }

    fun triggerAuto(captureState: () -> Captured) {
        // If manually triggered, ignore auto trigger
        if (manualJob?.isActive == true) return

        val captured = captureState()
        val previousJob = autoJob
        autoJob = scope.launch {
            jobMutex.withLock {
                previousJob?.cancelAndJoin()
                runTask(
                    isManual = false,
                    captured = captured,
                )
            }
        }
    }

    private suspend fun runTask(
        isManual: Boolean,
        captured: Captured,
    ) {
        try {
            Snapshot.withMutableSnapshot {
                state.isActive = true
                state.isManualTrigger = isManual
                state.lastError = null
            }

            val result = taskAction(captured)
            state.lastResult = isManual to result
        } catch (throwable: Throwable) {
            currentCoroutineContext().ensureActive()
            state.lastError = throwable
        } finally {
            Snapshot.withMutableSnapshot {
                state.isActive = false
                state.isManualTrigger = false
            }
        }
    }
}
