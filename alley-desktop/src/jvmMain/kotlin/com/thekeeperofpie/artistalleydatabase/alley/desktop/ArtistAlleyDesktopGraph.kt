package com.thekeeperofpie.artistalleydatabase.alley.desktop

import artistalleydatabase.alley_desktop.generated.resources.Res
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.settings.AboutLibrariesProvider
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_network.buildNetworkClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@DependencyGraph
@SingleIn(AppScope::class)
interface ArtistAlleyDesktopGraph : ArtistAlleyGraph {
    val appFileSystem: AppFileSystem

    @Binds
    val ArtistAlleyDesktopSettings.bindArtistAlleySettings: ArtistAlleySettings

    @Provides
    @SingleIn(AppScope::class)
    fun provideNetworkClient(): NetworkClient = buildNetworkClient()

    @IntoSet
    @Provides
    fun provideAlleyAppAboutLibrariesProvider(): AboutLibrariesProvider =
        AlleyAppAboutLibrariesProvider

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides scope: ApplicationScope): ArtistAlleyDesktopGraph
    }
}

internal object AlleyAppAboutLibrariesProvider : AboutLibrariesProvider {
    override suspend fun readBytes() = Res.readBytes("files/aboutlibraries.json")
}
