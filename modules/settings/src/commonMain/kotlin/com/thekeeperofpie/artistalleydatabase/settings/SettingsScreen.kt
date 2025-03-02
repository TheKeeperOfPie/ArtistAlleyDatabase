package com.thekeeperofpie.artistalleydatabase.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import artistalleydatabase.modules.settings.generated.resources.settings_show_last_crash
import artistalleydatabase.modules.settings.generated.resources.settings_subsection_feature_tiers
import artistalleydatabase.modules.utils_compose.generated.resources.open
import artistalleydatabase.modules.vgmdb.generated.resources.vgmdb_albums
import artistalleydatabase.modules.vgmdb.generated.resources.vgmdb_artists
import coil3.compose.AsyncImage
import com.thekeeperofpie.artistalleydatabase.monetization.LocalMonetizationProvider
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistsStrings
import com.thekeeperofpie.artistalleydatabase.settings.ui.SettingsScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppMetadataProvider
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
        onClickFeatureTiers: () -> Unit,
        onClickViewMediaHistory: () -> Unit,
        onClickViewMediaIgnore: () -> Unit,
    ) {
        SettingsScreen(
            sections = viewModel.sections,
            upIconOption = upIconOption,
            customSection = {
                when (it.id) {
                    "header" -> Header(viewModel, appMetadataProvider)
                    "openLastCrash" -> ButtonRow(
                        labelTextRes = Res.string.settings_show_last_crash,
                        buttonTextRes = UtilsStrings.open,
                        onClick = onClickShowLastCrash,
                    )
//                            "showLicenses" -> ButtonRow(
//                                labelTextRes = Res.string.settings_show_licenses,
//                                buttonTextRes = UtilsStrings.open,
//                                onClick = onClickShowLicenses,
//                            )
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
            }
        )
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
