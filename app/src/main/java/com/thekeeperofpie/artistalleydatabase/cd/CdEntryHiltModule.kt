package com.thekeeperofpie.artistalleydatabase.cd

import com.thekeeperofpie.artistalleydatabase.AppDatabase
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryDataConverter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CdEntryHiltModule {

    @Provides
    fun provideCdEntryDao(appDatabase: AppDatabase) = appDatabase.cdEntryDao()

    @Provides
    fun provideCdEntryDetailsDao(appDatabase: AppDatabase) = appDatabase.cdEntryDetailsDao()

    @Provides
    fun provideCdEntryDataConverter(vgmdbJson: VgmdbJson) = CdEntryDataConverter(vgmdbJson)
}