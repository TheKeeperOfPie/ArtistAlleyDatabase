package com.thekeeperofpie.artistalleydatabase.alley.discord.models

import kotlinx.serialization.Serializable

@Serializable(with = InteractionType.Serializer::class)
internal enum class InteractionType(val value: Int) {
    PING(1),
    APPLICATION_COMMAND(2),
    MESSAGE_COMPONENT(3),
    APPLICATION_COMMAND_AUTOCOMPLETE(4),
    MODAL_SUBMIT(5),
    ;

    internal object Serializer :
        IntEnumSerializer<InteractionType>(
            entries = InteractionType.entries,
            serialName = "InteractionType",
            value = { it.value },
        )
}
