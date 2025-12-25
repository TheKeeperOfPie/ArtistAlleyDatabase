package com.thekeeperofpie.artistalleydatabase.alley.edit.form

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography

object ArtistFormAccessKey {

    var key: String? = null
        private set

    fun setKey(key: String) {
        this.key = Uri.parseOrNull(key)
            ?.getQueryParameter(AlleyCryptography.ACCESS_KEY_PARAM)
            ?: key.substringAfter("?${AlleyCryptography.ACCESS_KEY_PARAM}=")
    }
}
