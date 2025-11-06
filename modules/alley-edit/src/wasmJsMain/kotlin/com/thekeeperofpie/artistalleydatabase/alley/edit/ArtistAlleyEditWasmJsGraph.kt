package com.thekeeperofpie.artistalleydatabase.alley.edit

import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@DependencyGraph
internal interface ArtistAlleyEditWasmJsGraph : ArtistAlleyEditGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides scope: ApplicationScope): ArtistAlleyEditWasmJsGraph
    }
}
