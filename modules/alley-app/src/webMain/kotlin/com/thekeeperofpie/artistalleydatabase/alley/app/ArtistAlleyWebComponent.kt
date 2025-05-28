package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
abstract class ArtistAlleyWebComponent(
    @get:Provides val scope: ApplicationScope,
) : ArtistAlleyAppComponent {
    abstract val appFileSystem: AppFileSystem
    abstract val artistImageCache: ArtistImageCache

    val ArtistAlleyWebSettings.bindArtistAlleySettings: ArtistAlleySettings
        @Provides get() = this
}
