package com.thekeeperofpie.artistalleydatabase.test_utils

import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkUtilsHiltModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkUtilsHiltModule::class]
)
class TestNetworkHiltModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(testNetworkController: TestNetworkController) =
        testNetworkController.okHttpClient

    @Singleton
    @Provides
    fun provideKtorClient(testNetworkController: TestNetworkController) = HttpClient(OkHttp) {
        engine {
            preconfigured = testNetworkController.okHttpClient
        }
    }

    @Singleton
    @Provides
    fun provideTestNetworkController() = TestNetworkController
}
