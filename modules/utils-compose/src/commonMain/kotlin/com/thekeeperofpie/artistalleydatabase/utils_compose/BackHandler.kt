package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.Composable

@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
