package com.thekeeperofpie.artistalleydatabase.server

import com.thekeeperofpie.artistalleydatabase.server.AniListServer.graphQlModule
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PageQueryTest {

    @Test
    fun simpleMedia2() = testApplication {
        application {
            graphQlModule()
        }
        val response = client.post("/graphql") {
            contentType(ContentType.parse("application/json"))
            setBody(
                graphQlQuery(
                    """
                        {
                             Page(page: 1, perPage: 10) {
                                media(sort: [TRENDING_DESC], isAdult: true) {
                                    id
                                    title {
                                    userPreferred
                                    }
                                }
                            }
                        }
                    """
                )

            )
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""
            {
              "data": {
                "Page": {
                  "media": [
                    {
                      "id": 11,
                      "title": {
                        "userPreferred": "userPreferredTitle"
                      }
                    }
                  ]
                }
              }
            }
        """.trimIndent(), response.bodyAsText())
    }

    private fun graphQlQuery(@Language("GraphQL") query: String) =
        """
            {
                "query": "${query.trimIndent()}"
            }
        """.trimIndent()
}
