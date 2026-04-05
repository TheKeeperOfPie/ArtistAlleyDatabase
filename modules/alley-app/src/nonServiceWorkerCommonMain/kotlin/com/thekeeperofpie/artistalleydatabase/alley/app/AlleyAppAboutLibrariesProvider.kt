package com.thekeeperofpie.artistalleydatabase.alley.app

import artistalleydatabase.modules.alley_app.generated.resources.Res
import com.thekeeperofpie.artistalleydatabase.alley.settings.AboutLibrariesProvider

internal object AlleyAppAboutLibrariesProvider : AboutLibrariesProvider {
    override suspend fun readBytes() = Res.readBytes("files/aboutlibraries.json")
}
