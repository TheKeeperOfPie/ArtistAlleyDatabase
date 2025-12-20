package com.thekeeperofpie.artistalleydatabase.utils_network

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.thekeeperofpie.artistalleydatabase.utils.buildconfig.BuildConfigProxy
import kotlinx.coroutines.CoroutineScope
import okhttp3.Cache
import java.io.File

fun buildNetworkClient(
    scope: CoroutineScope,
    application: Application,
    networkSettings: NetworkSettings,
    authProviders: Map<String, NetworkAuthProvider>,
): NetworkClient {
    @Suppress("KotlinConstantConditions")
    val loggingInterceptor =
        if (BuildConfigProxy.BUILD_TYPE == "debug" || BuildConfigProxy.BUILD_TYPE == "internal") {
            LoggingInterceptor(scope, networkSettings, "Network")
        } else null
//    val cronetEngine = CronetEngine.Builder(application)
//        .setStoragePath(File(application.cacheDir, "cronet").apply { mkdir() }.absolutePath)
//        .enableHttpCache(CronetEngine.Builder.HTTP_CACHE_DISK, 500 * 1024 * 1024)
//        .enableBrotli(true)
//        .build()
//    val cronetInterceptor = CronetInterceptor.newBuilder(cronetEngine).build()
    // TODO: Restore Cronet support
    val connectivityManager = application.getSystemService(ConnectivityManager::class.java)
    val interceptors = listOfNotNull(loggingInterceptor)//, cronetInterceptor)
    return NetworkClient(
        cache = Cache(
            directory = File(application.cacheDir, "okhttp"),
            maxSize = 100L * 1024L * 1024L // 100 MiB
        ),
        authProviders = authProviders,
        isConnected = { connectivityManager.isConnected() },
        interceptors = interceptors,
    )
}

private fun ConnectivityManager.isConnected() = activeNetwork
    ?.let { getNetworkCapabilities(it) }
    ?.let {
        it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    } == true
