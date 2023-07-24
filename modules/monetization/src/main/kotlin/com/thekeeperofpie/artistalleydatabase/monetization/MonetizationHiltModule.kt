package com.thekeeperofpie.artistalleydatabase.monetization

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MonetizationHiltModule {

    @Singleton
    @Provides
    fun provideMonetizationController(settings: MonetizationSettings) =
        MonetizationController(settings)
}
