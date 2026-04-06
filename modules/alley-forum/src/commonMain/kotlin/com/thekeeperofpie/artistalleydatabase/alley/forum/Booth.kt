package com.thekeeperofpie.artistalleydatabase.alley.forum

internal data class Booth(val letter: Char, val number: Int) : Comparable<Booth> {

    companion object {
        fun fromStringOrNull(value: String): Booth? {
            if (value.length !in (2..3)) {
                return null
            }
            val letter = value.firstOrNull()?.takeIf { it.isLetter() } ?: return null
            val number = value.drop(1).toIntOrNull()?.takeIf { it in (1..99) } ?: return null
            return Booth(letter, number)
        }
    }

    operator fun rangeTo(endInclusive: Booth): ClosedRange<Booth> = BoothRange(this, endInclusive)

    override operator fun compareTo(other: Booth): Int {
        val byLetter = letter.compareTo(other.letter)
        if (byLetter != 0) return byLetter
        return number.compareTo(other.number)
    }

    override fun toString() = "$letter${number.toString().padStart(2, '0')}"
}

private class BoothRange(
    override val start: Booth,
    override val endInclusive: Booth,
) : ClosedRange<Booth>, Iterable<Booth> {
    override fun iterator() = object : Iterator<Booth> {
        var current = start
        override fun next() = if (current.number == 99) {
            Booth(current.letter + 1, 1)
        } else {
            current.copy(number = current.number + 1)
        }

        override fun hasNext() = start <= endInclusive
    }
}
