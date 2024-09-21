package com.thekeeperofpie.artistalleydatabase.debug

import com.thekeeperofpie.artistalleydatabase.debug.network.DebugNetworkController
import com.thekeeperofpie.artistalleydatabase.debug.network.DebugNetworkViewModel
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface DebugComponent {

    val debugNetworkViewModel: () -> DebugNetworkViewModel

    @Provides
    @IntoSet
    fun provideApolloHttpInterceptor(debugNetworkController: DebugNetworkController) =
        debugNetworkController.apolloHttpInterceptor
}
