package com.thekeeperofpie.artistalleydatabase.vgmdb

data class SearchResults(
    val albums: List<AlbumResult> = emptyList(),
    val artists: List<ArtistResult> = emptyList(),
) {

    data class AlbumResult(
        val id: String,
        val catalogId: String?,
        val names: Map<String, String>,
    )

    data class ArtistResult(
        val id: String,
        val name: String,
    )
}