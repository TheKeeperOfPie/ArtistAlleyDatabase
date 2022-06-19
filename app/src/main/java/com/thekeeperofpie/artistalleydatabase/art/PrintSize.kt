package com.thekeeperofpie.artistalleydatabase.art

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.R
import kotlin.math.roundToInt

sealed class PrintSize(
    val printWidth: Int?,
    val printHeight: Int?,
    @StringRes val textRes: Int
) {

    companion object {

        val PORTRAITS = listOf(
            Unknown,
            Portrait8_5x11,
            Portrait11x17,
            Portrait12x18,
            Portrait13x19,
        )

        val LANDSCAPES = listOf(
            Unknown,
            Landscape11x8_5,
            Landscape17x11,
            Landscape18x12,
            Landscape19x13,
        )

        fun fromSize(printWidth: Int, printHeight: Int) = if (printWidth > printHeight) {
            LANDSCAPES.find { it.printWidth == printWidth && it.printHeight == printHeight }
        } else {
            PORTRAITS.find { it.printWidth == printWidth && it.printHeight == printHeight }
        } ?: Unknown
    }

    object Unknown : PrintSize(null, null, R.string.unknown)

    object Portrait8_5x11 :
        PrintSize((8.5 * 25.4).roundToInt(), (11 * 25.4).toInt(), R.string.print_size_8_5x11_inches)

    object Landscape11x8_5 :
        PrintSize((1 * 25.4).toInt(), (8.5 * 25.4).toInt(), R.string.print_size_11x8_5_inches)

    object Portrait11x17 :
        PrintSize((11 * 25.4).toInt(), (17 * 25.4).toInt(), R.string.print_size_11x17_inches)

    object Landscape17x11 :
        PrintSize((17 * 25.4).toInt(), (11 * 25.4).toInt(), R.string.print_size_17x11_inches)

    object Portrait12x18 :
        PrintSize((12 * 25.4).toInt(), (18 * 25.4).toInt(), R.string.print_size_12x18_inches)

    object Landscape18x12 :
        PrintSize((18 * 25.4).toInt(), (12 * 25.4).toInt(), R.string.print_size_18x12_inches)

    object Portrait13x19 :
        PrintSize((13 * 25.4).toInt(), (19 * 25.4).toInt(), R.string.print_size_13x19_inches)

    object Landscape19x13 :
        PrintSize((19 * 25.4).toInt(), (13 * 25.4).toInt(), R.string.print_size_19x13_inches)
}