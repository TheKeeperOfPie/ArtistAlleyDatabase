package com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

data class ArtistPreviousYearData(
    val artistId: String,
    val name: Pair<DataYear, String>?,
    val summary: Pair<DataYear, String>?,
    val socialLinks: Pair<DataYear, List<String>>?,
    val storeLinks: Pair<DataYear, List<String>>?,
    val series: Pair<DataYear, List<String>>?,
    val merch: Pair<DataYear, List<String>>?,
)
