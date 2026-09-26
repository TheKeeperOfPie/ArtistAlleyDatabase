package com.thekeeperofpie.artistalleydatabase.alley

import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.alley.export.QrCodeViewModel
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.settings.AboutLibrariesProvider
import com.thekeeperofpie.artistalleydatabase.alley.settings.AlleyAboutLibrariesProvider
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.utils.buildconfig.BuildConfig
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import kotlin.reflect.KClass

interface ArtistAlleyGraph {

    val buildConfig: BuildConfig
    val seriesEntryCache: SeriesEntryCache

    val alleyCoilInit: AlleyCoilInit

    val settings: ArtistAlleySettings
    val aboutLibrariesProviders: Set<AboutLibrariesProvider>
    
    val navigatorGraphFactory: ArtistAlleyNavigatorGraph.Factory

    @IntoSet
    @Provides
    fun provideAlleyAboutLibrariesProvider(): AboutLibrariesProvider = AlleyAboutLibrariesProvider
}

@GraphExtension(NavigatorScope::class)
interface ArtistAlleyNavigatorGraph : ViewModelGraph {

    @GraphExtension.Factory
    interface Factory {
        fun create(@Provides navStack: AlleyNavStack): ArtistAlleyNavigatorGraph
    }
}

@ContributesBinding(NavigatorScope::class)
@SingleIn(NavigatorScope::class)
class ViewModelFactory(
    override val viewModelProviders: Map<KClass<out ViewModel>, () -> ViewModel>,
    override val assistedFactoryProviders: Map<KClass<out ViewModel>, () -> ViewModelAssistedFactory>,
    override val manualAssistedFactoryProviders: Map<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory>,
) : MetroViewModelFactory()
