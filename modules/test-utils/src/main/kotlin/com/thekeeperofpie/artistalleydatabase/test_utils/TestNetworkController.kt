package com.thekeeperofpie.artistalleydatabase.test_utils

import com.thekeeperofpie.artistalleydatabase.server.AniListServer.graphQlModule
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.server.testing.TestApplication
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.buffer
import okio.sink
import java.io.ByteArrayOutputStream

object TestNetworkController {

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor {
            runBlocking {
                val request = it.request()
                val response = testClient.request {
                    url(request.url.toUrl())
                    method = HttpMethod.parse(request.method)
                    headers {
                        request.headers.toMultimap().forEach {
                            appendAll(it.key, it.value)
                        }
                    }
                    ByteArrayOutputStream().use {
                        it.sink().use {
                            it.buffer().use {
                                request.body?.writeTo(it)
                            }
                        }
                        setBody(it.toByteArray())
                    }
                }

                Response.Builder()
                    .request(request)
                    .code(response.status.value)
                    .message(response.status.description)
                    .body(
                        response.bodyAsText()
                            .toResponseBody(response.contentType()?.contentType?.toMediaTypeOrNull())
                    )
                    .build()
            }
        }
        .build()

    private lateinit var testApplication: TestApplication
    private lateinit var testClient: HttpClient

    fun initialize() {
        testApplication = TestApplication {
            application {
                graphQlModule()
            }
        }
        testApplication.start()
        testClient = testApplication.client

    }

    fun destroy() {
        testApplication.stop()
    }
}
