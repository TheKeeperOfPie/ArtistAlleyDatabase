package com.thekeeperofpie.artistalleydatabase.anime.data

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class NextAiringEpisode(
    val episode: Int,
    val airingAt: Instant,
)
