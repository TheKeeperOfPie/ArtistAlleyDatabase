package com.thekeeperofpie.artistalleydatabase.debug

import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.debug.network.DebugNetworkController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DebugModule {

    @Singleton
    @Provides
    fun provideDebugNetworkController(scopedApplication: ScopedApplication) =
        DebugNetworkController(scopedApplication)

    @Singleton
    @Provides
    @IntoSet
    fun provideApolloHttpInterceptor(debugNetworkController: DebugNetworkController) =
        debugNetworkController.apolloHttpInterceptor
}
