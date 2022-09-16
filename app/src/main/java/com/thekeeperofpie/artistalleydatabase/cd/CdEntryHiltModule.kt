package com.thekeeperofpie.artistalleydatabase.cd

import com.thekeeperofpie.artistalleydatabase.AppDatabase
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
}