package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import com.anilist.data.LicensorsQuery
import com.anilist.data.type.ExternalLinkMediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@SingleIn(AppScope::class)
@Inject
class MediaLicensorsController(
    private val scope: ApplicationScope,
    private val aniListApi: AuthedAniListApi,
) {
    private val refresh = RefreshFlow()

    val anime = licensorFlow(ExternalLinkMediaType.ANIME)
    val manga = licensorFlow(ExternalLinkMediaType.MANGA)

    private fun licensorFlow(mediaType: ExternalLinkMediaType) = refresh.updates
        .mapLatest {
            aniListApi.licensors(mediaType)
                .groupBy { it.language }
                .map { LanguageAndSites(it.key, it.value.distinctBy { it.siteId }) }
                .sortedBy { it.language }
        }
        .catch { emit(emptyList()) }
        .flowOn(CustomDispatchers.IO)
        .stateIn(scope, SharingStarted.Lazily, emptyList())

    fun refresh() = refresh.refresh()

    data class LanguageAndSites(
        val language: String?,
        val sites: List<LicensorsQuery.Data.ExternalLinkSourceCollection>,
    )
}
