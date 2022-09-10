package com.thekeeperofpie.artistalleydatabase.json_schema.models

import com.thekeeperofpie.artistalleydatabase.json_schema.Converters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
sealed class JsonSchemaType {
    abstract val title: String

    @Serializable
    @SerialName("number")
    data class Number(
        override val title: String = "",
    ) : JsonSchemaType()

    @Serializable
    @SerialName("integer")
    data class Integer(
        override val title: String = "",
    ) : JsonSchemaType()

    @Serializable
    @SerialName("string")
    data class StringType(
        override val title: String = "",
        val enum: Set<String> = emptySet(),
        val example: String? = null,
        @Serializable(Converters.RegexConverter::class)
        val pattern: Regex? = null,
    ) : JsonSchemaType()

    @Serializable
    @SerialName("array")
    data class Array(
        override val title: String = "",
        val items: JsonElement? = null,
    ) : JsonSchemaType()

    @Serializable
    @SerialName("object")
    data class Object(
        override val title: String = "",
        val properties: Map<String, JsonElement> = emptyMap(),
        val patternProperties: Map<String, JsonElement> = emptyMap(),
        val allOf: List<JsonElement> = emptyList(),
        val anyOf: List<JsonElement> = emptyList(),
        val required: List<String> = emptyList(),
    ) : JsonSchemaType()

    @Serializable
    class Unknown(
        val anyOf: List<JsonElement> = emptyList(),
    ) : JsonSchemaType() {
        override val title: String = ""
    }
}