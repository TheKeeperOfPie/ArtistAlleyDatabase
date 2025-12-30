package com.thekeeperofpie.artistalleydatabase.alley.form

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils

actual object FormUtils {
    actual suspend fun generateEncryptedFormLink(): String {
        // Desktop doesn't encrypt since it's not published
        return Uri.parse(AlleyUtils.formUrl)
            .buildUpon()
            .appendQueryParameter(AlleyCryptography.ACCESS_KEY_PARAM, ArtistFormAccessKey.key)
            .build()
            .toString()
    }
}
