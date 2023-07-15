package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import com.anilist.type.MediaSeason
import java.time.LocalDate

sealed interface AiringDate {

    data class Basic(
        val season: MediaSeason? = null,
        val seasonYear: String = "",
    ) : AiringDate

    data class Advanced(
        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null,
    ) : AiringDate
}
