package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlin.random.Random
import kotlin.uuid.Uuid

internal object Utils {

    fun uuidFromRandomBytes(seed: String): Uuid {
        val randomBytes = ByteArray(Uuid.SIZE_BYTES)
            .also { Random(seed.hashCode()).nextBytes(it) }
        randomBytes[6] = (randomBytes[6].toInt() and 0x0f).toByte()
        randomBytes[6] = (randomBytes[6].toInt() or 0x40).toByte()
        randomBytes[8] = (randomBytes[8].toInt() and 0x3f).toByte()
        randomBytes[8] = (randomBytes[8].toInt() or 0x80).toByte()
        return Uuid.fromByteArray(randomBytes)
    }
}
