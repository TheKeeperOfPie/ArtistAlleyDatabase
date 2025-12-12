package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.ui.util.fastForEachReversed
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry

data class ArtistHistoryEntryWithDiff(
    val entry: ArtistHistoryEntry,
    val linksDiff: Diff?,
    val storeLinksDiff: Diff?,
    val catalogLinksDiff: Diff?,
    val commissionsDiff: Diff?,
    val seriesInferredDiff: Diff?,
    val seriesConfirmedDiff: Diff?,
    val merchInferredDiff: Diff?,
    val merchConfirmedDiff: Diff?,
) {
    companion object {
        fun calculateDiffs(entries: List<ArtistHistoryEntry>): List<ArtistHistoryEntryWithDiff> {
            val oldestEntry = entries.lastOrNull()
            var lastLinks = oldestEntry?.links.orEmpty()
            var lastStoreLinks = oldestEntry?.storeLinks.orEmpty()
            var lastCatalogLinks = oldestEntry?.catalogLinks.orEmpty()
            var lastCommissions = oldestEntry?.commissions.orEmpty()
            var lastSeriesInferred = oldestEntry?.seriesInferred.orEmpty()
            var lastSeriesConfirmed = oldestEntry?.seriesConfirmed.orEmpty()
            var lastMerchInferred = oldestEntry?.merchInferred.orEmpty()
            var lastMerchConfirmed = oldestEntry?.merchConfirmed.orEmpty()
            val results = mutableListOf<ArtistHistoryEntryWithDiff>()
            entries.fastForEachReversed {
                val linksDiff = diffList(lastLinks, it.links)
                val storeLinksDiff = diffList(lastStoreLinks, it.storeLinks)
                val catalogLinksDiff = diffList(lastCatalogLinks, it.catalogLinks)
                val commissionsDiff = diffList(lastCommissions, it.commissions)
                val seriesInferredDiff = diffList(lastSeriesInferred, it.seriesInferred)
                val seriesConfirmedDiff = diffList(lastSeriesConfirmed, it.seriesConfirmed)
                val merchInferredDiff = diffList(lastMerchInferred, it.merchInferred)
                val merchConfirmedDiff = diffList(lastMerchConfirmed, it.merchConfirmed)
                results += ArtistHistoryEntryWithDiff(
                    entry = it,
                    linksDiff = linksDiff,
                    storeLinksDiff = storeLinksDiff,
                    catalogLinksDiff = catalogLinksDiff,
                    commissionsDiff = commissionsDiff,
                    seriesInferredDiff = seriesInferredDiff,
                    seriesConfirmedDiff = seriesConfirmedDiff,
                    merchInferredDiff = merchInferredDiff,
                    merchConfirmedDiff = merchConfirmedDiff,
                )

                lastLinks = it.links ?: lastLinks
                lastStoreLinks = it.storeLinks ?: lastStoreLinks
                lastCatalogLinks = it.catalogLinks ?: lastCatalogLinks
                lastCommissions = it.commissions ?: lastCommissions
                lastSeriesInferred = it.seriesInferred ?: lastSeriesInferred
                lastSeriesConfirmed = it.seriesConfirmed ?: lastSeriesConfirmed
                lastMerchInferred = it.merchInferred ?: lastMerchInferred
                lastMerchConfirmed = it.merchConfirmed ?: lastMerchConfirmed
            }

            // Reverse again re-order reverse chronologically
            return results.reversed()
        }

        private fun diffList(previous: List<String>, next: List<String>?) = if (next == null) {
            null
        } else {
            Diff(
                added = next - previous.toSet(),
                deleted = previous - next.toSet(),
            )
        }
    }

    data class Diff(
        val added: List<String>,
        val deleted: List<String>,
    )
}
