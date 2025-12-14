package com.thekeeperofpie.artistalleydatabase.utils

import androidx.annotation.MainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ExclusiveProgressJob<T, R>(
    private val scope: CoroutineScope,
    private val run: suspend (T) -> R,
) {
    val state = MutableStateFlow<JobProgress<R>>(JobProgress.Idle())
    var job: Job? = null

    @MainThread
    fun launch(captureState: () -> T) {
        if (state.value is JobProgress.Loading) return
        val captured = captureState()
        val previousJob = job
        job = scope.launch {
            state.value = JobProgress.Loading()
            previousJob?.cancelAndJoin()
            state.value = JobProgress.Loading()
            state.value = try {
                val result = run(captured)
                JobProgress.Finished.Result(result)
            } catch (throwable: Throwable) {
                JobProgress.Finished.UnhandledError(throwable)
            }
        }
    }
}
