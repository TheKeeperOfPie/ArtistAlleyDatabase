package com.thekeeperofpie.artistalleydatabase

import com.thekeeperofpie.artistalleydatabase.animethemes.AnimeThemesComponent
import com.thekeeperofpie.artistalleydatabase.debug.DebugComponent
import com.thekeeperofpie.artistalleydatabase.utils.buildconfig.BuildConfig
import dev.zacsweers.metro.Binds

internal interface ApplicationVariantComponent : DebugComponent, AnimeThemesComponent {

    @Binds
    val InternalBuildConfig.bindBuildConfig: BuildConfig
}
