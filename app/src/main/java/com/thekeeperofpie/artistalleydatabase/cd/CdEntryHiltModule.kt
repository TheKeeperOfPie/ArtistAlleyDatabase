package com.thekeeperofpie.artistalleydatabase.cd

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.AppDatabase
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.export.Exporter
import com.thekeeperofpie.artistalleydatabase.android_utils.importer.Importer
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDataConverter
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.CdExporter
import com.thekeeperofpie.artistalleydatabase.cds.CdImporter
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
object CdEntryHiltModule {

    @Provides
    fun provideCdEntryDao(appDatabase: AppDatabase) = appDatabase.cdEntryDao()

    @Provides
    fun provideCdEntryDetailsDao(appDatabase: AppDatabase) = appDatabase.cdEntryDetailsDao()

    @IntoSet
    @Provides
    fun provideCdExporter(
        application: Application,
        cdEntryDao: CdEntryDao,
        aniListDataConverter: AniListDataConverter,
        vgmdbDataConverter: VgmdbDataConverter,
        appJson: AppJson
    ): Exporter = CdExporter(
        appContext = application,
        cdEntryDao = cdEntryDao,
        aniListDataConverter = aniListDataConverter,
        vgmdbDataConverter = vgmdbDataConverter,
        appJson = appJson
    )

    @IntoSet
    @Provides
    fun provideCdImporter(
        application: Application,
        cdEntryDao: CdEntryDao,
        appMoshi: AppMoshi,
    ): Importer = CdImporter(
        appContext = application,
        cdEntryDao = cdEntryDao,
        moshi = appMoshi.moshi,
    )
}