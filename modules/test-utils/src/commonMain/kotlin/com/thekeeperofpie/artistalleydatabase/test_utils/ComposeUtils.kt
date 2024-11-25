@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.thekeeperofpie.artistalleydatabase.test_utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.WindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalAnimatedVisibilityScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavHostController

@Composable
fun ComposeTestRoot(vararg values: ProvidedValue<*>, content: @Composable () -> Unit) {
    SharedTransitionLayout {
        AnimatedVisibility(visible = true) {
            CompositionLocalProvider(
                LocalWindowConfiguration provides WindowConfiguration(100.dp, 100.dp),
                LocalNavHostController provides rememberNavController(),
                LocalSharedTransitionScope provides this@SharedTransitionLayout,
                LocalAnimatedVisibilityScope provides this@AnimatedVisibility,
                *values,
                content = content,
            )
        }
    }
}
