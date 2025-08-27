package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.serialization.Serializable

@Serializable
data class CatalogImage(
    val name: String,
    val width: Int?,
    val height: Int?,
)
