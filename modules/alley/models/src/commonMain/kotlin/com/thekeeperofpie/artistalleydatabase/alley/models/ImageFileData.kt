package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ImageFileData(
    val id: Uuid,
    val extension: String,
)
