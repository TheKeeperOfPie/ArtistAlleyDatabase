package com.thekeeperofpie.artistalleydatabase.alley.search

import androidx.compose.ui.graphics.vector.ImageVector
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_card
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_image
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_list
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_table
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled.ViewList
import com.thekeeperofpie.artistalleydatabase.icons.filled.Image
import com.thekeeperofpie.artistalleydatabase.icons.filled.TableChart
import com.thekeeperofpie.artistalleydatabase.icons.filled.ViewAgenda
import org.jetbrains.compose.resources.StringResource

enum class SearchDisplayType(val label: StringResource, val icon: ImageVector) {
    CARD(Res.string.alley_display_type_card, Icons.Filled.ViewAgenda),
    IMAGE(Res.string.alley_display_type_image, Icons.Filled.Image),
    LIST(Res.string.alley_display_type_list, Icons.AutoMirrored.Filled.ViewList),
    TABLE(Res.string.alley_display_type_table, Icons.Default.TableChart),
}
