package com.thekeeperofpie.artistalleydatabase.alley.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.settings.ui.SettingsScreen
import com.thekeeperofpie.artistalleydatabase.settings.ui.SettingsSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController

@OptIn(ExperimentalMaterial3Api::class)
internal object SettingsScreen {

    @Composable
    operator fun invoke(sections: List<SettingsSection>) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
            SettingsScreen(
                sections = sections,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                modifier = Modifier.widthIn(max = 1200.dp)
            )
        }
    }
}
