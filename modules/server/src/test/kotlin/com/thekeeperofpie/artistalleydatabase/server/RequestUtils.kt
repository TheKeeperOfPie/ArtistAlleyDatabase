package com.thekeeperofpie.artistalleydatabase.server

import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.api.composeJsonRequest
import com.apollographql.apollo3.api.json.BufferedSinkJsonWriter
import com.apollographql.apollo3.api.json.jsonReader
import com.apollographql.apollo3.api.parseResponse
import com.google.common.truth.Truth.assertThat
import com.thekeeperofpie.artistalleydatabase.server.AniListServer.graphQlModule
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import okio.Buffer
import okio.buffer
import okio.sink
import org.intellij.lang.annotations.Language
import java.io.ByteArrayOutputStream

internal object RequestUtils {

    @OptIn(ApolloExperimental::class)
    fun <T : Query.Data> executeQuery(query: Query<T>): T {
        var result: ApolloResponse<T>? = null
        testApplication {
            application {
                graphQlModule()
            }
            ByteArrayOutputStream().use {
                it.sink().use {
                    it.buffer().use {
                        BufferedSinkJsonWriter(it).use {
                            query.composeJsonRequest(it)
                        }
                    }
                }
                val response = client.post("/graphql") {
                    contentType(ContentType.parse("application/json"))
                    setBody(String(it.toByteArray()))
                }
                result = query.parseResponse(Buffer().writeUtf8(response.bodyAsText()).jsonReader())
            }
        }

        assertThat(result).isNotNull()
        assertThat(result!!.errors.orEmpty()).isEmpty()
        return result!!.dataOrThrow()
    }

    private fun graphQlQuery(@Language("GraphQL") query: String) =
        """
            {
                "query": "${query.trimIndent()}"
            }
        """.trimIndent()
}
