package com.thekeeperofpie.artistalleydatabase.alley.data

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography

object AlleyDataUtils {

    fun formLink(baseUrl: String, accessKey: String): String =
        Uri.parse(baseUrl)
            .buildUpon()
            .path("/form/artist")
            .appendQueryParameter(AlleyCryptography.ACCESS_KEY_PARAM, accessKey)
            .build()
            .toString()
}
