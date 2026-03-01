package com.thekeeperofpie.artistalleydatabase.alley.edit.remote

import com.thekeeperofpie.artistalleydatabase.alley.links.LinkCategory
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.category
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistRemoteEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.HistoryListDiff
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link

data class RemoteArtistDataDiff(
    val booth: String?,
    val name: String?,
    val summary: String?,
    val socialLinks: HistoryListDiff?,
    val storeLinks: HistoryListDiff?,
    val portfolioLinks: HistoryListDiff?,
    val commissions: HistoryListDiff?,
) {
    companion object {
        /**
         * Fields necessary:
         * - Latest remote entry
         * - Previous remote entry
         * - Current artist entry
         *
         * Information necessary to calculate final diff:
         * - Diff between previous remote entry and latest remote entry
         * - Diff between current artist entry and latest remote entry
         */
        fun diff(
            artist: ArtistDatabaseEntry?,
            previousEntry: ArtistRemoteEntry?,
            currentEntry: ArtistRemoteEntry,
        ): RemoteArtistDataDiff {
            val linkModels = currentEntry.links.map(LinkModel::parse)
            return RemoteArtistDataDiff(
                booth = currentEntry.booth.takeIf { artist == null || it != artist.booth && it != previousEntry?.booth },
                name = currentEntry.name.takeIf { artist == null || it != artist.name && it != previousEntry?.name },
                summary = currentEntry.summary.takeIf { it != artist?.summary && it != previousEntry?.summary },
                socialLinks = if (previousEntry?.links.orEmpty() == currentEntry.links) {
                    null
                } else {
                    HistoryListDiff.diffList(
                        previous = artist?.socialLinks,
                        next = linkModels.filter {
                            (it.type.category == LinkCategory.SOCIALS || it.type.category == LinkCategory.SUPPORT) ||
                                    (it.type.category == LinkCategory.OTHER && it.type != Link.Type.VGEN)
                        }.map { it.link },
                    )
                },
                storeLinks = if (previousEntry?.links.orEmpty() == currentEntry.links) {
                    null
                } else {
                    HistoryListDiff.diffList(
                        previous = artist?.storeLinks,
                        next = linkModels.filter { it.type.category == LinkCategory.STORES }
                            .map { it.link },
                    )
                },
                portfolioLinks = if (previousEntry?.links.orEmpty() == currentEntry.links) {
                    null
                } else {
                    HistoryListDiff.diffList(
                        previous = artist?.portfolioLinks,
                        next = linkModels.filter { it.type.category == LinkCategory.PORTFOLIOS }
                            .map { it.link },
                    )
                },
                commissions = if (previousEntry?.links.orEmpty() == currentEntry.links) {
                    null
                } else {
                    HistoryListDiff.diffList(
                        previous = artist?.commissions,
                        next = linkModels.filter { it.type == Link.Type.VGEN }.map { it.link },
                    )
                },
            )
        }
    }
}
