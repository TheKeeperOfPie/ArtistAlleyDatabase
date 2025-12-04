@file:OptIn(ExperimentalUuidApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ArtistSummary(
    val id: Uuid,
    val booth: String?,
    val name: String?,
    val links: List<String>,
    val storeLinks: List<String>,
    val catalogLinks: List<String>,
    val seriesInferred: List<String>,
    val seriesConfirmed: List<String>,
    val merchInferred: List<String>,
    val merchConfirmed: List<String>,
    val images: List<CatalogImage>,
)
