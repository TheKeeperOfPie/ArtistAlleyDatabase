package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class StampRallyEntryDiff(
    val id: String,
    val fandom: String?,
    val tables: ListDiff<String>?,
    val links: ListDiff<String>?,
    val tableMin: TableMin?,
    val prize: String?,
    val prizeLimit: Long?,
    val series: ListDiff<String>?,
    val merch: ListDiff<String>?,
    val notes: String?,
    val deleted: Boolean,
    val timestamp: Instant,
)
