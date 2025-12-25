package com.thekeeperofpie.artistalleydatabase.alley.form

import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@DependencyGraph
internal interface ArtistAlleyFormWasmJsGraph : ArtistAlleyFormGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides scope: ApplicationScope): ArtistAlleyFormWasmJsGraph
    }
}
