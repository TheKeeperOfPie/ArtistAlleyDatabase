package com.thekeeperofpie.artistalleydatabase.utils

interface ComponentProvider {
    fun <T> singletonComponent(): T
}
