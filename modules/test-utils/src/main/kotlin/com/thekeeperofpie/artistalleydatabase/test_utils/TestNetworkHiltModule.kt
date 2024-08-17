package com.thekeeperofpie.artistalleydatabase.test_utils

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
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
