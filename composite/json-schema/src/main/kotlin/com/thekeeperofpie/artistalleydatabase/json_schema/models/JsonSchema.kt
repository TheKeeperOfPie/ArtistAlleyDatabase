package com.thekeeperofpie.artistalleydatabase.json_schema.models

import com.thekeeperofpie.artistalleydatabase.composite.utils.Utils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

@Serializable
data class JsonSchema(
    @SerialName("\$schema")
    val schema: String,
    val title: String = "",
    val definitions: Map<String, JsonSchemaType> = emptyMap(),
    val properties: Map<String, JsonElement> = emptyMap(),
    val required: List<String> = emptyList(),
) {
    @Transient
    lateinit var url: String

    val name by lazy { Utils.jsonUrlToClassName(url) }
}