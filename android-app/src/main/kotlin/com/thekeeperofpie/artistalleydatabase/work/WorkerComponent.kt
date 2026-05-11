package com.thekeeperofpie.artistalleydatabase.work

import android.app.Application
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.thekeeperofpie.artistalleydatabase.export.ExportWorker
import com.thekeeperofpie.artistalleydatabase.importing.ImportWorker
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseSyncWorker
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

typealias WorkerCreator = (Context, WorkerParameters) -> ListenableWorker

interface WorkerComponent {
    val injectedWorkerFactory: InjectedWorkerFactory

    @SingleIn(AppScope::class)
    @Provides
    fun provideWorkManager(application: Application): WorkManager =
        WorkManager.getInstance(application)

    @Provides
    @IntoMap
    @ClassKey(ImportWorker::class)
    fun provideImportWorker(
        workerCreator: ImportWorker.Factory,
    ): WorkerCreator = { context, parameters -> workerCreator.create(context, parameters) }

    @Provides
    @IntoMap
    @ClassKey(ExportWorker::class)
    fun provideExportWorker(
        workerCreator: ExportWorker.Factory,
    ): WorkerCreator = { context, parameters -> workerCreator.create(context, parameters) }

    @Provides
    @IntoMap
    @ClassKey(DatabaseSyncWorker::class)
    fun provideDatabaseSyncWorker(
        workerCreator: DatabaseSyncWorker.Factory,
    ): WorkerCreator = { context, parameters -> workerCreator.create(context, parameters) }
}
