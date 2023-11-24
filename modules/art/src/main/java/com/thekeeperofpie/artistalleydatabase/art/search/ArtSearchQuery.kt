package com.thekeeperofpie.artistalleydatabase.art.search

import com.thekeeperofpie.artistalleydatabase.art.sections.SourceType
import kotlinx.serialization.Serializable

@Serializable
data class ArtSearchQuery(
    val artists: List<String> = emptyList(),
    val source: SourceType? = null,
    val series: List<String> = emptyList(),
    val seriesById: List<String> = emptyList(),
    val characters: List<String> = emptyList(),
    val charactersById: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val printWidth: Int?,
    val printHeight: Int?,
    val notes: String? = null,
    val artistsLocked: Boolean? = null,
    val sourceLocked: Boolean? = null,
    val seriesLocked: Boolean? = null,
    val charactersLocked: Boolean? = null,
    val tagsLocked: Boolean? = null,
    val notesLocked: Boolean? = null,
    val printSizeLocked: Boolean? = null,
)
