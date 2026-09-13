package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_booth
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_commissions
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_merch
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_name
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_series
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_social_links
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_store_links
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_summary
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import org.jetbrains.compose.resources.StringResource

enum class ArtistSearchColumn(
    override val size: Dp,
    override val text: StringResource,
) : TwoWayGrid.Column {
    BOOTH(120.dp, Res.string.alley_artist_column_booth),
    NAME(160.dp, Res.string.alley_artist_column_name),
    SUMMARY(400.dp, Res.string.alley_artist_column_summary),
    SERIES(288.dp, Res.string.alley_artist_column_series),
    MERCH(144.dp, Res.string.alley_artist_column_merch),
    SOCIAL_LINKS(144.dp, Res.string.alley_artist_column_social_links),
    STORE_LINKS(96.dp, Res.string.alley_artist_column_store_links),
    COMMISSIONS(144.dp, Res.string.alley_artist_column_commissions),
}
