package com.thekeeperofpie.artistalleydatabase.cds

import android.app.Application
import com.squareup.moshi.Moshi
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.export.Exporter
import com.thekeeperofpie.artistalleydatabase.android_utils.importer.Importer
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDataConverter
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.cds.persistence.CdExporter
import com.thekeeperofpie.artistalleydatabase.cds.persistence.CdImporter
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
    fun provideCdEntryDao(database: CdEntryDatabase) = database.cdEntryDao()

    @Provides
    fun provideCdEntryDetailsDao(database: CdEntryDatabase) = database.cdEntryDetailsDao()

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
        moshi: Moshi,
    ): Importer = CdImporter(
        appContext = application,
        cdEntryDao = cdEntryDao,
        moshi = moshi,
    )
}