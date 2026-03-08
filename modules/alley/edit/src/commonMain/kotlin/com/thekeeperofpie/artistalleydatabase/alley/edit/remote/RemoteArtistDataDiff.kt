package com.thekeeperofpie.artistalleydatabase.alley.edit.remote

import com.thekeeperofpie.artistalleydatabase.alley.links.LinkCategory
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.category
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistRemoteEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ListDiff

data class RemoteArtistDataDiff(
    val booth: String?,
    val name: String?,
    val summary: String?,
    val socialLinks: ListDiff<String>?,
    val storeLinks: ListDiff<String>?,
    val portfolioLinks: ListDiff<String>?,
    val commissions: ListDiff<String>?,
    val otherLinks: ListDiff<String>?,
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
            val previousLinkModels = previousEntry?.links?.map(LinkModel::parse)
            val currentLinkModels = currentEntry.links.map(LinkModel::parse)
            val artistSocialLinks = artist?.socialLinks?.map(LinkModel::parse)
            val artistStoreLinks = artist?.storeLinks?.map(LinkModel::parse)
            val artistPortfolioLinks = artist?.portfolioLinks?.map(LinkModel::parse)
            val artistCommissions = artist?.commissions?.map(LinkModel::parse)
            return RemoteArtistDataDiff(
                booth = currentEntry.booth.takeIf { artist == null || it != artist.booth && it != previousEntry?.booth },
                name = currentEntry.name.takeIf { artist == null || it != artist.name && it != previousEntry?.name },
                summary = currentEntry.summary.takeIf { it != artist?.summary && it != previousEntry?.summary },
                socialLinks = diffList(
                    current = artistSocialLinks,
                    previous = previousLinkModels?.filter {
                        it.type.category == LinkCategory.SOCIALS || it.type.category == LinkCategory.SUPPORT
                    },
                    next = currentLinkModels.filter {
                        it.type.category == LinkCategory.SOCIALS || it.type.category == LinkCategory.SUPPORT
                    },
                ),
                storeLinks = diffList(
                    current = artistStoreLinks,
                    previous = previousLinkModels?.filter { it.type.category == LinkCategory.STORES },
                    next = currentLinkModels.filter { it.type.category == LinkCategory.STORES },
                ),
                portfolioLinks = diffList(
                    current = artistPortfolioLinks,
                    previous = previousLinkModels?.filter { it.type.category == LinkCategory.PORTFOLIOS },
                    next = currentLinkModels.filter { it.type.category == LinkCategory.PORTFOLIOS },
                ),
                commissions = diffList(
                    current = artistCommissions,
                    previous = previousLinkModels?.filter { it.type.category == LinkCategory.COMMISSIONS },
                    next = currentLinkModels.filter { it.type.category == LinkCategory.COMMISSIONS },
                ),
                otherLinks = diffList(
                    current = artistSocialLinks.orEmpty() +
                            artistStoreLinks.orEmpty() +
                            artistPortfolioLinks.orEmpty() +
                            artistCommissions.orEmpty(),
                    previous = previousLinkModels?.filter { it.type.category == LinkCategory.OTHER },
                    next = currentLinkModels.filter { it.type.category == LinkCategory.OTHER },
                ),
            )
        }

        private fun diffList(
            current: List<LinkModel>?,
            previous: List<LinkModel>?,
            next: List<LinkModel>?,
        ): ListDiff<String>? {
            val added = next.orEmpty().filter {
                current.orEmpty()
                    .none { currentLink ->
                        currentLink.type == it.type &&
                                currentLink.identifier.equals(it.identifier, ignoreCase = true)
                    }
            }.ifEmpty { null }
            val deleted = previous.orEmpty().filter {
                next.orEmpty()
                    .none { nextLink ->
                        nextLink.type == it.type &&
                                nextLink.identifier.equals(it.identifier, ignoreCase = true)
                    }
            }
                .filter {
                    current == null || current.any { currentLink ->
                        (currentLink.type == it.type &&
                                currentLink.identifier.equals(it.identifier, ignoreCase = true)) ||
                                currentLink.link.equals(it.link, ignoreCase = true)
                    }
                }
                .ifEmpty { null }
            if (added == null && deleted == null) {
                return null
            }

            return ListDiff(
                added = added?.map { it.link },
                deleted = deleted?.map { it.link },
            )
        }
    }
}
