package com.thekeeperofpie.artistalleydatabase.alley.web

import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.settings.AboutLibrariesProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.CloseableGraph
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.OnGraphInit
import dev.zacsweers.metro.Provides

@DependencyGraph(AppScope::class)
interface ArtistAlleyWebGraph : ArtistAlleyGraph, CloseableGraph {
    val artistImageCache: ArtistImageCache

    @IntoSet
    @Provides
    fun provideAlleyAppAboutLibrariesProvider(): AboutLibrariesProvider =
        AlleyAppAboutLibrariesProvider

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides scope: ApplicationScope): ArtistAlleyWebGraph
    }
}
