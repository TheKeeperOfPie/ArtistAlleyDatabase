package com.thekeeperofpie.artistalleydatabase.utils

import kotlin.math.max

object StringUtils {

    fun compareSimilarity(first: String, second: String): Float {
        if (first == second) return 1f
        if (first.length < 3 || second.length < 3) return 0f

        val firstNormalized = first.replace(Regex("\\s+"), "")
        val secondNormalized = second.replace(Regex("\\s+"), "")
        val longestSubstring = findLongestCommonSubstring(firstNormalized, secondNormalized)
        val editDistance = calculateLevenshteinDistance(firstNormalized, secondNormalized)

        val longerLength = max(firstNormalized.length, secondNormalized.length).toFloat()
        val levenshteinScore = 1f - (editDistance / longerLength)
        val substringScore = longestSubstring.length / longerLength
        return max(levenshteinScore, substringScore)
    }

    private fun findLongestCommonSubstring(first: String, second: String): String {
        val firstLength = first.length
        val secondLength = second.length

        val matrix = Array(firstLength + 1) { IntArray(secondLength + 1) }

        var maximumLengthFound = 0
        var endingIndexInFirstText = 0

        for (row in 1..firstLength) {
            for (column in 1..secondLength) {
                if (first[row - 1].equals(second[column - 1], ignoreCase = true)) {
                    val currentSequenceLength = matrix[row - 1][column - 1] + 1
                    matrix[row][column] = currentSequenceLength

                    if (currentSequenceLength > maximumLengthFound) {
                        maximumLengthFound = currentSequenceLength
                        endingIndexInFirstText = row
                    }
                } else {
                    matrix[row][column] = 0
                }
            }
        }

        return if (maximumLengthFound == 0) {
            ""
        } else {
            first.substring(endingIndexInFirstText - maximumLengthFound, endingIndexInFirstText)
        }
    }

    private fun calculateLevenshteinDistance(first: String, second: String): Int {
        val firstLength = first.length
        val secondLength = second.length

        var previousRow = IntArray(secondLength + 1) { it }
        var currentRow = IntArray(secondLength + 1)

        (1..firstLength).forEach { firstIndex ->
            currentRow[0] = firstIndex
            (1..secondLength).forEach { secondIndex ->
                val equals = first[firstIndex - 1].equals(
                    other = second[secondIndex - 1],
                    ignoreCase = true,
                )

                val cost = if (equals) 0 else 1

                currentRow[secondIndex] = minOf(
                    currentRow[secondIndex - 1] + 1,
                    previousRow[secondIndex] + 1,
                    previousRow[secondIndex - 1] + cost
                )
            }

            val temp = previousRow
            previousRow = currentRow
            currentRow = temp
        }

        return previousRow[secondLength]
    }
}
