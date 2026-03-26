package com.thekeeperofpie.artistalleydatabase.alley.discord.models

import kotlinx.serialization.Serializable

@Serializable(with = OptionType.Serializer::class)
internal enum class OptionType(val value: Int) {
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

    object Serializer :
        IntEnumSerializer<OptionType>(
            entries = OptionType.entries,
            serialName = "OptionType",
            value = { it.value },
        )
}
