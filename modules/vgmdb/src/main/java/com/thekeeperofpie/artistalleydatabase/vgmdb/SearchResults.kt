package com.thekeeperofpie.artistalleydatabase.vgmdb

data class SearchResults(val albums: List<AlbumResult> = emptyList()) {

    data class AlbumResult(
        val id: String,
        val catalogId: String?,
        val names: Map<String, String>,
    )
}