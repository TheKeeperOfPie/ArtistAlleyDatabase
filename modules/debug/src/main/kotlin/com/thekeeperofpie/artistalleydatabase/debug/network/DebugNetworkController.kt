package com.thekeeperofpie.artistalleydatabase.debug.network

import androidx.compose.runtime.mutableStateListOf
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import graphql.language.AstPrinter
import graphql.parser.Parser
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okio.Buffer
import java.nio.charset.Charset
import java.time.Instant
import java.util.UUID

class DebugNetworkController(scopedApplication: ScopedApplication) {

    val apolloHttpInterceptor = object : HttpInterceptor {
        override suspend fun intercept(
            request: HttpRequest,
            chain: HttpInterceptorChain,
        ): HttpResponse {
            val requestTimestamp = Instant.now()
            return chain.proceed(request).let {
                val responseBody = it.body
                val responseBodyString =
                    responseBody?.readString(Charset.defaultCharset()).toString()
                graphQlResponses.trySend(
                    GraphQlResponse(
                        requestTimestamp = requestTimestamp,
                        request = request,
                        responseHeaders = it.headers.associate { it.name to it.value },
                        responseBodyString = responseBodyString,
                        responseTimestamp = Instant.now(),
                    )
                )

                if (responseBody == null) {
                    it
                } else {
                    val buffer = Buffer()
                    buffer.writeString(responseBodyString, Charset.defaultCharset())
                    HttpResponse.Builder(statusCode = it.statusCode)
                        .addHeaders(it.headers)
                        .body(buffer)
                        .build()
                }
            }
        }
    }

    val graphQlData = mutableStateListOf<GraphQlData>()

    private val graphQlResponses = Channel<GraphQlResponse>(
        capacity = 20,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    init {
        scopedApplication.scope.launch(CustomDispatchers.IO) {
            for (graphQlResponse in graphQlResponses) {
                val (requestTimestamp, request, responseTimestamp, responseHeaders, responseBodyString) = graphQlResponse
                val requestBodyBuffer = Buffer()
                request.body?.writeTo(requestBodyBuffer)
                val requestBodyString = requestBodyBuffer.readByteString().utf8()
                val queryBody = json.decodeFromString<GraphQlQueryBody>(requestBodyString)

                val responseBody = responseBodyString.takeIf(String::isNotBlank)
                    ?.let<String, GraphQlResponseBody>(json::decodeFromString)
                val responseJson = GraphQlResponseWithHeaders(
                    headers = responseHeaders,
                    body = responseBody?.data,
                ).let(json::encodeToString)

                graphQlData += GraphQlData(
                    requestTimestamp = requestTimestamp,
                    responseTimestamp = responseTimestamp,
                    operationName = queryBody.operationName.orEmpty(),
                    query = AstPrinter.printAst(Parser().parseDocument(queryBody.query)),
                    variablesJson = queryBody.variables?.let(json::encodeToString).orEmpty(),
                    responseJson = responseJson,
                    errors = responseBody?.errors.orEmpty(),
                )
            }
        }
    }

    fun clear() = graphQlData.clear()

    data class GraphQlResponse(
        val requestTimestamp: Instant,
        val request: HttpRequest,
        val responseTimestamp: Instant,
        val responseHeaders: Map<String, String>,
        val responseBodyString: String,
    )

    @Serializable
    data class GraphQlResponseWithHeaders(
        val headers: Map<String, String>,
        val body: JsonObject?,
    )

    data class GraphQlData(
        val requestTimestamp: Instant,
        val responseTimestamp: Instant,
        val operationName: String,
        val query: String,
        val variablesJson: String,
        val responseJson: String,
        val errors: List<String>,
        val id: String = UUID.randomUUID().toString(),
    )

    @Serializable
    data class GraphQlQueryBody(
        val operationName: String? = null,
        val variables: JsonObject? = null,
        val query: String? = null,
    )

    @Serializable
    data class GraphQlResponseBody(
        val data: JsonObject? = null,
        val errors: List<String> = emptyList(),
    )
}
