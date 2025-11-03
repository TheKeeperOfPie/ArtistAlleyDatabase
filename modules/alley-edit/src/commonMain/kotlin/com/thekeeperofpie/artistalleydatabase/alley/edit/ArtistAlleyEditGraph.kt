package com.thekeeperofpie.artistalleydatabase.alley.edit

import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@DependencyGraph
interface ArtistAlleyEditGraph {

    val appFileSystem: AppFileSystem

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides scope: ApplicationScope): ArtistAlleyEditGraph
    }
}
