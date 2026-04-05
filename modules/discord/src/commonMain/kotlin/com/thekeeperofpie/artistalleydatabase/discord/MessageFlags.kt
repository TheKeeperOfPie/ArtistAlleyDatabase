package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class MessageFlags(val flags: Int) {
    constructor(vararg flags: MessageFlag) : this(
        flags = flags.fold(0) { acc, value -> acc or value.flag }
    )
}

enum class MessageFlag(val flag: Int) {
    CROSSPOSTED(1 shl 0),
    IS_CROSSPOST(1 shl 1),
    SUPPRESS_EMBEDS(1 shl 2),
    SOURCE_MESSAGE_DELETED(1 shl 3),
    URGENT(1 shl 4),
    HAS_THREAD(1 shl 5),
    EPHEMERAL(1 shl 6),
    LOADING(1 shl 7),
    FAILED_TO_MENTION_SOME_ROLES_IN_THREAD(1 shl 8),
    SUPPRESS_NOTIFICATIONS(1 shl 12),
    IS_VOICE_MESSAGE(1 shl 13),
    HAS_SNAPSHOT(1 shl 14),
    IS_COMPONENTS_V2(1 shl 15),
}
