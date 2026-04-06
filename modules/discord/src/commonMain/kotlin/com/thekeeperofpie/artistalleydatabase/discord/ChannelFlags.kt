package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class ChannelFlags(val flags: Int) {
    constructor(vararg flags: ChannelFlag) : this(
        flags = flags.fold(0) { acc, value -> acc or value.flag }
    )
}

enum class ChannelFlag(val flag: Int) {
    PINNED(1 shl 1),
    REQUIRE_TAG(1 shl 4),
    HIDE_MEDIA_DOWNLOAD_OPTIONS(1 shl 15),
}
