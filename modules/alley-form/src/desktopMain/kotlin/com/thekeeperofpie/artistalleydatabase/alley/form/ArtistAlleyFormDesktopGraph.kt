package com.thekeeperofpie.artistalleydatabase.alley.form

import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_network.buildNetworkClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@DependencyGraph
internal interface ArtistAlleyFormDesktopGraph : ArtistAlleyFormGraph, ArtistAlleyEditGraph {

    @Provides
    @SingleIn(AppScope::class)
    fun provideNetworkClient(): NetworkClient = buildNetworkClient()

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides scope: ApplicationScope): ArtistAlleyFormDesktopGraph
    }
}
