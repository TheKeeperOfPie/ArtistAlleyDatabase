package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlin.uuid.Uuid

data class ArtistInferenceData(
    val artistId: Uuid,
    val name: String,
    val links: List<String>,
    val storeLinks: List<String>,
    val catalogLinks: List<String>,
)
