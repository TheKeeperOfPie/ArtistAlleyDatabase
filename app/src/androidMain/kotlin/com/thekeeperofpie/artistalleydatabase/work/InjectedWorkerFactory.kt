package com.thekeeperofpie.artistalleydatabase.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import me.tatarka.inject.annotations.Inject

@Inject
class InjectedWorkerFactory(
    private val workerCreators: Map<String, WorkerCreator>,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = workerCreators[workerClassName]?.invoke(appContext, workerParameters)
}
