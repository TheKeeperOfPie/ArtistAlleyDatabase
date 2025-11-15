package com.thekeeperofpie.artistalleydatabase.alley.artist

import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry

data class ArtistEntry(
    val databaseEntry: ArtistDatabaseEntry.Impl,
): ArtistDatabaseEntry by databaseEntry {
    val linkModels by lazy {
        links.map { LinkModel.parse(it) }.sortedBy { it.logo }
    }
    val storeLinkModels by lazy {
        storeLinks.map { LinkModel.parse(it) }.sortedBy { it.logo }
    }
    val commissionModels by lazy {
        commissions.map { CommissionModel.parse(it) }.sortedBy {
            when (it) {
                CommissionModel.OnSite -> 0
                CommissionModel.Online -> 1
                is CommissionModel.Link -> 2
                is CommissionModel.Unknown -> 3
            }
        }
    }
}
