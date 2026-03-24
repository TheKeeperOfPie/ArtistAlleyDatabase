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
        val choices: List<String>? = null,
    ) {

        @Serializable(with = OptionType.Serializer::class)
        enum class OptionType(val value: Int) {
            SUB_COMMAND(1),
            SUB_COMMAND_GROUP(2),
            STRING(3),
            INTEGER(4),
            BOOLEAN(5),
            USER(6),
            CHANNEL(7),
            ROLE(8),
            MENTIONABLE(9),
            NUMBER(10),
            ATTACHMENT(11),
            ;

            internal object Serializer :
                IntEnumSerializer<OptionType>(
                    entries = OptionType.entries,
                    serialName = "OptionType",
                    value = { it.value },
                )
        }
    }
}
