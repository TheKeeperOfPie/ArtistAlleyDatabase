package com.thekeeperofpie.artistalleydatabase.play

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppUpdateChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayHiltModule {

    @Singleton
    @Provides
    fun provideSubscriptionProvider(
        scopedApplication: ScopedApplication,
        settings: MonetizationSettings,
    ): SubscriptionProvider = PlaySubscriptionProvider(scopedApplication, settings)

    @Singleton
    @Provides
    fun provideAppUpdateChecker(application: Application): AppUpdateChecker =
        PlayAppUpdateChecker(application)
}
