package com.thekeeperofpie.artistalleydatabase.composite.utils

import java.io.File
import java.net.URI
import java.util.Locale

object Utils {

    private val WHITESPACE_OR_UNDERLINE_OR_SLASH = Regex("[\\s_/]")

    fun String.toClassName() = split(WHITESPACE_OR_UNDERLINE_OR_SLASH)
        .joinToString(separator = "", transform = ::capitalized)

    fun String.toPropertyName() = split(WHITESPACE_OR_UNDERLINE_OR_SLASH)
        .joinToString(separator = "", transform = ::capitalized)
        .replaceFirstChar { it.lowercase() }

    fun String.toFunctionName() = split(WHITESPACE_OR_UNDERLINE_OR_SLASH)
        .filterNot { it.startsWith("{") }
        .joinToString(separator = "", transform = ::capitalized)
        .replaceFirstChar { it.lowercase() }

    private fun capitalized(value: String) =
        value.replaceFirstChar {
            if (it.isLowerCase()) {
                it.titlecase(Locale.getDefault())
            } else it.toString()
        }

    fun reformatKotlinSource(file: File) {
        file.writeText(
            file.readText()
                // Strip public modifier from vals
                .replace(Regex("^(\\s*)(public )(val.*\\n)", RegexOption.MULTILINE)) {
                    it.groupValues[1] + it.groupValues[3]
                }
                // Strip public modifier from class headers
                .replace(Regex("(.*)(public )(.*class .*\\n)", RegexOption.MULTILINE)) {
                    it.groupValues[1] + it.groupValues[3]
                }
                // Strip public modifier from function headers
                .replace(Regex("(.*)(public )(.*fun .*\\n)", RegexOption.MULTILINE)) {
                    it.groupValues[1] + it.groupValues[3]
                }
                // Strip explicit fun typing
                .replace(Regex("(.*fun .*):.*( = .*[^{]\$)", RegexOption.MULTILINE)) {
                    it.groupValues[1] + it.groupValues[2]
                }
                // Strip explicit generic typing
                .replace(Regex("(.*return .*)<.*>(.*\\n)", RegexOption.MULTILINE)) {
                    it.groupValues[1] + it.groupValues[2]
                }
                // Remove extra newlines
                .replace("\n\n\n", "\n\n")
                .run {
                    if (!contains("data class")) {
                        // Strip explicit typing
                        replace(Regex("(.*val .*): .*( =.*\\n)", RegexOption.MULTILINE)) {
                            it.groupValues[1] + it.groupValues[2]
                        }
                    } else this
                }
        )
    }

    fun jsonUrlToClassName(url: String) = URI.create(url).path
        .substringAfterLast("/")
        .removeSuffix(".json")
        .filter(Char::isLetterOrDigit)
        .run(::capitalized)
}