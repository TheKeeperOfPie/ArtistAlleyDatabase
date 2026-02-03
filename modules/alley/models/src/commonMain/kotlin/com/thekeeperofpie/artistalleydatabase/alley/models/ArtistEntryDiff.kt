package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ArtistEntryDiff(
    val booth: String?,
    val name: String?,
    val summary: String?,
    val notes: String?,
    val socialLinks: HistoryListDiff?,
    val storeLinks: HistoryListDiff?,
    val portfolioLinks: HistoryListDiff?,
    val catalogLinks: HistoryListDiff?,
    val commissions: HistoryListDiff?,
    val seriesInferred: HistoryListDiff?,
    val seriesConfirmed: HistoryListDiff?,
    val merchInferred: HistoryListDiff?,
    val merchConfirmed: HistoryListDiff?,
    val formNotes: String,
    val timestamp: Instant,
)
