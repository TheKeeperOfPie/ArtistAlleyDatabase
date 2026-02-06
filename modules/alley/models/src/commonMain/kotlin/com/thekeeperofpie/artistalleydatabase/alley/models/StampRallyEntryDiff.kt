package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import kotlinx.serialization.Serializable

@Serializable
data class StampRallyEntryDiff(
    val id: String,
    val fandom: String?,
    val tables: HistoryListDiff?,
    val links: HistoryListDiff?,
    val tableMin: TableMin?,
    val prize: String?,
    val prizeLimit: Long?,
    val series: HistoryListDiff?,
    val merch: HistoryListDiff?,
    val notes: String?,
)
