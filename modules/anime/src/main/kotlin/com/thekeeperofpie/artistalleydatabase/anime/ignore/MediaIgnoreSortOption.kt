package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption

enum class MediaIgnoreSortOption(@StringRes override val textRes: Int) : SortOption {
    ID(R.string.anime_media_sort_id),
}
