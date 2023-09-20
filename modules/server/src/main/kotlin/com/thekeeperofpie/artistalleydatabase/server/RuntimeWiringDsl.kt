package com.thekeeperofpie.artistalleydatabase.server

import graphql.schema.AsyncDataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.PropertyDataFetcher
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring

internal fun RuntimeWiring.Builder.query(block: TypeRuntimeWiring.Builder.() -> Unit) =
    TypeRuntimeWiring.newTypeWiring("Query")
        .apply(block)
        .build()
        .let(::type)!!

inline fun <reified T> RuntimeWiring.Builder.type(block: TypeRuntimeWiring.Builder.() -> Unit) =
    TypeRuntimeWiring.newTypeWiring(T::class.simpleName)
        .apply(block)
        .build()
        .let(::type)!!

internal fun <T> TypeRuntimeWiring.Builder.field(
    name: String,
    block: (DataFetchingEnvironment) -> T?,
) = dataFetcher(name, AsyncDataFetcher<T> {
    val source = it.getSource<Any?>() ?: return@AsyncDataFetcher block(it)
    try {
        @Suppress("UNCHECKED_CAST")
        PropertyDataFetcher.fetching<Any>(it.field.name)
            .get(it.fieldDefinition, source) { it } as T
    } catch (exception: IllegalStateException) {
        if (exception.message?.endsWith("was not requested") != true) {
            throw exception
        } else {
            block(it)
        }
    }
})!!
