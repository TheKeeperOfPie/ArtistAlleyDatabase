package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class AppUpdateViewModel : ViewModel() {
    var updateAppUrl by mutableStateOf<String?>(null)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                // TODO: Re-enable, URL not multiplatform, should use Ktor
//                val latestVersion = URL(Secrets.updateUrl)
//                    .openStream()
//                    .use { it.reader().readText() }
//                    .let(Regex("(\\d+)", RegexOption.MULTILINE)::find)
//                    ?.groupValues
//                    ?.firstOrNull()
//                if (latestVersion != Secrets.currentSheetVersion) {
//                    withContext(CustomDispatchers.Main) {
//                        updateAppUrl = Secrets.apkUrl
//                    }
//                }
            } catch (_: Throwable) {
            }
        }
    }
}
