package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.settings.AboutLibrariesProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
interface ArtistAlleyAppGraph : ArtistAlleyGraph {
    @IntoSet
    @Provides
    fun provideAlleyAppAboutLibrariesProvider(): AboutLibrariesProvider =
        AlleyAppAboutLibrariesProvider
}
