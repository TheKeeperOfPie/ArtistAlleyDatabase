package com.thekeeperofpie.artistalleydatabase.search.advanced

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AdvancedSearchSingletonModule {

    @Provides
    fun provideAdvancedSearchRepository() = AdvancedSearchRepository
}