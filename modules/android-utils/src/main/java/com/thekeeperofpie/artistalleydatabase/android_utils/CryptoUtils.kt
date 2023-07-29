package com.thekeeperofpie.artistalleydatabase.android_utils

import android.app.Application
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.MasterKey

object CryptoUtils {

    fun masterKey(application: Application): MasterKey {
        val masterKeyAlias = "${application.packageName}.AnichiveMasterKey"
        return MasterKey.Builder(application, masterKeyAlias)
            .setKeyGenParameterSpec(
                KeyGenParameterSpec.Builder(
                    masterKeyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            .setRequestStrongBoxBacked(true)
            .build()
    }
}
