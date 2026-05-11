package com.thekeeperofpie.artistalleydatabase.anilist

import com.apollographql.apollo.cache.normalized.api.NormalizedCacheFactory

actual class AniListSqlCache {
    actual val cache: NormalizedCacheFactory? = null
}

actual interface AniListSqlCacheComponent
