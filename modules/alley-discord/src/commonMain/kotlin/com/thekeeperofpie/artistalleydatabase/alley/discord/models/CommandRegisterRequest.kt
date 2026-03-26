package com.thekeeperofpie.artistalleydatabase.alley.discord.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CommandRegisterRequest(
    val name: String,
    val type: CommandType,
    val description: String,
    val options: List<Option>? = null,
) {
    @Serializable(with = CommandType.Serializer::class)
    enum class CommandType(val value: Int) {
        CHAT_INPUT(1),
        USER(2),
        MESSAGE(3),
        PRIMARY_ENTRY_POINT(4),
        ;

        internal object Serializer :
            IntEnumSerializer<CommandType>(
                entries = CommandType.entries,
                serialName = "CommandType",
                value = { it.value },
            )
    }

    @Serializable
    data class Option(
        val name: String,
        val type: OptionType,
        val description: String,
        val required: Boolean = false,
        val choices: List<Choice>? = null,
    ) {
        @Serializable
        data class Choice(
            val name: String,
            val value: String,
        )
    }
}
