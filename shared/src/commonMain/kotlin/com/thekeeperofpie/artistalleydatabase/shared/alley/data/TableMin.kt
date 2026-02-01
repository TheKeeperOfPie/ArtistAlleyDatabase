package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.serialization.Serializable

@Serializable
sealed class TableMin(val serializedValue: Int) {

    @Serializable
    data object Paid : TableMin(-1)

    @Serializable
    data object Free : TableMin(0)

    @Serializable
    data object Any : TableMin(1)

    @Serializable
    data class Price(val usd: Int) : TableMin(usd)

    @Serializable
    data object Other : TableMin(-2)

    fun totalCost(tableCount: Int) = when (this) {
        Any -> tableCount
        Free -> 0
        Other,
        Paid,
            -> null
        is Price -> tableCount * usd
    }

    companion object {
        fun parseFromValue(serializedValue: Int) = when (serializedValue) {
            Other.serializedValue -> Other
            Paid.serializedValue -> Paid
            Free.serializedValue -> Free
            Any.serializedValue -> Any
            else -> Price(serializedValue)
        }
    }
}
