package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

@Serializable
data class NavigationRequestKey<T : Any>(
    val key: String = Uuid.random().toString(),
    val clazz: KClass<T>,
) {
    companion object {
        inline operator fun <reified T : Any> invoke(key: String) =
            NavigationRequestKey(key, T::class)
    }

    fun unique() = scoped(Uuid.random().toString())
    fun scoped(id: String) = copy(key = "$key-$id")
}

@Composable
fun <T : Any> rememberNavigationRequestKey(parentKey: NavigationRequestKey<T>, id: String? = null) =
    rememberSaveable(id) {
        if (id == null) {
            parentKey.unique()
        } else {
            parentKey.scoped(id)
        }
    }
