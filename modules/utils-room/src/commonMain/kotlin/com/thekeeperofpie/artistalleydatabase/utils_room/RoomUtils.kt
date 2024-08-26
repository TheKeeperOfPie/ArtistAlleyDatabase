package com.thekeeperofpie.artistalleydatabase.utils_room

import kotlinx.serialization.json.Json

object RoomUtils {

    fun wrapMatchQuery(query: String) = "*${query.replaceDoubleQuotes()}*"

    fun wrapLikeQuery(query: String) = "%${query.replace(Regex("\\s+"), "%").replaceDoubleQuotes()}%"

    private fun String.replaceDoubleQuotes(): String = replace(Regex.fromLiteral("\""), "\"\"")

    fun Boolean.toBit() = if (this) "1" else "0"

    suspend fun queryListStringColumn(
        query: String,
        matchFunction: suspend (String) -> List<String>,
        likeFunction: suspend (String) -> List<String>,
    ): List<String> {
        // TODO: Filter results so that only individual entries with the query are returned
        val matchQuery = wrapMatchQuery(query)
        val likeQuery = wrapLikeQuery(query)
        return tryReturnEmptyList(matchFunction, matchQuery)
            .asSequence()
            .plus(tryReturnEmptyList(matchFunction, matchQuery.lowercase()))
            .plus(tryReturnEmptyList(matchFunction, matchQuery.uppercase()))
            .plus(tryReturnEmptyList(likeFunction, likeQuery))
            .plus(tryReturnEmptyList(likeFunction, likeQuery.lowercase()))
            .plus(tryReturnEmptyList(likeFunction, likeQuery.uppercase()))
            .flatMap { Json.decodeFromString<List<String>>(it) }
            .distinct()
            .toList()
    }

    private suspend fun tryReturnEmptyList(
        function: suspend (String) -> List<String>,
        query: String,
    ) = try {
        function(query)
    } catch (ignored: Throwable) {
        emptyList()
    }
}
