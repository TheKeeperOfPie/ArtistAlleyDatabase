package com.thekeeperofpie.artistalleydatabase.android_utils

import java.util.Locale

object RoomUtils {

    fun wrapMatchQuery(query: String) = "*$query*"
    fun wrapLikeQuery(query: String) = "%${query.replace(Regex("\\s+"), "%")}%"

    fun Boolean.toBit() = if (this) "1" else "0"

    suspend fun queryListStringColumn(
        query: String,
        matchFunction: suspend (String) -> List<String>,
        likeFunction: suspend (String) -> List<String>,
    ): List<String> {
        // TODO: Filter results so that only individual entries with the query are returned
        val matchQuery = wrapMatchQuery(query)
        val likeQuery = wrapLikeQuery(query)
        return matchFunction(matchQuery)
            .plus(matchFunction(matchQuery.lowercase(Locale.getDefault())))
            .plus(matchFunction(matchQuery.uppercase(Locale.getDefault())))
            .plus(likeFunction(likeQuery))
            .plus(likeFunction(likeQuery.lowercase(Locale.getDefault())))
            .plus(likeFunction(likeQuery.uppercase(Locale.getDefault())))
            .flatMap(JsonUtils::readStringList)
            .distinct()
    }
}