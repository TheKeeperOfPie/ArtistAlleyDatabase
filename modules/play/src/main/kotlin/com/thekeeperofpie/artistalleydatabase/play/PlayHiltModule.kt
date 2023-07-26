package com.thekeeperofpie.artistalleydatabase.play

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PlayHiltModule {

    @Provides
    fun provideMonetizationProvider(
        application: Application,
        settings: MonetizationSettings,
    ): MonetizationProvider = AdMobMonetizationProvider(application, settings)
}
