package com.thekeeperofpie.artistalleydatabase.play

import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PlayHiltModule {

    @Provides
    fun provideSubscriptionProvider(
        scopedApplication: ScopedApplication,
        settings: MonetizationSettings,
    ): SubscriptionProvider = PlaySubscriptionProvider(scopedApplication, settings)
}
