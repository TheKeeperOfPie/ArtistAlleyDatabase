package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController

enum class MediaIgnoreSortOption(@StringRes override val textRes: Int) :
    AnimeMediaFilterController.Data.SortOption {
    ID(R.string.anime_media_sort_id),
}
