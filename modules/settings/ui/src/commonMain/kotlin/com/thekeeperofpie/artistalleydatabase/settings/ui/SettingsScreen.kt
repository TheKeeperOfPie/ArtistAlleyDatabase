package com.thekeeperofpie.artistalleydatabase.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import artistalleydatabase.modules.settings.ui.generated.resources.Res
import artistalleydatabase.modules.settings.ui.generated.resources.settings_title
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
object SettingsScreen {

    @Composable
    operator fun invoke(
        sections: List<SettingsSection>,
        upIconOption: UpIconOption?,
        modifier: Modifier = Modifier,
        automaticallyInsertDividers: Boolean = true,
        customSection: @Composable (SettingsSection.Placeholder) -> Unit = {},
    ) {
        var currentSubsectionId by rememberSaveable { mutableStateOf<String?>(null) }
        val currentSubsection = remember(currentSubsectionId) {
            currentSubsectionId?.let { sections.find(it) }
                    as? SettingsSection.Subsection
        }
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(Res.string.settings_title),
                    upIconOption = if (currentSubsection == null) {
                        upIconOption
                    } else {
                        UpIconOption.Back {
                            currentSubsectionId =
                                currentSubsection.let { sections.findParent(it) }?.id
                        }
                    }
                )
            },
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .verticalScroll(rememberScrollState())
            ) {
                BackHandler(currentSubsection != null) {
                    currentSubsectionId = currentSubsection?.let { sections.findParent(it) }?.id
                }

                (currentSubsection?.children ?: sections).forEach { section ->
                    when (section) {
                        is SettingsSection.Placeholder -> customSection(section)
                        is SettingsSection.Subsection ->
                            section.Content(Modifier.clickable { currentSubsectionId = section.id })
                        else -> section.Content(Modifier)
                    }
                    if (automaticallyInsertDividers) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    private fun List<SettingsSection>.findParent(target: SettingsSection): SettingsSection? {
        forEach {
            if (it.id == target.id) return null
            if (it is SettingsSection.Subsection) {
                val subsectionParent = it.findParent(target)
                if (subsectionParent != null) return subsectionParent
            }
        }
        return null
    }

    private fun SettingsSection.Subsection.findParent(target: SettingsSection): SettingsSection? {
        children.forEach {
            if (it.id == target.id) return this
            if (it is SettingsSection.Subsection) {
                val subsectionParent = it.findParent(target)
                if (subsectionParent != null) return subsectionParent
            }
        }
        return null
    }

    private fun List<SettingsSection>.find(targetId: String): SettingsSection? {
        forEach {
            if (it.id == targetId) return it
            if (it is SettingsSection.Subsection) {
                val subsection = it.find(targetId)
                if (subsection != null) return subsection
            }
        }
        return null
    }

    private fun SettingsSection.Subsection.find(targetId: String): SettingsSection? {
        children.forEach {
            if (it.id == targetId) return this
            if (it is SettingsSection.Subsection) {
                val subsection = it.find(targetId)
                if (subsection != null) return subsection
            }
        }
        return null
    }
}
