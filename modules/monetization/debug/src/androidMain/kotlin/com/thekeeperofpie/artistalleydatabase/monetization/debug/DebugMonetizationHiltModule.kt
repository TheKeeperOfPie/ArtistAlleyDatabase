package com.thekeeperofpie.artistalleydatabase.monetization.debug

import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
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
        applicationScope: ApplicationScope,
        settings: MonetizationSettings,
    ): SubscriptionProvider =
        DebugSubscriptionProvider(applicationScope, settings)
}
