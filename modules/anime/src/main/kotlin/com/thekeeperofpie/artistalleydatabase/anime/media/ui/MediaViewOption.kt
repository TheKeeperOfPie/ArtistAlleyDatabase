package com.thekeeperofpie.artistalleydatabase.anime.media.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.anime.R

enum class MediaViewOption(@StringRes val textRes: Int, val icon: ImageVector) {

    SMALL_CARD(R.string.anime_media_view_option_small_card, Icons.Filled.ViewStream),
    LARGE_CARD(R.string.anime_media_view_option_large_card, Icons.Filled.ViewAgenda),
    COMPACT(R.string.anime_media_view_option_compact, Icons.Filled.ViewList),
    GRID(R.string.anime_media_view_option_grid, Icons.Filled.GridView),
}
