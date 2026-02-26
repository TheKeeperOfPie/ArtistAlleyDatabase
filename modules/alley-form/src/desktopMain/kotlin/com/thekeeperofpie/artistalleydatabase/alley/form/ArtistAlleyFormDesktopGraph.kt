package com.thekeeperofpie.artistalleydatabase.alley.form

import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditRemoteDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyFormRemoteDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImageUploader
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_network.buildNetworkClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@DependencyGraph
internal interface ArtistAlleyFormDesktopGraph : ArtistAlleyFormGraph, ArtistAlleyEditGraph {

    val editDatabase: AlleyEditRemoteDatabase
    val formDatabase: AlleyFormRemoteDatabase

    @Provides
    @SingleIn(AppScope::class)
    fun provideNetworkClient(): NetworkClient = buildNetworkClient()

    @Binds
    val FormImageUploader.bindImageUploader: ImageUploader

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides scope: ApplicationScope): ArtistAlleyFormDesktopGraph
    }
}
