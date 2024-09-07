package com.thekeeperofpie.artistalleydatabase.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.musical_artists.generated.resources.musical_artists
import artistalleydatabase.modules.settings.generated.resources.Res
import artistalleydatabase.modules.settings.generated.resources.settings_ads_disable_button
import artistalleydatabase.modules.settings.generated.resources.settings_aniList_characters
import artistalleydatabase.modules.settings.generated.resources.settings_aniList_media
import artistalleydatabase.modules.settings.generated.resources.settings_header
import artistalleydatabase.modules.settings.generated.resources.settings_header_app_icon_content_description
import artistalleydatabase.modules.settings.generated.resources.settings_log_out_button
import artistalleydatabase.modules.settings.generated.resources.settings_media_history_view
import artistalleydatabase.modules.settings.generated.resources.settings_media_view_ignored
import artistalleydatabase.modules.settings.generated.resources.settings_nav_drawer
import artistalleydatabase.modules.settings.generated.resources.settings_show_last_crash
import artistalleydatabase.modules.settings.generated.resources.settings_show_licenses
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_feature_tiers
import artistalleydatabase.modules.utils_compose.generated.resources.open
import artistalleydatabase.modules.vgmdb.generated.resources.vgmdb_albums
import artistalleydatabase.modules.vgmdb.generated.resources.vgmdb_artists
import coil3.compose.AsyncImage
import com.thekeeperofpie.artistalleydatabase.monetization.LocalMonetizationProvider
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistsStrings
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppMetadataProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.BackHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbStrings
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object SettingsScreen {

    @Composable
    operator fun invoke(
        viewModel: SettingsViewModel,
        appMetadataProvider: AppMetadataProvider,
        upIconOption: UpIconOption?,
        onClickShowLastCrash: () -> Unit,
        onClickShowLicenses: () -> Unit,
        onClickFeatureTiers: () -> Unit,
        onClickViewMediaHistory: () -> Unit,
        onClickViewMediaIgnore: () -> Unit,
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
                    text = stringResource(Res.string.settings_nav_drawer),
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
                            "header" -> Header(viewModel, appMetadataProvider)
                            "openLastCrash" -> ButtonRow(
                                labelTextRes = Res.string.settings_show_last_crash,
                                buttonTextRes = UtilsStrings.open,
                                onClick = onClickShowLastCrash,
                            )
                            "showLicenses" -> ButtonRow(
                                labelTextRes = Res.string.settings_show_licenses,
                                buttonTextRes = UtilsStrings.open,
                                onClick = onClickShowLicenses,
                            )
                            "viewMediaHistory" -> ButtonRow(
                                labelTextRes = Res.string.settings_media_history_view,
                                buttonTextRes = UtilsStrings.open,
                                onClick = onClickViewMediaHistory,
                            )
                            "viewMediaIgnore" -> ButtonRow(
                                labelTextRes = Res.string.settings_media_view_ignored,
                                buttonTextRes = UtilsStrings.open,
                                onClick = onClickViewMediaIgnore,
                            )
                            "featureTiers" -> {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = onClickFeatureTiers)
                                        .padding(horizontal = 32.dp, vertical = 16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MonetizationOn,
                                        contentDescription = stringResource(
                                            Res.string.settings_subsection_feature_tiers
                                        ),
                                    )
                                    Text(
                                        text = stringResource(
                                            Res.string.settings_subsection_feature_tiers
                                        ),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
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

    @Composable
    private fun Header(viewModel: SettingsViewModel, appMetadataProvider: AppMetadataProvider) {
        OutlinedCard(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                AsyncImage(
                    model = appMetadataProvider.appDrawableModel,
                    contentDescription = stringResource(Res.string.settings_header_app_icon_content_description),
                    modifier = Modifier.size(72.dp)
                )
                Text(text = stringResource(Res.string.settings_header))
            }

            val monetizationProvider = LocalMonetizationProvider.current
            val adsEnabled by viewModel.adsEnabled.collectAsState()
            val hasAuth by viewModel.hasAuth.collectAsState(false)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                if (adsEnabled) {
                    FilledTonalButton(
                        onClick = {
                            if (monetizationProvider != null) {
                                monetizationProvider.revokeAds()
                            } else {
                                viewModel.debugDisableAds()
                            }
                        },
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text(text = stringResource(Res.string.settings_ads_disable_button))
                    }
                }
                if (hasAuth) {
                    FilledTonalButton(
                        onClick = viewModel::logOut,
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text(text = stringResource(Res.string.settings_log_out_button))
                    }
                }
            }
        }
    }

    enum class DatabaseType(val labelRes: StringResource) {
        ANILIST_CHARACTERS(Res.string.settings_aniList_characters),
        ANILIST_MEDIA(Res.string.settings_aniList_media),
        VGMDB_ALBUMS(VgmdbStrings.vgmdb_albums),
        VGMDB_ARTISTS(VgmdbStrings.vgmdb_artists),
        MUSICAL_ARTISTS(MusicalArtistsStrings.musical_artists),
    }
}
