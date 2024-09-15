package com.thekeeperofpie.artistalleydatabase.anime.data

interface MediaQuickEditData {
    val mediaId: String
    val coverImageUrl: String?
    val mediaType: MediaType?
    val titleRomaji: String?
    val titleEnglish: String?
    val titleNative: String?
}
