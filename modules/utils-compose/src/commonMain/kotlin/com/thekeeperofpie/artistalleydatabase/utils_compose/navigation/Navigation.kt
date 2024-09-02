package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import kotlin.reflect.KType

val LocalNavHostController = staticCompositionLocalOf<NavHostController> { throw IllegalStateException("No NavHostController provided")}

class NavigationTypeMap(val typeMap: Map<KType, NavType<*>>)
