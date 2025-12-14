package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class MerchInfo(
    val name: String,
    val uuid: Uuid,
    val notes: String?,
) {
    companion object {
        fun fake(id: String) = MerchInfo(
            name = id,
            uuid = Utils.uuidFromRandomBytes(id),
            notes = null,
        )
    }
}
