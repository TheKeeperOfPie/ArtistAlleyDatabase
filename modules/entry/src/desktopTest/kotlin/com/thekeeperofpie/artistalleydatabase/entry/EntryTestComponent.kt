package com.thekeeperofpie.artistalleydatabase.entry

import com.thekeeperofpie.artistalleydatabase.image.crop.CropController
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@SingletonScope
@Component
abstract class EntryTestComponent {
    @get:Provides
    val json: Json = Json.Default

    abstract val appFileSystem: AppFileSystem
    abstract val cropController: CropController
}
