package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import com.anilist.AuthedUserQuery
import com.anilist.type.ScoreFormat
import com.anilist.type.UserTitleLanguage
import kotlinx.serialization.Serializable

@Serializable
data class AniListViewer(
    val id: String,
    val name: String,
    val titleLanguage: UserTitleLanguage? = null,
    val scoreFormat: ScoreFormat? = null,
) {
    constructor(viewer: AuthedUserQuery.Data.Viewer) : this(
        id = viewer.id.toString(),
        name = viewer.name,
        titleLanguage = viewer.options?.titleLanguage,
        scoreFormat = viewer.mediaListOptions?.scoreFormat,
    )
}
