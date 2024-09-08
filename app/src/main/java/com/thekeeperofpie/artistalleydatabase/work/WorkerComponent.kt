package com.thekeeperofpie.artistalleydatabase.work

import android.app.Application
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.thekeeperofpie.artistalleydatabase.export.ExportWorker
import com.thekeeperofpie.artistalleydatabase.importing.ImportWorker
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseSyncWorker
import me.tatarka.inject.annotations.IntoMap
import me.tatarka.inject.annotations.Provides

typealias WorkerCreator = (Context, WorkerParameters) -> ListenableWorker

interface WorkerComponent {
    val injectedWorkerFactory: InjectedWorkerFactory

    @SingletonScope
    @Provides
    fun provideWorkManager(application: Application) = WorkManager.getInstance(application)

    @Provides
    @IntoMap
    fun provideImportWorker(
        workerCreator: (context: Context, params: WorkerParameters) -> ImportWorker,
    ): Pair<String, WorkerCreator> = ImportWorker::class.qualifiedName!! to workerCreator

    @Provides
    @IntoMap
    fun provideExportWorker(
        workerCreator: (context: Context, params: WorkerParameters) -> ExportWorker,
    ): Pair<String, WorkerCreator> = ExportWorker::class.qualifiedName!! to workerCreator

    @Provides
    @IntoMap
    fun provideDatabaseSyncWorker(
        workerCreator: (context: Context, params: WorkerParameters) -> DatabaseSyncWorker,
    ): Pair<String, WorkerCreator> = DatabaseSyncWorker::class.qualifiedName!! to workerCreator
}
