package com.thekeeperofpie.artistalleydatabase.anilist

import com.apollographql.apollo.cache.normalized.api.NormalizedCacheFactory

expect class AniListSqlCache {
    val cache: NormalizedCacheFactory?
}

expect interface AniListSqlCacheComponent
