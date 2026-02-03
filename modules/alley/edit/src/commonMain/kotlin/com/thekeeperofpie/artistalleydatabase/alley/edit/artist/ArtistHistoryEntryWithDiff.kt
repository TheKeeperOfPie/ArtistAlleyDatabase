package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.ui.util.fastForEachReversed
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.HistoryListDiff

data class ArtistHistoryEntryWithDiff(
    val entry: ArtistHistoryEntry,
    val socialLinksDiff: HistoryListDiff?,
    val storeLinksDiff: HistoryListDiff?,
    val portfolioLinksDiff: HistoryListDiff?,
    val catalogLinksDiff: HistoryListDiff?,
    val commissionsDiff: HistoryListDiff?,
    val seriesInferredDiff: HistoryListDiff?,
    val seriesConfirmedDiff: HistoryListDiff?,
    val merchInferredDiff: HistoryListDiff?,
    val merchConfirmedDiff: HistoryListDiff?,
) {
    companion object {
        fun calculateDiffs(entries: List<ArtistHistoryEntry>): List<ArtistHistoryEntryWithDiff> {
            val oldestEntry = entries.lastOrNull()
            var lastSocialLinks = oldestEntry?.socialLinks.orEmpty()
            var lastStoreLinks = oldestEntry?.storeLinks.orEmpty()
            var lastPortfolioLinks = oldestEntry?.portfolioLinks.orEmpty()
            var lastCatalogLinks = oldestEntry?.catalogLinks.orEmpty()
            var lastCommissions = oldestEntry?.commissions.orEmpty()
            var lastSeriesInferred = oldestEntry?.seriesInferred.orEmpty()
            var lastSeriesConfirmed = oldestEntry?.seriesConfirmed.orEmpty()
            var lastMerchInferred = oldestEntry?.merchInferred.orEmpty()
            var lastMerchConfirmed = oldestEntry?.merchConfirmed.orEmpty()
            val results = mutableListOf<ArtistHistoryEntryWithDiff>()
            entries.fastForEachReversed {
                val socialLinksDiff = HistoryListDiff.diffList(lastSocialLinks, it.socialLinks)
                val storeLinksDiff = HistoryListDiff.diffList(lastStoreLinks, it.storeLinks)
                val portfolioLinksDiff = HistoryListDiff.diffList(lastPortfolioLinks, it.portfolioLinks)
                val catalogLinksDiff = HistoryListDiff.diffList(lastCatalogLinks, it.catalogLinks)
                val commissionsDiff = HistoryListDiff.diffList(lastCommissions, it.commissions)
                val seriesInferredDiff = HistoryListDiff.diffList(lastSeriesInferred, it.seriesInferred)
                val seriesConfirmedDiff = HistoryListDiff.diffList(lastSeriesConfirmed, it.seriesConfirmed)
                val merchInferredDiff = HistoryListDiff.diffList(lastMerchInferred, it.merchInferred)
                val merchConfirmedDiff = HistoryListDiff.diffList(lastMerchConfirmed, it.merchConfirmed)
                results += ArtistHistoryEntryWithDiff(
                    entry = it,
                    socialLinksDiff = socialLinksDiff,
                    storeLinksDiff = storeLinksDiff,
                    portfolioLinksDiff = portfolioLinksDiff,
                    catalogLinksDiff = catalogLinksDiff,
                    commissionsDiff = commissionsDiff,
                    seriesInferredDiff = seriesInferredDiff,
                    seriesConfirmedDiff = seriesConfirmedDiff,
                    merchInferredDiff = merchInferredDiff,
                    merchConfirmedDiff = merchConfirmedDiff,
                )

                lastSocialLinks = it.socialLinks ?: lastSocialLinks
                lastStoreLinks = it.storeLinks ?: lastStoreLinks
                lastPortfolioLinks = it.portfolioLinks ?: lastPortfolioLinks
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
    }
}
