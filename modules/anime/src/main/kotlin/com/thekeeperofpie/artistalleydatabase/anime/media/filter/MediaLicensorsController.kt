package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import android.os.SystemClock
import com.anilist.LicensorsQuery
import com.anilist.type.ExternalLinkMediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn

@OptIn(ExperimentalCoroutinesApi::class)
class MediaLicensorsController(
    private val scope: ApplicationScope,
    private val aniListApi: AuthedAniListApi,
) {
    private val refreshUptimeMillis = MutableStateFlow(-1L)

    val anime = licensorFlow(ExternalLinkMediaType.ANIME)
    val manga = licensorFlow(ExternalLinkMediaType.MANGA)

    private fun licensorFlow(mediaType: ExternalLinkMediaType) = refreshUptimeMillis.mapLatest {
        aniListApi.licensors(mediaType)
            .groupBy { it.language }
            .map { LanguageAndSites(it.key, it.value.distinctBy { it.siteId }) }
            .sortedBy { it.language }
    }
        .catch { emit(emptyList()) }
        .flowOn(CustomDispatchers.IO)
        .shareIn(scope, SharingStarted.Lazily, 1)

    fun refresh() {
        refreshUptimeMillis.value = SystemClock.uptimeMillis()
    }

    data class LanguageAndSites(
        val language: String?,
        val sites: List<LicensorsQuery.Data.ExternalLinkSourceCollection>,
    )
}
