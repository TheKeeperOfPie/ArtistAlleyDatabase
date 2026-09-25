package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_column_booth
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_column_fandom
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import org.jetbrains.compose.resources.StringResource

enum class StampRallySearchColumn(
    override val size: Dp,
    override val text: StringResource,
) : TwoWayGrid.Column {
    BOOTH(64.dp, Res.string.alley_stamp_rally_column_booth),
    FANDOM(160.dp, Res.string.alley_stamp_rally_column_fandom),
}
