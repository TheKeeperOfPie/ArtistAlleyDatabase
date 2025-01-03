package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import androidx.compose.runtime.Stable
import com.anilist.data.AuthedUserQuery
import com.anilist.data.type.ScoreFormat
import com.anilist.data.type.UserTitleLanguage
import kotlinx.serialization.Serializable

@Stable
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
