package com.thekeeperofpie.artistalleydatabase.alley.discord

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

internal data class OAuthState(
    val userId: String,
    val dataYear: DataYear,
    val booth: String,
) {
    companion object {
        fun decode(value: String): OAuthState? {
            val values = value.split("-")
            if (values.size != 3) return null
            return OAuthState(
                userId = values[0].ifEmpty { return null },
                dataYear = DataYear.deserialize(values[1].ifEmpty { return null }) ?: return null,
                booth = values[2].ifEmpty { return null },
            )
        }
    }

    fun encode() = "$userId-${dataYear.serializedName}-$booth"
}
