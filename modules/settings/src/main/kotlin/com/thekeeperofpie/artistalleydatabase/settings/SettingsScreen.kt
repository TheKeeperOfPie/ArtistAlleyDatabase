package com.thekeeperofpie.artistalleydatabase.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anilist.AniListStringR
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.ItemDropdown
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.entry.EntryStringR
import com.thekeeperofpie.artistalleydatabase.monetization.LocalMonetizationProvider
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistsStringR
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbStringR

@OptIn(ExperimentalMaterial3Api::class)
object SettingsScreen {

    @Composable
    operator fun invoke(
        viewModel: SettingsViewModel = hiltViewModel<SettingsViewModel>(),
        upIconOption: UpIconOption? = UpIconOption.Back {},
        onClickShowLastCrash: () -> Unit = {},
        onClickShowLicenses: () -> Unit = {},
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(R.string.settings_nav_drawer),
                    upIconOption = upIconOption,
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .verticalScroll(rememberScrollState())
            ) {
                val adsEnabled by viewModel.adsEnabled.collectAsState()
                val monetizationProvider = LocalMonetizationProvider.current
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp,
                        bottom = 10.dp
                    )
                ) {
                    Text(
                        text = stringResource(
                            if (adsEnabled) {
                                R.string.settings_ads_enabled
                            } else {
                                R.string.settings_ads_disabled
                            }
                        ),
                        Modifier.weight(1f)
                    )
                    FilledTonalButton(
                        onClick = {
                            monetizationProvider?.onAdsRevoked() ?: viewModel.disableAds()
                        },
                        enabled = adsEnabled,
                    ) {
                        Text(text = stringResource(R.string.settings_ads_disable_button))
                    }
                }

                Divider()

                ButtonRow(
                    titleRes = R.string.settings_clear_aniList_cache,
                    buttonTextRes = R.string.settings_clear,
                    onClick = viewModel::clearAniListCache,
                )

                Divider()

                ButtonRow(
                    titleRes = R.string.settings_clear_vgmdb_cache,
                    buttonTextRes = R.string.settings_clear,
                    onClick = viewModel::clearVgmdbCache,
                )

                Divider()

                ButtonRow(
                    titleRes = R.string.settings_database_fetch,
                    buttonTextRes = R.string.settings_fetch,
                    onClick = viewModel::onClickDatabaseFetch,
                )

                Divider()

                ClearDatabaseByIdRow(viewModel::onClickClearDatabaseById)

                Divider()

                RebuildDatabaseRow(viewModel::onClickRebuildDatabase)

                Divider()

                ButtonRow(
                    titleRes = R.string.settings_crop_clear,
                    buttonTextRes = R.string.settings_clear,
                    onClick = viewModel::onClickCropClear,
                )

                Divider()

                ButtonRow(
                    titleRes = R.string.settings_clear_aniList_oAuth,
                    buttonTextRes = R.string.settings_clear,
                    onClick = viewModel::onClickClearAniListOAuth,
                )

                Divider()

                val networkLoggingLevel = viewModel.networkLoggingLevel.collectAsState().value
                ItemDropdown(
                    label = R.string.settings_network_logging_level_label,
                    value = networkLoggingLevel,
                    iconContentDescription = R.string.settings_network_logging_level_label_dropdown_content_description,
                    values = { NetworkSettings.NetworkLoggingLevel.values().toList() },
                    textForValue = { it.name },
                    onSelectItem = viewModel::onChangeNetworkLoggingLevel,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp,
                        bottom = 10.dp
                    ),
                )

                Divider()

                ButtonRow(
                    titleRes = R.string.settings_show_last_crash,
                    buttonTextRes = UtilsStringR.open,
                    onClick = onClickShowLastCrash,
                )

                Divider()

                val unlockAllFeatures by viewModel.unlockAllFeatures.collectAsState()
                SwitchRow(
                    titleRes = R.string.settings_unlock_all_features,
                    checked = { unlockAllFeatures },
                    onCheckedChange = viewModel::onUnlockAllFeaturesChanged,
                )

                Divider()

                if (BuildConfig.DEBUG) {
                    val hideStatusBar by viewModel.hideStatusBar.collectAsState()
                    SwitchRow(
                        titleRes = R.string.settings_hide_status_bar,
                        checked = { hideStatusBar },
                        onCheckedChange = viewModel::onHideStatusBarChanged,
                    )

                    Divider()
                }

                val screenshotMode by viewModel.screenshotMode.collectAsState()
                SwitchRow(
                    titleRes = R.string.settings_screenshot_mode,
                    checked = { screenshotMode },
                    onCheckedChange = viewModel::onScreenshotModeChanged,
                )

                Divider()

                ButtonRow(
                    titleRes = R.string.settings_show_licenses,
                    buttonTextRes = UtilsStringR.open,
                    onClick = onClickShowLicenses,
                )

                Divider()
            }
        }
    }

    @Composable
    private fun ButtonRow(
        @StringRes titleRes: Int,
        @StringRes buttonTextRes: Int,
        onClick: () -> Unit,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
        ) {
            Text(
                text = stringResource(titleRes),
                Modifier
                    .weight(1f)
            )
            FilledTonalButton(onClick = onClick) { Text(text = stringResource(buttonTextRes)) }
        }
    }

    @Composable
    private fun SwitchRow(
        @StringRes titleRes: Int,
        checked: @Composable () -> Boolean,
        onCheckedChange: (Boolean) -> Unit,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
        ) {
            Text(
                text = stringResource(titleRes),
                Modifier
                    .weight(1f)
            )
            Switch(checked = checked(), onCheckedChange = onCheckedChange)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DatabaseDropdown(
        modifier: Modifier = Modifier,
        onSelectDatabase: (DatabaseType) -> Unit,
    ) {
        var expanded by remember { mutableStateOf(false) }
        var selectedDatabaseTypeIndex by rememberSaveable { mutableIntStateOf(0) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = modifier,
        ) {
            TextField(
                value = stringResource(
                    DatabaseType.values()[selectedDatabaseTypeIndex].labelRes
                ),
                onValueChange = {},
                label = { Text(stringResource(R.string.settings_database_type)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .clickable(false) {},
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DatabaseType.values().forEachIndexed { index, databaseType ->
                    DropdownMenuItem(
                        text = { Text(stringResource(databaseType.labelRes)) },
                        onClick = {
                            selectedDatabaseTypeIndex = index
                            expanded = false
                            onSelectDatabase(DatabaseType.values()[selectedDatabaseTypeIndex])
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }

    @Composable
    private fun ClearDatabaseByIdRow(
        onClickClearDatabaseById: (DatabaseType, String) -> Unit = { _, _ -> },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 10.dp,
                bottom = 10.dp
            )
        ) {
            var selectedDatabase by rememberSaveable {
                mutableStateOf(DatabaseType.values().first())
            }

            DatabaseDropdown(
                Modifier
                    .wrapContentWidth()
                    .fillMaxWidth(0.5f)
            ) { selectedDatabase = it }

            Spacer(modifier = Modifier.width(16.dp))

            var clearDatabaseId by remember { mutableStateOf("") }
            TextField(
                value = clearDatabaseId,
                onValueChange = { clearDatabaseId = it },
                label = { Text(stringResource(id = R.string.settings_label_id)) },
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(onClick = {
                onClickClearDatabaseById(selectedDatabase, clearDatabaseId)
            }) { Text(text = stringResource(EntryStringR.delete)) }
        }
    }

    @Composable
    private fun RebuildDatabaseRow(onClickRebuildDatabase: (DatabaseType) -> Unit) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 10.dp,
                bottom = 10.dp
            )
        ) {
            var selectedDatabase by rememberSaveable {
                mutableStateOf(DatabaseType.values().first())
            }

            DatabaseDropdown(
                Modifier
                    .weight(1f, true)
            ) { selectedDatabase = it }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(onClick = { onClickRebuildDatabase(selectedDatabase) }) {
                Text(text = stringResource(EntryStringR.rebuild))
            }
        }
    }

    @Composable
    private fun Divider() {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.inverseOnSurface)
        )
    }

    enum class DatabaseType(@StringRes val labelRes: Int) {
        ANILIST_CHARACTERS(AniListStringR.aniList_characters),
        ANILIST_MEDIA(AniListStringR.aniList_media),
        VGMDB_ALBUMS(VgmdbStringR.vgmdb_albums),
        VGMDB_ARTISTS(VgmdbStringR.vgmdb_artists),
        MUSICAL_ARTISTS(MusicalArtistsStringR.musical_artists),
    }
}
