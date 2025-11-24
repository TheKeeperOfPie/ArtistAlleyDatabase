package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import artistalleydatabase.modules.alley.generated.resources.Res

@Composable
fun VariableFontEffect() {
    val fontFamilyResolver = LocalFontFamilyResolver.current
    LaunchedEffect(Unit) {
        try {
            val fonts = listOf(
                "NotoSansJP.ttf",
                "NotoSansKR.ttf",
                "NotoSansSC.ttf",
            ).map {
                Font(it, Res.readBytes("font/$it"))
            }
            fontFamilyResolver.preload(FontFamily(fonts))
        } catch (_: Throwable) {
        }
    }
}
