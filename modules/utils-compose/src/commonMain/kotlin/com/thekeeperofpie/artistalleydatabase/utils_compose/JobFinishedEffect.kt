package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.thekeeperofpie.artistalleydatabase.utils.JobProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun JobFinishedEffect(state: MutableStateFlow<JobProgress>, onFinished: () -> Unit) {
    val updatedOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        state.collectLatest {
            if (it is JobProgress.Finished.Success) {
                updatedOnFinished()
                state.value = JobProgress.Idle
            }
        }
    }
}
