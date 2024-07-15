package com.thekeeperofpie.artistalleydatabase.compose.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavType
import androidx.navigation.toRoute
import kotlin.reflect.KType

inline fun <reified T : Any> SavedStateHandle.toDestination(typeMap: Map<KType, NavType<*>>) =
    toRoute<T>(CustomNavTypes.baseTypeMap + typeMap)
