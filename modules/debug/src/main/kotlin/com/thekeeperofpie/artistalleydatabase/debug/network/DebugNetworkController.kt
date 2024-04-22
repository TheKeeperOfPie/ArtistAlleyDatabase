package com.thekeeperofpie.artistalleydatabase.debug.network

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.network_utils.RateLimitUtils
import graphql.language.AstPrinter
import graphql.parser.Parser
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okio.Buffer
import java.nio.charset.Charset
import java.time.Instant
import java.util.UUID

class DebugNetworkController(scopedApplication: ScopedApplication) {

    companion object {
        private const val TAG = "DebugNetworkController"
    }

    val apolloHttpInterceptor = object : HttpInterceptor {
        override suspend fun intercept(
            request: HttpRequest,
            chain: HttpInterceptorChain,
        ): HttpResponse {
            val requestTimestamp = Instant.now()
            val id = UUID.randomUUID().toString()
            graphQlRequests.trySend(
                GraphQlRequest(
                    id = id,
                    timestamp = requestTimestamp,
                    request = request,
                )
            )

            val response = RateLimitUtils.rateLimit(request, chain) { (response, _) ->
                val responseBody = response.body
                val responseBodyString =
                    responseBody?.readString(Charset.defaultCharset()).toString()
                graphQlResponses.trySend(
                    GraphQlResponse(
                        id = id,
                        headers = response.headers.associate { it.name to it.value },
                        bodyString = responseBodyString,
                        timestamp = Instant.now(),
                        final = false,
                    )
                )
            }

            val responseBody = response.body
            val responseBodyString = responseBody?.readString(Charset.defaultCharset()).toString()
            graphQlResponses.trySend(
                GraphQlResponse(
                    id = id,
                    headers = response.headers.associate { it.name to it.value },
                    bodyString = responseBodyString,
                    timestamp = Instant.now(),
                    final = true,
                )
            )

            return if (responseBody == null) {
                response
            } else {
                val buffer = Buffer()
                buffer.writeString(responseBodyString, Charset.defaultCharset())
                HttpResponse.Builder(statusCode = response.statusCode)
                    .addHeaders(response.headers)
                    .body(buffer)
                    .build()
            }
        }
    }

    val graphQlData = mutableStateListOf<GraphQlData>()
    private val graphQlDataMutex = Mutex()

    private val graphQlRequests = Channel<GraphQlRequest>(
        capacity = 20,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

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
            for (graphQlRequest in graphQlRequests) {
                val requestBodyBuffer = Buffer()
                graphQlRequest.request.body?.writeTo(requestBodyBuffer)
                val requestBodyString = requestBodyBuffer.readByteString().utf8()
                val queryBody = json.decodeFromString<GraphQlQueryBody>(requestBodyString)
                val operationName = queryBody.operationName.orEmpty()
                val query = AstPrinter.printAst(Parser().parseDocument(queryBody.query))
                val variablesJson = queryBody.variables?.let(json::encodeToString).orEmpty()
                graphQlDataMutex.withLock {
                    graphQlData += GraphQlData(
                        id = graphQlRequest.id,
                        request = GraphQlData.Request(
                            timestamp = graphQlRequest.timestamp,
                            operationName = operationName,
                            query = query,
                            variablesJson = variablesJson,
                        )
                    )
                }
            }
        }

        scopedApplication.scope.launch(CustomDispatchers.IO) {
            for (graphQlResponse in graphQlResponses) {
                val responseBody = graphQlResponse.bodyString.takeIf(String::isNotBlank)
                    ?.let<String, GraphQlResponseBody>(json::decodeFromString)
                val responseJson = GraphQlResponseWithHeaders(
                    headers = graphQlResponse.headers,
                    body = responseBody?.data,
                ).let(json::encodeToString)
                val errors = responseBody?.errors?.map(json::encodeToString).orEmpty()
                graphQlDataMutex.withLock {
                    val index = graphQlData.indexOfFirst { it.id == graphQlResponse.id }
                    if (index == -1) {
                        Log.d(TAG, "Error associating response with request: $graphQlResponse")
                        return@withLock
                    }
                    graphQlData[index] = graphQlData[index].copy(
                        response = GraphQlData.Response(
                            timestamp = graphQlResponse.timestamp,
                            bodyJson = responseJson,
                            errors = errors,
                            final = graphQlResponse.final,
                        )
                    )
                }
            }
        }
    }

    fun clear() = graphQlData.clear()

    data class GraphQlRequest(
        val id: String,
        val timestamp: Instant,
        val request: HttpRequest,
    )

    data class GraphQlResponse(
        val id: String,
        val timestamp: Instant,
        val headers: Map<String, String>,
        val bodyString: String,
        val final: Boolean,
    )

    @Serializable
    data class GraphQlResponseWithHeaders(
        val headers: Map<String, String>,
        val body: JsonObject?,
    )

    data class GraphQlData(
        val id: String,
        val request: Request,
        val response: Response? = null,
    ) {
        data class Request(
            val timestamp: Instant,
            val operationName: String,
            val query: String,
            val variablesJson: String,
        )

        data class Response(
            val timestamp: Instant,
            val bodyJson: String,
            val errors: List<String>,
            val final: Boolean,
        )
    }

    @Serializable
    data class GraphQlQueryBody(
        val operationName: String? = null,
        val variables: JsonObject? = null,
        val query: String? = null,
    )

    @Serializable
    data class GraphQlResponseBody(
        val data: JsonObject? = null,
        val errors: List<JsonObject> = emptyList(),
    )
}
