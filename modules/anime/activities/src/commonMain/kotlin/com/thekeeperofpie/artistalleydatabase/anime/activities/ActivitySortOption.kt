package com.thekeeperofpie.artistalleydatabase.anime.activities

import artistalleydatabase.modules.anime.activities.generated.resources.Res
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_sort_newest
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_sort_oldest
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_sort_pinned
import com.anilist.data.type.ActivitySort
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import org.jetbrains.compose.resources.StringResource

enum class ActivitySortOption(
    override val textRes: StringResource,
    override val supportsAscending: Boolean = false,
) : SortOption {
    NEWEST(Res.string.anime_activity_sort_newest),
    OLDEST(Res.string.anime_activity_sort_oldest),
    PINNED(Res.string.anime_activity_sort_pinned),
    ;

    fun toApiValue() = when (this) {
        NEWEST -> listOf(ActivitySort.ID_DESC)
        OLDEST -> listOf(ActivitySort.ID)
        PINNED -> listOf(ActivitySort.PINNED, ActivitySort.ID_DESC)
    }
}
