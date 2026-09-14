package com.thekeeperofpie.artistalleydatabase.alley.models

// TODO: Doesn't support double letters well
data class Booth(val letters: String, val number: Int) : Comparable<Booth> {

    companion object {
        fun fromStringOrNull(value: String): Booth? {
            if (value.length !in (2..4)) {
                return null
            }
            val letters = value.takeWhile { !it.isDigit() }.ifEmpty { null } ?: return null
            val number = value.dropWhile { !it.isDigit() }.toIntOrNull()?.takeIf { it in (1..99) }
                ?: return null
            return Booth(letters, number)
        }
    }

    operator fun rangeTo(endInclusive: Booth): ClosedRange<Booth> = BoothRange(this, endInclusive)

    override operator fun compareTo(other: Booth): Int {
        val byLetter = letters.compareTo(other.letters)
        if (byLetter != 0) return byLetter
        return number.compareTo(other.number)
    }

    fun generateNext(): Booth = if (number == 99) {
        Booth(letters + 1, 1)
    } else {
        copy(number = number + 1)
    }

    override fun toString() = "$letters${number.toString().padStart(2, '0')}"
}

private class BoothRange(
    override val start: Booth,
    override val endInclusive: Booth,
) : ClosedRange<Booth>, Iterable<Booth> {
    // TODO: Doesn't support double letter booths
    override fun iterator() = object : Iterator<Booth> {
        var current = start
        override fun next() = current.generateNext().also { current = it }

        override fun hasNext() = start <= endInclusive
    }
}
