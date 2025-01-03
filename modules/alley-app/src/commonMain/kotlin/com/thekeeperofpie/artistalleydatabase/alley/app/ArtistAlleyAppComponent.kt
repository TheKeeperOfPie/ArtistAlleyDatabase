package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.navigation.NavType
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyComponent
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.DataInitializer
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import kotlin.reflect.KType

@SingletonScope
interface ArtistAlleyAppComponent : ArtistAlleyComponent {
    val dataInitializer: DataInitializer
    val navigationTypeMap: NavigationTypeMap

    @SingletonScope
    @Provides
    fun provideArtistAlleyDatabase(database: ArtistAlleyAppDatabase): ArtistAlleyDatabase = database

    @SingletonScope
    @Provides
    @IntoSet
    fun provideBaseTypeMap() : Map<KType, NavType<*>> = CustomNavTypes.baseTypeMap

    @SingletonScope
    @Provides
    fun bindsTypeMap(typeMaps: @JvmSuppressWildcards Set<Map<KType, NavType<*>>>): NavigationTypeMap =
        NavigationTypeMap(typeMaps.fold(mapOf<KType, NavType<*>>()) { acc, map -> acc + map })

    @SingletonScope
    @Provides
    fun provideJson() = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }
}
