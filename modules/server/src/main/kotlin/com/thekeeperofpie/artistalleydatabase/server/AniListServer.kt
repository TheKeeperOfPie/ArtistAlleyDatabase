package com.thekeeperofpie.artistalleydatabase.server

import com.anilist.data.server.api.model.types.Page
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

fun main() {
}

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
        val runtimeWiring = wiring {
            query {
                field("Page") {
                    Page()
                }
            }
            type<Page> {
                field("media") {
                    listOf(MockMedia.generate(it.arguments))
                }
            }
        }
            .build()
        val schema = SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)
        return GraphQL.newGraphQL(schema)
            .queryExecutionStrategy(AsyncExecutionStrategy())
            .build()
    }

    private fun wiring(block: RuntimeWiring.Builder.() -> Unit) =
        RuntimeWiring.newRuntimeWiring().apply {
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

            block()
        }

    private fun executeRequest(graphQL: GraphQL, request: ExecutionInput): String {
        val result = graphQL.execute(request)
        val response = DataBindings()
        result.errors
            .takeIf { it.isNotEmpty() }
            ?.map(GraphQLError::toSpecification)
            ?.let { response["errors"] = it }
        result.getData<Any>()
            ?.let { response["data"] = it }
        return Json.toJson(response)
    }
}
