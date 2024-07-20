package com.thekeeperofpie.artistalleydatabase.compose.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavType
import androidx.navigation.toRoute
import kotlin.reflect.KType

class NavigationTypeMap(val typeMap: Map<KType, NavType<*>>)

inline fun <reified T : Any> SavedStateHandle.toDestination(typeMap: NavigationTypeMap) = toRoute<T>(typeMap.typeMap)
