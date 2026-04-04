package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = MessageComponent.Serializer::class)
sealed interface MessageComponent {

    val type: Int

    @Serializable
    data class ActionRow(
        override val type: Int,
        val components: List<MessageComponent>,
    ) : MessageComponent {
        constructor(vararg components: MessageComponent) : this(
            type = 1,
            components = components.toList(),
        )
    }

    @Serializable
    data class Button(
        override val type: Int,
        val style: Style,
        val label: String,
        val url: String? = null,
    ) : MessageComponent {
        constructor(
            style: Style,
            label: String,
            url: String? = null,
        ) : this(
            type = 2,
            style = style,
            label = label,
            url = url,
        )

        @Serializable(Style.Serializer::class)
        enum class Style(val value: Int) {
            PRIMARY(1),
            SECONDARY(2),
            SUCCESS(3),
            DANGER(4),
            LINK(5),
            PREMIUM(6),
            ;

            object Serializer : IntEnumSerializer<Style>(
                entries = Style.entries,
                serialName = "MessageComponent.Button.Style",
                value = { it.value },
            )
        }
    }

    object Serializer :
        JsonContentPolymorphicSerializer<MessageComponent>(MessageComponent::class) {
        override fun selectDeserializer(element: JsonElement) =
            when (val type = element.jsonObject["type"]?.jsonPrimitive?.intOrNull) {
                1 -> ActionRow.serializer()
                2 -> Button.serializer()
                else -> throw SerializationException("Failed to deserialize $type")
            }
    }
}
