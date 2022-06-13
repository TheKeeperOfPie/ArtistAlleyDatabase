package com.thekeeperofpie.artistalleydatabase.add

import com.thekeeperofpie.artistalleydatabase.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AddEntryHiltModule {

    @Provides
    fun provideAppEntryDao(database: AppDatabase) = database.artEntryDao()
}