package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph
interface ArtistAlleyWebGraph : ArtistAlleyAppGraph {
    val appFileSystem: AppFileSystem
    val artistImageCache: ArtistImageCache
    val deepLinker: DeepLinker

    @Binds
    val ArtistAlleyWebSettings.bindArtistAlleySettings: ArtistAlleySettings

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides scope: ApplicationScope): ArtistAlleyWebGraph
    }
}
