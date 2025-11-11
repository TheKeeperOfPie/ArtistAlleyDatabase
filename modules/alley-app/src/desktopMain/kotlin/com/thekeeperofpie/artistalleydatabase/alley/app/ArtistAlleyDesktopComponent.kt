package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_network.buildNetworkClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@DependencyGraph
interface ArtistAlleyDesktopComponent : ArtistAlleyAppComponent {
    val appFileSystem: AppFileSystem

    @Binds
    val ArtistAlleyDesktopSettings.bindArtistAlleySettings: ArtistAlleySettings

    @Provides
    @SingleIn(AppScope::class)
    fun provideNetworkClient(): NetworkClient = buildNetworkClient()

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides scope: ApplicationScope): ArtistAlleyDesktopComponent
    }
}
