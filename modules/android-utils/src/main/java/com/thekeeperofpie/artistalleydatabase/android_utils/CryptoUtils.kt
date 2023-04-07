package com.thekeeperofpie.artistalleydatabase.android_utils

import android.app.Application
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.MasterKey

object CryptoUtils {

    private const val MASTER_KEY_ALIAS = "ArtistAlleyDatabaseKey"

    fun masterKey(application: Application) = MasterKey.Builder(application, MASTER_KEY_ALIAS)
        .setKeyGenParameterSpec(
            KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
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