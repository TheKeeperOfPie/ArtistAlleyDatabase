package com.thekeeperofpie.artistalleydatabase.android_utils.export

import java.io.File

object ExportUtils {

    fun buildEntryFilePath(entryId: String, vararg values: List<String>) = values
        .map { it.filter { it.isNotBlank() } }
        .filter { it.isNotEmpty() }
        .map {
            it.joinToString(separator = "-") {
                it.replace("\\", "\u29F5")
                    .replace("/", "\u2215")
                    .replace(":", "\uA789")
                    .replace("*", "\u204E")
                    .replace("?", "\uFF1F")
                    .replace("\"", "\u201D")
                    .replace("<", "\uFF1C")
                    .replace(">", "\uFF1E")
                    .replace("|", "\u23D0")
            }
                .take(120)
        }
        .fold(mutableListOf<String>()) { list, next ->
            if ((list.sumOf { it.length } + list.count()) < 850) {
                list.apply { add(next.take(120)) }
            } else list
        }
        .joinToString(
            separator = File.separator,
        ).ifBlank { "Unknown" } + "${File.separator}$entryId.jpg"
}