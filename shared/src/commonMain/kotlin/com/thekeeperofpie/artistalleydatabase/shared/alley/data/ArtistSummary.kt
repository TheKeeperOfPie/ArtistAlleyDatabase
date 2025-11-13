@file:OptIn(ExperimentalUuidApi::class)

package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ArtistSummary(
    val id: Uuid,
    val booth: String?,
    val name: String?,
)
