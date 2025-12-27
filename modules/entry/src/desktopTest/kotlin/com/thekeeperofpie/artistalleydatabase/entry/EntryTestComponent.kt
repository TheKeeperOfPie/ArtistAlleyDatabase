package com.thekeeperofpie.artistalleydatabase.entry

import com.thekeeperofpie.artistalleydatabase.image.crop.CropController
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json

@SingleIn(AppScope::class)
@DependencyGraph
interface EntryTestComponent {
    @Provides
    fun provideJson(): Json = Json.Default

    val appFileSystem: AppFileSystem
}
