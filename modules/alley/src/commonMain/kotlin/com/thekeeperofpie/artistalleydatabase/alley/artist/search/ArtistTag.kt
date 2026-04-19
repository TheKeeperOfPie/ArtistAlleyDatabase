package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_tag_has_catalog
import artistalleydatabase.modules.alley.generated.resources.alley_artist_tag_new
import artistalleydatabase.modules.alley.generated.resources.alley_artist_tag_verified
import org.jetbrains.compose.resources.StringResource

enum class ArtistTag(internal val textRes: StringResource) {
    HAS_CATALOG(Res.string.alley_artist_tag_has_catalog),
    VERIFIED(Res.string.alley_artist_tag_verified),
    NEW(Res.string.alley_artist_tag_new),
}
