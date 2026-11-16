package com.thekeeperofpie.artistalleydatabase.alley.edit

import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.lastviewed.LastViewedConnection
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.AlleyEditNavStack
import com.thekeeperofpie.artistalleydatabase.alley.navigation.AlleyNavStack
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.AlleyUserSettings
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

@SingleIn(AppScope::class)
interface ArtistAlleyEditGraph : ArtistAlleyGraph {

    val appFileSystem: AppFileSystem
    val lastViewedConnection: LastViewedConnection

    val alleyEditCoilInit: AlleyEditCoilInit

    @Binds
    val ArtistAlleySettings.bindAlleyUserSettings: AlleyUserSettings

    val editNavigatorGraphFactory: ArtistAlleyEditNavigatorGraph.Factory
}

@GraphExtension(NavigatorScope::class)
interface ArtistAlleyEditNavigatorGraph : ViewModelGraph {

    @GraphExtension.Factory
    interface Factory {
        fun create(
            @Provides editNavStack: AlleyEditNavStack,
            @Provides alleyNavStack: AlleyNavStack,
        ): ArtistAlleyEditNavigatorGraph
    }
}
