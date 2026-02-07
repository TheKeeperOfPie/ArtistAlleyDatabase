package com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography

actual object ArtistFormAccessKey {

    private var _key: String? = null

    actual val key: String?
        get() = _key

    actual fun setKey(key: String) {
        _key = Uri.parseOrNull(key)
            ?.getQueryParameter(AlleyCryptography.ACCESS_KEY_PARAM)
            ?: key.substringAfter("?${AlleyCryptography.ACCESS_KEY_PARAM}=")
    }

    actual suspend fun setKeyEncrypted(key: String) {
        _key = key
    }
}
