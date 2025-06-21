package com.thekeeperofpie.artistalleydatabase.shared.alley.data

sealed class TableMin(val serializedValue: Int) {

    data object Paid : TableMin(-1)
    data object Free : TableMin(0)
    data object Any : TableMin(1)
    data class Price(val usd: Int) : TableMin(usd)
    data object Other : TableMin(-2)

    fun totalCost(tableCount: Int) = when (this) {
        Any -> tableCount
        Free -> 0
        Other,
        Paid -> null
        is Price -> tableCount * usd
    }

    companion object {
        fun parseFromSheet(value: String?) = when {
            value.isNullOrBlank() -> null
            value.equals("Other", ignoreCase = true) -> Other
            value.equals("Paid", ignoreCase = true) -> Paid
            value.equals("Free", ignoreCase = true) -> Free
            value.equals("Any", ignoreCase = true) -> Any
            else -> value.removePrefix("$").toIntOrNull()?.let(::Price)
        }

        fun parseFromValue(serializedValue: Int?) = when (serializedValue) {
            Other.serializedValue -> Other
            Paid.serializedValue -> Paid
            Free.serializedValue -> Free
            Any.serializedValue -> Any
            null -> null
            else -> Price(serializedValue)
        }
    }
}
