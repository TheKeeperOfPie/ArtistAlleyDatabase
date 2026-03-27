package com.thekeeperofpie.artistalleydatabase.alley.discord.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = InteractionRequestData.Serializer::class)
internal sealed interface InteractionRequestData {

    val type: Int

    @Serializable
    data class SlashCommand(
        override val type: Int = 1,
        val name: String,
        val options: List<Option>? = null,
    ) : InteractionRequestData {

        @Serializable
        data class Option(
            val type: OptionType,
            val name: String,
            val value: String? = null,
            val options: List<Option>? = null,
        )
    }

    object Serializer :
        JsonContentPolymorphicSerializer<InteractionRequestData>(InteractionRequestData::class) {
        override fun selectDeserializer(element: JsonElement) =
            when (val type = element.jsonObject["type"]?.jsonPrimitive?.intOrNull) {
                1 -> SlashCommand.serializer()
                else -> throw SerializationException("Failed to deserialize $type")
            }
    }
}
