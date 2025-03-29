package com.thekeeperofpie.artistalleydatabase.alley.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController

@OptIn(ExperimentalCoilApi::class)
@Composable
fun PreviewDark(content: @Composable () -> Unit) {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(it.data.hashCode())
    }
    val navigationController = object : NavigationController {
        override fun navigateUp() = false
        override fun navigate(navDestination: NavDestination) = Unit
        override fun popBackStack() = false
    }

    CompositionLocalProvider(
        LocalAsyncImagePreviewHandler provides previewHandler,
        LocalNavigationController provides navigationController,
    ) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Surface(content = content)
        }
    }
}
