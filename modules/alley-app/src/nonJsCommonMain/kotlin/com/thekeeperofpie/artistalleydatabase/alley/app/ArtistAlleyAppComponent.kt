package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.navigation.NavType
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyComponent
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import kotlin.jvm.JvmSuppressWildcards
import kotlin.reflect.KType

@SingletonScope
interface ArtistAlleyAppComponent : ArtistAlleyComponent {

    @SingletonScope
    @Provides
    @IntoSet
    fun provideBaseTypeMap() : Map<KType, NavType<*>> = CustomNavTypes.baseTypeMap

    @SingletonScope
    @Provides
    fun bindsTypeMap(typeMaps: @JvmSuppressWildcards Set<Map<KType, NavType<*>>>): NavigationTypeMap =
        NavigationTypeMap(typeMaps.fold(mapOf<KType, NavType<*>>()) { acc, map -> acc + map })
}
