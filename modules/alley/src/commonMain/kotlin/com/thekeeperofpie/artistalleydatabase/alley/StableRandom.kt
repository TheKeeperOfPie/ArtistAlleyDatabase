package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.random.Random

val LocalStableRandomSeed = staticCompositionLocalOf(Random.Default::nextInt)
