package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.serialization.decodeArguments
import androidx.navigation.serialization.generateNavArguments
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KType

val LocalNavHostController = staticCompositionLocalOf<NavHostController> { throw IllegalStateException("No NavHostController provided")}

class NavigationTypeMap(val typeMap: Map<KType, NavType<*>>)

inline fun <reified T : Any> SavedStateHandle.toDestination(typeMap: NavigationTypeMap) = toRoute<T>(typeMap.typeMap)

inline fun <reified T : Any> SavedStateHandle.toRoute(
    typeMap: Map<KType, NavType<*>> = emptyMap()
): T = internalToRoute(T::class, typeMap)

@OptIn(InternalSerializationApi::class)
fun <T : Any> SavedStateHandle.internalToRoute(
    route: KClass<T>,
    typeMap: Map<KType, NavType<*>>
): T {
    val map: MutableMap<String, NavType<*>> = mutableMapOf()
    val serializer = route.serializer()
    serializer.generateNavArguments(typeMap).onEach { map[it.name] = it.argument.type }
    return serializer.decodeArguments(this, map)
}
