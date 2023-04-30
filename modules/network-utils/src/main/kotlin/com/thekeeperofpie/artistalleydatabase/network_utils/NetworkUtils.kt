package com.thekeeperofpie.artistalleydatabase.network_utils

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

fun OkHttpClient.Builder.addLoggingInterceptors(
    tag: String,
    networkSettings: NetworkSettings,
) = apply {
    if (BuildConfig.DEBUG) {
        addInterceptor(HttpLoggingInterceptor {
            Log.d(tag, "OkHttp request: $it")
        }.apply {
            level = when (networkSettings.networkLoggingLevel.value) {
                NetworkSettings.NetworkLoggingLevel.NONE -> HttpLoggingInterceptor.Level.NONE
                NetworkSettings.NetworkLoggingLevel.BASIC -> HttpLoggingInterceptor.Level.BASIC
                NetworkSettings.NetworkLoggingLevel.HEADERS -> HttpLoggingInterceptor.Level.HEADERS
                NetworkSettings.NetworkLoggingLevel.BODY -> HttpLoggingInterceptor.Level.BODY
            }
        })
    }
}
