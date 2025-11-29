package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.serialization.Serializable

// See :modules:alley:edit string for descriptions
@Serializable
enum class ArtistStatus {
    UNKNOWN, LOCKED, NEEDS_ATTENTION, INFERRED, FINAL
}
