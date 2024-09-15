package com.thekeeperofpie.artistalleydatabase.apollo.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import com.apollographql.apollo3.api.Optional as ApolloOptional

class OptionalSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<ApolloOptional<T>> {
    override val descriptor = dataSerializer.descriptor

    private val serializer = Optional.serializer(dataSerializer)

    override fun deserialize(decoder: Decoder) =
        when (val optional = serializer.deserialize(decoder)) {
            Optional.Absent -> ApolloOptional.absent()
            is Optional.Present -> ApolloOptional.present(optional.value)
        }

    override fun serialize(encoder: Encoder, value: com.apollographql.apollo3.api.Optional<T>) {
        val optional = when (value) {
            com.apollographql.apollo3.api.Optional.Absent -> Optional.Absent
            is com.apollographql.apollo3.api.Optional.Present -> Optional.Present(value.value)
        }
        serializer.serialize(encoder, optional)
    }
}

/**
 * Duplicates the Apollo class so that the serializer doesn't have to be written manually.
 */
@Serializable
sealed class Optional<out V> {
    @Serializable
    data class Present<V>(val value: V) : Optional<V>()
    @Serializable
    data object Absent : Optional<Nothing>()
}
