package com.thekeeperofpie.artistalleydatabase.search.advanced

import com.thekeeperofpie.artistalleydatabase.art.SourceType
import java.util.UUID

data class AdvancedSearchQuery(
    val id: String = UUID.randomUUID().toString(),
    val artists: List<String> = emptyList(),
    val source: SourceType? = null,
    val series: List<String> = emptyList(),
    val seriesById: List<Int> = emptyList(),
    val characters: List<String> = emptyList(),
    val charactersById: List<Int> = emptyList(),
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