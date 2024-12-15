package com.thekeeperofpie.artistalleydatabase.anime.media.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.ui.graphics.vector.ImageVector
import artistalleydatabase.modules.anime.media.data.generated.resources.Res
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_view_option_compact
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_view_option_grid
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_view_option_large_card
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_view_option_small_card
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import org.jetbrains.compose.resources.StringResource

enum class MediaViewOption(val textRes: StringResource, val icon: ImageVector) {

    SMALL_CARD(Res.string.anime_media_view_option_small_card, Icons.Filled.ViewStream),
    LARGE_CARD(Res.string.anime_media_view_option_large_card, Icons.Filled.ViewAgenda),
    COMPACT(Res.string.anime_media_view_option_compact, Icons.AutoMirrored.Filled.ViewList),
    GRID(Res.string.anime_media_view_option_grid, Icons.Filled.GridView),
}

val MediaViewOption.widthAdaptiveCells
    get() = when (this) {
        MediaViewOption.SMALL_CARD,
        MediaViewOption.LARGE_CARD,
        MediaViewOption.COMPACT,
            -> GridUtils.standardWidthAdaptiveCells
        MediaViewOption.GRID -> GridUtils.smallWidthAdaptiveCells
    }
