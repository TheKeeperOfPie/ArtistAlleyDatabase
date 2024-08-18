package com.thekeeperofpie.artistalleydatabase.monetization.debug

import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DebugMonetizationHiltModule {

    @Provides
    fun provideMonetizationProvider(settings: MonetizationSettings): MonetizationProvider =
        DebugMonetizationProvider(settings)

    @Provides
    fun provideSubscriptionProvider(
        scopedApplication: ScopedApplication,
        settings: MonetizationSettings,
    ): SubscriptionProvider =
        DebugSubscriptionProvider(scopedApplication.scope, settings)
}
