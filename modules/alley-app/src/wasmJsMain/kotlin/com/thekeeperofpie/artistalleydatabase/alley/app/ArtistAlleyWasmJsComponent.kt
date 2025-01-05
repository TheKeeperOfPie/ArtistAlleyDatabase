package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
abstract class ArtistAlleyWasmJsComponent(
    @get:Provides val scope: ApplicationScope,
) : ArtistAlleyAppComponent {
    abstract val appFileSystem: AppFileSystem

    val ArtistAlleyWasmJsDatabase.bindArtistAlleyDatabase: ArtistAlleyDatabase
        @Provides get() = this

    val ArtistAlleyWasmJsSettings.bindArtistAlleySettings: ArtistAlleySettings
        @Provides get() = this
}
