package com.thekeeperofpie.artistalleydatabase.web_infra

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class WebApi(val json: Json, val baseUrl: String) {

    suspend inline fun get(
        basePath: String,
        uriParameters: Map<String, String?>,
        queryParameters: Map<String, String?>,
    ): InputStream = withContext(Dispatchers.IO) {
        var path = basePath
        uriParameters.filterValues { it != null }
            .forEach { (name, value) ->
                path = path.replace(
                    "{$name}",
                    URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                )
            }
        val url = "$baseUrl$path?format=json" + queryParameters.filterValues { it != null }
            .entries
            .joinToString(
                separator = "&",
                transform = { (key, value) ->
                    "$key=${URLEncoder.encode(value, StandardCharsets.UTF_8.toString())}"
                }
            )

        URL(url).openStream()
    }

    suspend fun getString(
        basePath: String,
        uriParameters: Map<String, String?> = emptyMap(),
        queryParameters: Map<String, String?> = emptyMap(),
    ) = get(
        basePath = basePath,
        uriParameters = uriParameters,
        queryParameters = queryParameters
    ).use {
        it.bufferedReader().use {
            it.readText()
        }
    }

    suspend inline fun <reified T> getObject(
        basePath: String,
        uriParameters: Map<String, String?> = emptyMap(),
        queryParameters: Map<String, String?> = emptyMap(),
    ): T {
        @Suppress("OPT_IN_USAGE")
        return get(
            basePath = basePath,
            uriParameters = uriParameters,
            queryParameters = queryParameters
        ).use(json::decodeFromStream)
    }
}