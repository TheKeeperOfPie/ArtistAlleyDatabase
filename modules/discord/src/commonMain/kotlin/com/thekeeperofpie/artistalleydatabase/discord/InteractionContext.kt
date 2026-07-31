package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable

@Serializable(with = InteractionContext.Serializer::class)
enum class InteractionContext(val value: Int) {
    GUILD(0),
    BOT_DM(1),
    PRIVATE_CHANNEL(2),
    ;

    object Serializer :
        IntEnumSerializer<InteractionContext>(
            entries = InteractionContext.entries,
            serialName = "InteractionContext",
            value = { it.value },
        )
}
