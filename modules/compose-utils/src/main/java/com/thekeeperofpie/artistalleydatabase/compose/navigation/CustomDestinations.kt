package com.thekeeperofpie.artistalleydatabase.compose.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap

inline fun <reified T : Any> SavedStateHandle.toDestination(typeMap: NavigationTypeMap) = toRoute<T>(typeMap.typeMap)
