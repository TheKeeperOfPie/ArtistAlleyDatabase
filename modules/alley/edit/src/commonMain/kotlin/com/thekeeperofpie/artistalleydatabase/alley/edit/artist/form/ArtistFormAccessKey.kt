package com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form

expect object ArtistFormAccessKey {

    val key: String?
    fun setKey(key: String)
    suspend fun setKeyEncrypted(key: String)
}
