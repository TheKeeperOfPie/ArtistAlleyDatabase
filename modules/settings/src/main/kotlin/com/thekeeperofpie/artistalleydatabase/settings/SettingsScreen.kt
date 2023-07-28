package com.thekeeperofpie.artistalleydatabase.settings

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anilist.AniListStringR
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistsStringR
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbStringR

@OptIn(ExperimentalMaterial3Api::class)
object SettingsScreen {

    @Composable
    operator fun invoke(
        viewModel: SettingsViewModel,
        upIconOption: UpIconOption?,
        onClickShowLastCrash: () -> Unit,
        onClickShowLicenses: () -> Unit,
    ) {
        var currentSubsectionId by rememberSaveable { mutableStateOf<String?>(null) }
        val currentSubsection = remember(currentSubsectionId) {
            currentSubsectionId?.let { viewModel.sections.find(it) }
                    as? SettingsSection.Subsection
        }
        val sections = viewModel.sections
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(R.string.settings_nav_drawer),
                    upIconOption = if (upIconOption is UpIconOption.Back) {
                        UpIconOption.Back {
                            if (currentSubsection == null) {
                                upIconOption.onClick()
                            } else {
                                currentSubsectionId =
                                    currentSubsection.let { sections.findParent(it) }?.id
                            }
                        }
                    } else upIconOption,
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .verticalScroll(rememberScrollState())
            ) {
                BackHandler(currentSubsection != null) {
                    currentSubsectionId = currentSubsection?.let { sections.findParent(it) }?.id
                }

                (currentSubsection?.children ?: viewModel.sections).forEach { section ->
                    if (section is SettingsSection.Placeholder) {
                        when (section.id) {
                            "openLastCrash" -> ButtonRow(
                                labelTextRes = R.string.settings_show_last_crash,
                                buttonTextRes = UtilsStringR.open,
                                onClick = onClickShowLastCrash,
                            )
                            "showLicenses" -> ButtonRow(
                                labelTextRes = R.string.settings_show_licenses,
                                buttonTextRes = UtilsStringR.open,
                                onClick = onClickShowLicenses,
                            )
                        }
                    } else if (section is SettingsSection.Subsection) {
                        section.Content(Modifier.clickable { currentSubsectionId = section.id })
                    } else {
                        section.Content(Modifier)
                    }
                    HorizontalDivider()
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

    enum class DatabaseType(@StringRes val labelRes: Int) {
        ANILIST_CHARACTERS(AniListStringR.aniList_characters),
        ANILIST_MEDIA(AniListStringR.aniList_media),
        VGMDB_ALBUMS(VgmdbStringR.vgmdb_albums),
        VGMDB_ARTISTS(VgmdbStringR.vgmdb_artists),
        MUSICAL_ARTISTS(MusicalArtistsStringR.musical_artists),
    }
}
