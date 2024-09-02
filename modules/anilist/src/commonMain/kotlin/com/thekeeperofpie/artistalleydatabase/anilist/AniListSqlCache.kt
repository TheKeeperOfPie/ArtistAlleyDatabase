package com.thekeeperofpie.artistalleydatabase.anilist

import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory

expect class AniListSqlCache {
    val cache: NormalizedCacheFactory?
}

expect interface AniListSqlCacheComponent
