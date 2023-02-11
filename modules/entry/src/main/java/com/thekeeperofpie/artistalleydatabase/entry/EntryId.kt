package com.thekeeperofpie.artistalleydatabase.entry

data class EntryId(
    val type: String,
    val valueId: String,
) {
    val scopedId by lazy { "${type}_$valueId" }

    override fun toString() = throw IllegalStateException(
        "$scopedId should not be serialized toString, use valueId manually"
    )
}