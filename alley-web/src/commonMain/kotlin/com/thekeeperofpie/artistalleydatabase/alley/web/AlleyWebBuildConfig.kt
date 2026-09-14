package com.thekeeperofpie.artistalleydatabase.alley.web

import com.thekeeperofpie.artistalleydatabase.alley.web.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.utils.buildconfig.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
object AlleyWebBuildConfig : BuildConfig {
    override val buildType = if (BuildKonfig.debug) "debug" else "release"
}
