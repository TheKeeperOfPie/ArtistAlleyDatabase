package com.thekeeperofpie.artistalleydatabase.entry

import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import kotlinx.coroutines.flow.MutableStateFlow

internal class TestSettings(cropUri: String? = null) : CropSettings {
    override var cropImageUri = MutableStateFlow(cropUri)
}
