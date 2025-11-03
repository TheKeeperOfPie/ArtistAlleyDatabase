package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.utils.CryptoUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.utils_network.buildNetworkClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow

@SingleIn(AppScope::class)
@DependencyGraph
interface ArtistAlleyAndroidComponent : ArtistAlleyAppComponent {

    @Provides
    fun bindArtistAlleySettings(settings: ArtistAlleyAppSettings): ArtistAlleySettings = settings

    @SingleIn(AppScope::class)
    @Provides
    fun provideMasterKey(application: Application): MasterKey =
        CryptoUtils.masterKey(application, "ArtistAlleyMasterKey")

    @SingleIn(AppScope::class)
    @Provides
    fun provideNetworkClient(application: Application, appScope: ApplicationScope): NetworkClient =
        buildNetworkClient(
            scope = appScope,
            application = application,
            networkSettings = object : NetworkSettings {
                override val networkLoggingLevel =
                    MutableStateFlow(NetworkSettings.NetworkLoggingLevel.NONE)
                override val enableNetworkCaching = MutableStateFlow(false)
            },
            authProviders = emptyMap(),
        )

    @DependencyGraph.Factory
    interface Factory {
        fun create(
            @Provides application: Application,
            @Provides appScope: ApplicationScope,
        ):ArtistAlleyAndroidComponent
    }
}
