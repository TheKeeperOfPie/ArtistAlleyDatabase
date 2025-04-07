package com.thekeeperofpie.artistalleydatabase.utils_network

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

class LoggingInterceptor private constructor(
    scope: CoroutineScope,
    networkSettings: NetworkSettings,
    interceptor: HttpLoggingInterceptor,
) : Interceptor by interceptor {

    companion object {
        operator fun invoke(
            scope: CoroutineScope,
            networkSettings: NetworkSettings,
            tag: String,
        ) = LoggingInterceptor(
            scope = scope,
            networkSettings = networkSettings,
            interceptor = HttpLoggingInterceptor { Logger.d(tag) { "OkHttp request: $it" } },
        )
    }

    init {
        scope.launch {
            networkSettings.enableNetworkCaching.collectLatest {
                interceptor.level = when (networkSettings.networkLoggingLevel.value) {
                    NetworkSettings.NetworkLoggingLevel.NONE -> HttpLoggingInterceptor.Level.NONE
                    NetworkSettings.NetworkLoggingLevel.BASIC -> HttpLoggingInterceptor.Level.BASIC
                    NetworkSettings.NetworkLoggingLevel.HEADERS -> HttpLoggingInterceptor.Level.HEADERS
                    NetworkSettings.NetworkLoggingLevel.BODY -> HttpLoggingInterceptor.Level.BODY
                }
            }
        }
    }
}
