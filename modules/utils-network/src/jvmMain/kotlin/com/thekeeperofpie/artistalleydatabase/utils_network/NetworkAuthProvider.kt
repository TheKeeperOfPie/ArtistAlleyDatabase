package com.thekeeperofpie.artistalleydatabase.utils_network

interface NetworkAuthProvider {
    val host: String
    val authHeader: String?
}
