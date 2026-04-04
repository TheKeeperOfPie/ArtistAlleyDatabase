package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable

@Serializable(with = InteractionCallbackType.Serializer::class)
enum class InteractionCallbackType(val value: Int) {
    PONG(1),
    CHANNEL_MESSAGE_WITH_SOURCE(4),
    DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE(5),
    DEFERRED_UPDATE_MESSAGE(6),
    UPDATE_MESSAGE(7),
    APPLICATION_COMMAND_AUTOCOMPLETE_RESULT(8),
    MODAL(9),
    PREMIUM_REQUIRED(10),
    LAUNCH_ACTIVITY(12),
    ;

    object Serializer :
        IntEnumSerializer<InteractionCallbackType>(
            entries = InteractionCallbackType.entries,
            serialName = "InteractionCallbackType",
            value = { it.value },
        )
}
