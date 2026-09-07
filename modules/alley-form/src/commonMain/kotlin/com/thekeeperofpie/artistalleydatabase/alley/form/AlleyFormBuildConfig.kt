package com.thekeeperofpie.artistalleydatabase.alley.form

import com.thekeeperofpie.artistalleydatabase.alley.form.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.utils.buildconfig.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

// TODO: Contributes doesn't work on web
@ContributesBinding(AppScope::class)
@Inject
class AlleyFormBuildConfig : BuildConfig {
    override val buildType = if (BuildKonfig.debug) "debug" else "release"
}
