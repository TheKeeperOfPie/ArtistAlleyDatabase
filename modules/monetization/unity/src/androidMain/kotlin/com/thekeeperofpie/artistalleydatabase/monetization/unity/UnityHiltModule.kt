package com.thekeeperofpie.artistalleydatabase.monetization.unity

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.FeatureOverrideProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UnityHiltModule {

    @Provides
    fun provideMonetizationProvider(
        application: Application,
        settings: MonetizationSettings,
        featureOverrideProvider: FeatureOverrideProvider,
    ): MonetizationProvider =
        UnityMonetizationProvider(application, settings, featureOverrideProvider)
}
