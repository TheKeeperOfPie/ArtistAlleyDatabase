package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.ui.graphics.vector.ImageVector
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_artists
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_browse
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_favorites
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_map
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_stamp_rallies
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled.List
import com.thekeeperofpie.artistalleydatabase.icons.filled.Approval
import com.thekeeperofpie.artistalleydatabase.icons.filled.Brush
import com.thekeeperofpie.artistalleydatabase.icons.filled.Favorite
import com.thekeeperofpie.artistalleydatabase.icons.filled.Map
import org.jetbrains.compose.resources.StringResource

enum class AlleyRootDestination(val icon: ImageVector, val textRes: StringResource) {
    ARTISTS(Icons.Default.Brush, Res.string.alley_nav_bar_artists),
    BROWSE(Icons.AutoMirrored.Default.List, Res.string.alley_nav_bar_browse),
    FAVORITES(Icons.Default.Favorite, Res.string.alley_nav_bar_favorites),
    MAP(Icons.Default.Map, Res.string.alley_nav_bar_map),
    STAMP_RALLIES(Icons.Default.Approval, Res.string.alley_nav_bar_stamp_rallies),
}
