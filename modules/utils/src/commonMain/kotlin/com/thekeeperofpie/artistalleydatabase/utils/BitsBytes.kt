package com.thekeeperofpie.artistalleydatabase.utils

import kotlin.jvm.JvmInline

@JvmInline
value class Bits(private val bits: Long) {
    val inWholeBytes get() = bits / 8
    val inWholeKilobytes get() = inWholeBytes / 1000
    val inWholeMegabytes get() = inWholeKilobytes / 1000

    operator fun div(other: Int) = Bits(bits / other)
    operator fun div(other: Bits) = bits.toFloat() / other.bits
    operator fun times(other: Int) = Bits(bits * other)
    operator fun compareTo(other: Bits) = bits.compareTo(other.bits)
}

fun Int.asBytes() = Bits(this * 8L)
fun Long.asBytes() = Bits(this * 8L)

inline val Int.bytes get() = Bits(this * 8L)
inline val Int.kilobytes get() = bytes * 1000
inline val Int.megabytes get() = kilobytes * 1000
