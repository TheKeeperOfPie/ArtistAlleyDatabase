package com.thekeeperofpie.artistalleydatabase.compose

import androidx.navigation.NavArgumentBuilder
import androidx.navigation.navArgument

fun navArguments(
    vararg names: String,
    builder: NavArgumentBuilder.() -> Unit,
) = names.map { navArgument(it, builder) }
