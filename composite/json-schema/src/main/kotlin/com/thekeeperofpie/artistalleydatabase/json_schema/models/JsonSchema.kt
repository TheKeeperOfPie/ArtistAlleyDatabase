package com.thekeeperofpie.artistalleydatabase.json_schema.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import org.gradle.configurationcache.extensions.capitalized
import java.net.URI

@Serializable
data class JsonSchema(
    @SerialName("\$schema")
    val schema: String,
    val title: String = "",
    val definitions: Map<String, JsonSchemaType> = emptyMap(),
    val properties: Map<String, JsonElement> = emptyMap(),
) {
    @Transient
    lateinit var url: String

    val name by lazy {
        URI.create(url).path
            .substringAfterLast("/")
            .removeSuffix(".json")
            .filter(Char::isLetterOrDigit)
            .capitalized()
    }
}