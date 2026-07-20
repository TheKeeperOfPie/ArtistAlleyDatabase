package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

internal object StampRallyUtils {

    private val animeExpoHallIds = setOf("KH", "SH", "WH")

    fun toValidBooth(rawBooth: String): String? {
        val input = rawBooth.replace("-", "")
        // TODO: Doesn't support double letter booths
        return when (input.length) {
            2 -> {
                val letter = input[0].uppercaseChar()
                val number = input.drop(1).toIntOrNull()
                if (letter.isLetter() && number != null && number != 0) {
                    "${letter}${number.toString().padStart(2, '0')}"
                } else {
                    null
                }
            }
            3 -> {
                val letter = input[0].uppercaseChar()
                val number = input.drop(1).toIntOrNull()
                if (letter.isLetter() && number != null && number != 0) {
                    "$letter${number.toString().padStart(2, '0')}"
                } else {
                    null
                }
            }
            6 -> {
                val hall = input.take(2).uppercase()
                val table = input.drop(2).toIntOrNull()
                if (hall in animeExpoHallIds && table != null && table != 0) {
                    "$hall-${table.toString().padStart(4, '0')}"
                } else {
                    null
                }
            }
            else -> null
        }
    }
}
