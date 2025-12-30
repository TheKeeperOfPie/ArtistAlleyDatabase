package com.thekeeperofpie.artistalleydatabase.alley.edit.form

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import kotlinx.browser.sessionStorage
import org.w3c.dom.get
import org.w3c.dom.set

actual object ArtistFormAccessKey {

    private var _key: String? = null

    actual val key: String?
        get() = _key ?: try {
            sessionStorage[AlleyCryptography.ACCESS_KEY_PARAM]
        } catch (_: Throwable) {
            null
        }

    actual fun setKey(key: String) {
        val key = Uri.parseOrNull(key)
            ?.getQueryParameter(AlleyCryptography.ACCESS_KEY_PARAM)
            ?: key.substringAfter("?${AlleyCryptography.ACCESS_KEY_PARAM}=")
        _key = key
        sessionStorage[AlleyCryptography.ACCESS_KEY_PARAM] = key
    }
}
