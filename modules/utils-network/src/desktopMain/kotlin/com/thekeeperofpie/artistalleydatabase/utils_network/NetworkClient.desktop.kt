package com.thekeeperofpie.artistalleydatabase.utils_network

fun buildNetworkClient() = NetworkClient(
    cache = null,
    authProviders = emptyMap(),
    isConnected = { true },
    interceptors = emptyList()
)
