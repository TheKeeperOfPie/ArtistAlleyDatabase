package com.thekeeperofpie.artistalleydatabase.utils_network

interface WebScraper {
    fun get(url: String): Result

    data class Result(
        val finalUrl: String,
        val response: String,
    )
}
