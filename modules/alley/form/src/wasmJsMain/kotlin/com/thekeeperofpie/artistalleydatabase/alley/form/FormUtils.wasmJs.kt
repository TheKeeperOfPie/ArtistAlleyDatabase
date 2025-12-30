package com.thekeeperofpie.artistalleydatabase.alley.form

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

actual object FormUtils {
    actual suspend fun generateEncryptedFormLink(): String {
        val existingEncryptionKey = try {
            localStorage[AlleyCryptography.ENCRYPTION_KEY]
        } catch (_: Throwable) {
            null
        }
        val encryptionKey = existingEncryptionKey
            ?: AlleyCryptography.generateSymmetricEncryptionKey().also {
                localStorage[AlleyCryptography.ENCRYPTION_KEY] = it
            }

        val encryptedAccessKey =
            AlleyCryptography.symmetricEncrypt(encryptionKey, ArtistFormAccessKey.key.orEmpty())
        return Uri.parse(AlleyUtils.formUrl)
            .buildUpon()
            .appendQueryParameter(AlleyCryptography.ACCESS_KEY_ENCRYPTED_PARAM, encryptedAccessKey)
            .build()
            .toString()
    }
}
