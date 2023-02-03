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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.anilist.AniListStringR
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.dropdown.DropdownMenuItem
import com.thekeeperofpie.artistalleydatabase.form.FormStringR
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistsStringR
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbStringR

object SettingsScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        onClickNav: () -> Unit = {},
        errorRes: () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = {},
        onClickAniListClear: () -> Unit = {},
        onClickVgmdbClear: () -> Unit = {},
        onClickDatabaseFetch: () -> Unit = {},
        onClickClearDatabaseById: (DatabaseType, String) -> Unit = { _, _ -> },
        onClickRebuildDatabase: (DatabaseType) -> Unit = {},
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(R.string.nav_drawer_settings),
                    onClickNav = onClickNav
                )
            },
            snackbarHost = {
                SnackbarErrorText(
                    errorRes()?.first,
                    errorRes()?.second,
                    onErrorDismiss = onErrorDismiss
                )
            },
        ) {
            Column(Modifier.padding(it)) {
                ButtonRow(
                    title = R.string.settings_clear_aniList_cache,
                    buttonText = R.string.clear,
                    onClick = onClickAniListClear
                )

                Divider()

                ButtonRow(
                    title = R.string.settings_clear_vgmdb_cache,
                    buttonText = R.string.clear,
                    onClick = onClickVgmdbClear
                )

                Divider()

                ButtonRow(
                    title = R.string.settings_database_fetch,
                    buttonText = R.string.fetch,
                    onClick = onClickDatabaseFetch
                )

                Divider()

                ClearDatabaseByIdRow(onClickClearDatabaseById)

                Divider()

                RebuildDatabaseRow(onClickRebuildDatabase)
            }
        }
    }

    @Composable
    private fun ButtonRow(@StringRes title: Int, @StringRes buttonText: Int, onClick: () -> Unit) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
        ) {
            Text(
                text = stringResource(title),
                Modifier
                    .weight(1f)
            )
            FilledTonalButton(onClick = onClick) { Text(text = stringResource(buttonText)) }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DatabaseDropdown(
        modifier: Modifier = Modifier,
        onSelectDatabase: (DatabaseType) -> Unit,
    ) {
        var expanded by remember { mutableStateOf(false) }
        var selectedDatabaseTypeIndex by rememberSaveable { mutableStateOf(0) }
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
                label = { Text(stringResource(R.string.label_database_type)) },
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

    @OptIn(ExperimentalMaterial3Api::class)
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
                label = { Text(stringResource(id = R.string.label_id)) },
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(onClick = {
                onClickClearDatabaseById(selectedDatabase, clearDatabaseId)
            }) { Text(text = stringResource(FormStringR.delete)) }
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
                Text(text = stringResource(FormStringR.rebuild))
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
        ANILIST(AniListStringR.aniList),
        VGMDB(VgmdbStringR.vgmdb),
        MUSICAL_ARTISTS(MusicalArtistsStringR.musical_artists),
    }
}

@Preview
@Composable
fun Preview() {
    SettingsScreen()
}