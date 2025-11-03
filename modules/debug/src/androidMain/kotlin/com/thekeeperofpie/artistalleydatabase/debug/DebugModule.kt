package com.thekeeperofpie.artistalleydatabase.debug

import com.apollographql.apollo3.network.http.HttpInterceptor
import com.thekeeperofpie.artistalleydatabase.debug.network.DebugNetworkController
import com.thekeeperofpie.artistalleydatabase.debug.network.DebugNetworkViewModel
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides

interface DebugComponent {

    val debugNetworkViewModel: Provider<DebugNetworkViewModel>

    @Provides
    @IntoSet
    fun provideApolloHttpInterceptor(
        debugNetworkController: DebugNetworkController
    ): HttpInterceptor = debugNetworkController.apolloHttpInterceptor
}
