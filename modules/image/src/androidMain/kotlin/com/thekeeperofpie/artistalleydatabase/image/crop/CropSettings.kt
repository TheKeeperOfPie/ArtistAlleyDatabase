package com.thekeeperofpie.artistalleydatabase.image.crop

import kotlinx.coroutines.flow.MutableStateFlow

interface CropSettings {
    val cropImageUri: MutableStateFlow<String?>
}
