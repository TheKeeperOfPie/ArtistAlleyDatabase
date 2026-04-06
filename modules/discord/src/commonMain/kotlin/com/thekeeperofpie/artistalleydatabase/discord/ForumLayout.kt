package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable

@Serializable(with = ForumLayout.Serializer::class)
enum class ForumLayout(val value: Int) {
    NOT_SET(0),
    LIST_VIEW(1),
    GALLERY_VIEW(2),
    ;

    object Serializer :
        IntEnumSerializer<ForumLayout>(
            entries = ForumLayout.entries,
            serialName = "ForumLayout",
            value = { it.value },
        )
}
