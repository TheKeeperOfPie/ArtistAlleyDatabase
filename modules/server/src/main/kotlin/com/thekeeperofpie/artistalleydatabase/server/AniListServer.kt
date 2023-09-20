package com.thekeeperofpie.artistalleydatabase.server

import com.anilist.server.api.model.types.Media
import com.anilist.server.api.model.types.MediaTitle
import com.anilist.server.api.model.types.Page
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.GraphQLError
import graphql.execution.AsyncExecutionStrategy
import graphql.scalar.GraphqlStringCoercing
import graphql.schema.GraphQLScalarType
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import manifold.json.rt.Json
import manifold.json.rt.api.DataBindings
import java.io.InputStreamReader

object AniListServer {
    private val stringCoercer = GraphqlStringCoercing()

    fun Application.graphQlModule() {
        val graphQl = graphQl()
        routing {
            post("/graphql") {
                val body = call.receiveText()
                val json = Json.fromJson(body) as DataBindings
                val exec = ExecutionInput.newExecutionInput()
                    .query(json["query"] as String)
                    .variables(json["variables"] as? DataBindings ?: emptyMap())
                    .build()
                call.respondText(ContentType.parse("application/json")) {
                    executeRequest(graphQl, exec)
                }
            }
        }
    }

    private fun graphQl(): GraphQL {
        val typeDefinitionRegistry =
            AniListServer::class.java.classLoader.getResource("schema.graphqls")!!.openStream()
                .use {
                    InputStreamReader(it).use {
                        SchemaParser().parse(it)
                    }
                }
        val runtimeWiring = RuntimeWiring.newRuntimeWiring().apply {
            listOf("CountryCode", "FuzzyDateInt", "Json").forEach {
                scalar(
                    GraphQLScalarType.Builder()
                        .name(it)
                        .coercing(stringCoercer)
                        .build()
                )
            }

            listOf("NotificationUnion", "ActivityUnion", "LikeableUnion").forEach {
                type(it) {
                    it.typeResolver {
                        it.schema.getObjectType(it::class.simpleName)
                    }
                }
            }

            query {
                field("Page") {
                    Page()
                }
            }
            type<Page> {
                field("media") {
                    val isAdult = it.arguments["isAdult"] as? Boolean
                    listOf(
                        Media(
                            id = { 11 },
                            title = {
                                MediaTitle(
                                    userPreferred = { "userPreferredTitle" },
                                )
                            },
                            isAdult = { isAdult },
                        )
                    )
                }
            }
        }
            .build()
        val schema = SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)
        return GraphQL.newGraphQL(schema)
            .queryExecutionStrategy(AsyncExecutionStrategy())
            .build()
    }

    private fun executeRequest(graphQL: GraphQL, exec: ExecutionInput): String {
        val result = graphQL.execute(exec)
        val response = DataBindings()
        result.errors
            .takeIf { it.isNotEmpty() }
            ?.map(GraphQLError::toSpecification)
            ?.let { response["errors"] = it }
        result.getData<Any>()
            ?.let{ response["data"] = it }
        return Json.toJson(response)
    }
}
