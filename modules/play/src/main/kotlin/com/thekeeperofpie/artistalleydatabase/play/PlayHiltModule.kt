package com.thekeeperofpie.artistalleydatabase.play

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PlayHiltModule {

//    @Provides
//    fun provideMonetizationProvider(
//        application: Application,
//        settings: MonetizationSettings,
//    ): MonetizationProvider = AdMobMonetizationProvider(application, settings)
}
