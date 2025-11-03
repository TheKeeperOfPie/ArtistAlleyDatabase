package com.thekeeperofpie.artistalleydatabase.anilist

import android.app.Application
import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import dev.zacsweers.metro.Provides

actual class AniListSqlCache(actual val cache: NormalizedCacheFactory?)

actual interface AniListSqlCacheComponent {

    @Provides
    fun provideSqlCache(application: Application): AniListSqlCache = AniListSqlCache(
        SqlNormalizedCacheFactory(
            application,
            "apollo.db",
            useNoBackupDirectory = true,
        )
    )
}
