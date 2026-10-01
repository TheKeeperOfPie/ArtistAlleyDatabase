package com.thekeeperofpie.artistalleydatabase.alley.navigation

import androidx.compose.runtime.Composable
import androidx.savedstate.serialization.SavedStateConfiguration
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.TwoWayStack
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberTwoWayStack
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


private val SavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(baseClass = AlleyDestination::class) {
            subclass(serializer = AlleyDestination.Home.serializer())
            subclass(serializer = AlleyDestination.AboutLibraries.serializer())
            subclass(serializer = AlleyDestination.ArtistDetails.serializer())
            subclass(serializer = AlleyDestination.ArtistMap.serializer())
            subclass(serializer = AlleyDestination.ArtistsList.serializer())
            subclass(serializer = AlleyDestination.ArtistChangelog.serializer())
            subclass(serializer = AlleyDestination.Export.serializer())
            subclass(serializer = AlleyDestination.FavoritesChangelog.serializer())
            subclass(serializer = AlleyDestination.FavoriteArtistsChangelog.serializer())
            subclass(serializer = AlleyDestination.FavoriteSeriesChangelog.serializer())
            subclass(serializer = AlleyDestination.FavoriteMerchChangelog.serializer())
            subclass(serializer = AlleyDestination.FavoriteRalliesChangelog.serializer())
            subclass(serializer = AlleyDestination.Images.serializer())
            subclass(serializer = AlleyDestination.Import.serializer())
            subclass(serializer = AlleyDestination.Series.serializer())
            subclass(serializer = AlleyDestination.SeriesChangelog.serializer())
            subclass(serializer = AlleyDestination.SeriesTagChangelog.serializer())
            subclass(serializer = AlleyDestination.SeriesMap.serializer())
            subclass(serializer = AlleyDestination.StampRallyChangelog.serializer())
            subclass(serializer = AlleyDestination.Merch.serializer())
            subclass(serializer = AlleyDestination.MerchChangelog.serializer())
            subclass(serializer = AlleyDestination.MerchTagChangelog.serializer())
            subclass(serializer = AlleyDestination.MerchMap.serializer())
            subclass(serializer = AlleyDestination.Metrics.serializer())
            subclass(serializer = AlleyDestination.Settings.serializer())
            subclass(serializer = AlleyDestination.StampRallies.serializer())
            subclass(serializer = AlleyDestination.StampRallyDetails.serializer())
            subclass(serializer = AlleyDestination.StampRallyMap.serializer())
        }
    }
}

typealias AlleyNavStack = TwoWayStack<AlleyDestination>

@Composable
fun rememberAlleyNavStack(vararg initialDestinations: AlleyDestination): AlleyNavStack =
    rememberTwoWayStack(
        *initialDestinations.ifEmpty { arrayOf(AlleyDestination.Home) },
        savedStateConfiguration = SavedStateConfig,
        encode = AlleyDestination::toEncodedRoute,
    )
