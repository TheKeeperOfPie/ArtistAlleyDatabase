package com.thekeeperofpie.artistalleydatabase.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.zacsweers.metro.Inject
import kotlin.reflect.KClass

@Inject
class InjectedWorkerFactory(
    private val workerCreators: Map<KClass<*>, WorkerCreator>,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = workerCreators.entries.find { it.key.qualifiedName == workerClassName }
        ?.value
        ?.invoke(appContext, workerParameters)
}
