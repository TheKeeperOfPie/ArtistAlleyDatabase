package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface LastViewedPage {

    @Serializable
    data class ArtistEdit(val artistId: Uuid) : LastViewedPage

    @Serializable
    data class ArtistFormMerge(val artistId: Uuid) : LastViewedPage
}
