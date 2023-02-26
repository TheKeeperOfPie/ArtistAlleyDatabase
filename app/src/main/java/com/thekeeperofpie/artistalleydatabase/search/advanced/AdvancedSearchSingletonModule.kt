package com.thekeeperofpie.artistalleydatabase.search.advanced

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AdvancedSearchSingletonModule {

    @Singleton
    @Provides
    fun provideAdvancedSearchRepository() = AdvancedSearchRepository
}