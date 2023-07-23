package com.thekeeperofpie.artistalleydatabase.anime.activity

import androidx.annotation.StringRes
import com.anilist.type.ActivitySort
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption

enum class ActivitySortOption(
    @StringRes override val textRes: Int,
    override val supportsAscending: Boolean = false,
) : SortOption {
    NEWEST(R.string.anime_activity_sort_newest),
    OLDEST(R.string.anime_activity_sort_oldest),
    PINNED(R.string.anime_activity_sort_pinned),
    ;

    fun toApiValue() = when (this) {
        NEWEST -> listOf(ActivitySort.ID_DESC)
        OLDEST -> listOf(ActivitySort.ID)
        PINNED -> listOf(ActivitySort.PINNED, ActivitySort.ID_DESC)
    }
}
