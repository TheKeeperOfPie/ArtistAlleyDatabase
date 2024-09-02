package com.thekeeperofpie.artistalleydatabase.anilist

import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory

actual class AniListSqlCache {
    actual val cache: NormalizedCacheFactory? = null
}

actual interface AniListSqlCacheComponent
