package com.thekeeperofpie.artistalleydatabase.alley.artist

import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel

data class ArtistEntry(
    val year: DataYear,
    val id: String,
    val booth: String?,
    val name: String,
    val summary: String?,
    val links: List<String>,
    val storeLinks: List<String>,
    val catalogLinks: List<String>,
    val driveLink: String?,
    val notes: String?,
    val commissions: List<String> = emptyList(),
    val seriesInferred: List<String>,
    val seriesConfirmed: List<String>,
    val merchInferred: List<String>,
    val merchConfirmed: List<String>,
    val counter: Long,
) {
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
