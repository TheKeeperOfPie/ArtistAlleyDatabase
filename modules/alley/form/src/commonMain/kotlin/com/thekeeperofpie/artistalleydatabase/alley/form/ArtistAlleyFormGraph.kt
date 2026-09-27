package com.thekeeperofpie.artistalleydatabase.alley.form

import com.thekeeperofpie.artistalleydatabase.alley.AlleyNavStack
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.AlleyEditCoilInit
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

@SingleIn(AppScope::class)
interface ArtistAlleyFormGraph : ArtistAlleyGraph {
    val appFileSystem: AppFileSystem
    val alleyEditCoilInit: AlleyEditCoilInit

    val formNavigatorGraphFactory: ArtistAlleyFormNavigatorGraph.Factory
}

@GraphExtension(NavigatorScope::class)
interface ArtistAlleyFormNavigatorGraph : ViewModelGraph {

    @GraphExtension.Factory
    interface Factory {
        fun create(@Provides alleyNavStack: AlleyNavStack): ArtistAlleyFormNavigatorGraph
    }
}
