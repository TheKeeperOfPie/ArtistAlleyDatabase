package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.ui.util.fastForEachReversed
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ListDiff

data class ArtistHistoryEntryWithDiff(
    val entry: ArtistHistoryEntry,
    val socialLinksDiff: ListDiff<String>?,
    val storeLinksDiff: ListDiff<String>?,
    val portfolioLinksDiff: ListDiff<String>?,
    val catalogLinksDiff: ListDiff<String>?,
    val commissionsDiff: ListDiff<String>?,
    val seriesInferredDiff: ListDiff<String>?,
    val seriesConfirmedDiff: ListDiff<String>?,
    val merchInferredDiff: ListDiff<String>?,
    val merchConfirmedDiff: ListDiff<String>?,
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
                val socialLinksDiff = ListDiff.diffList(lastSocialLinks, it.socialLinks)
                val storeLinksDiff = ListDiff.diffList(lastStoreLinks, it.storeLinks)
                val portfolioLinksDiff = ListDiff.diffList(lastPortfolioLinks, it.portfolioLinks)
                val catalogLinksDiff = ListDiff.diffList(lastCatalogLinks, it.catalogLinks)
                val commissionsDiff = ListDiff.diffList(lastCommissions, it.commissions)
                val seriesInferredDiff = ListDiff.diffList(lastSeriesInferred, it.seriesInferred)
                val seriesConfirmedDiff = ListDiff.diffList(lastSeriesConfirmed, it.seriesConfirmed)
                val merchInferredDiff = ListDiff.diffList(lastMerchInferred, it.merchInferred)
                val merchConfirmedDiff = ListDiff.diffList(lastMerchConfirmed, it.merchConfirmed)
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
