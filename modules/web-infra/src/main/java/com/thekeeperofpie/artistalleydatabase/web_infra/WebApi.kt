package com.thekeeperofpie.artistalleydatabase.web_infra

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class WebApi(val json: Json, val baseUrl: String) {

    fun get(
        basePath: String,
        uriParameters: Map<String, String?>,
        queryParameters: Map<String, String?>,
    ): URL {
        var path = basePath
        uriParameters.filterValues { it != null }
            .forEach { (name, value) ->
                path = path.replace(
                    "{$name}",
                    URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                )
            }
        val url = "$baseUrl$path?" + (queryParameters + ("format" to "json"))
            .filterValues { it != null }
            .entries
            .joinToString(
                separator = "&",
                transform = { (key, value) ->
                    "$key=${URLEncoder.encode(value, StandardCharsets.UTF_8.toString())}"
                }
            )

        return URL(url)
    }

    suspend fun getString(
        basePath: String,
        uriParameters: Map<String, String?> = emptyMap(),
        queryParameters: Map<String, String?> = emptyMap(),
    ) = withContext(Dispatchers.IO) {
        get(
            basePath = basePath,
            uriParameters = uriParameters,
            queryParameters = queryParameters
        ).readText()
    }

    suspend inline fun <reified T> getObject(
        basePath: String,
        uriParameters: Map<String, String?> = emptyMap(),
        queryParameters: Map<String, String?> = emptyMap(),
    ): T = withContext(Dispatchers.IO) {
        @Suppress("OPT_IN_USAGE")
        get(
            basePath = basePath,
            uriParameters = uriParameters,
            queryParameters = queryParameters
        )
            .openStream()
            .buffered()
            .let(json::decodeFromStream)
    }
}