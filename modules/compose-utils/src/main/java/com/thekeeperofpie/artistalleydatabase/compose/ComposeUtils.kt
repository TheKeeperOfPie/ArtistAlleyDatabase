package com.thekeeperofpie.artistalleydatabase.compose

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import java.util.Locale

val CompositionLocal<Configuration>.currentLocale: Locale
    @Composable get() {
        return ConfigurationCompat.getLocales(this.current).get(0)
            ?: LocaleListCompat.getDefault()[0]!!
    }
