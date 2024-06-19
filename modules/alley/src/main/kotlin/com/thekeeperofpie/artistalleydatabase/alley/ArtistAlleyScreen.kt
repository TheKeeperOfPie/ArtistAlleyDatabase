package com.thekeeperofpie.artistalleydatabase.alley

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
object ArtistAlleyScreen {

    @Composable
    operator fun invoke(
        onClickBack: () -> Unit,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
        onStampRallyClick: (StampRallyEntryGridModel, Int) -> Unit,
    ) {
        val updateNotice = stringResource(R.string.alley_update_notice)
        val updateOpenUpdate = stringResource(R.string.alley_open_update)
        val uriHandler = LocalUriHandler.current
        val updateAppUrl = hiltViewModel<UpdateViewModel>().updateAppUrl
        val artistsScaffoldState = rememberBottomSheetScaffoldState(
            rememberStandardBottomSheetState(
                confirmValueChange = { it != SheetValue.Hidden },
                skipHiddenState = true,
            )
        )
        LaunchedEffect(updateAppUrl) {
            if (updateAppUrl != null) {
                val result = artistsScaffoldState.snackbarHostState.showSnackbar(
                    message = updateNotice,
                    withDismissAction = true,
                    actionLabel = updateOpenUpdate,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    uriHandler.openUri(updateAppUrl)
                }
            }
        }

        var currentIndex by rememberSaveable { mutableIntStateOf(0) }
        var currentDestination by rememberSaveable { mutableStateOf(Destinations.ARTISTS) }
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                Destinations.entries.forEach {
                    item(
                        icon = {
                            Icon(
                                it.icon,
                                contentDescription = stringResource(it.textRes)
                            )
                        },
                        label = { Text(stringResource(it.textRes)) },
                        selected = it == currentDestination,
                        onClick = { currentDestination = it }
                    )
                }
            }
        ) {
            when (currentDestination) {
                Destinations.ARTISTS -> ArtistSearchScreen(
                    onClickBack,
                    onArtistClick,
                    artistsScaffoldState,
                )
                Destinations.MAP -> TODO()
                Destinations.STAMP_RALLIES -> StampRallySearchScreen(onStampRallyClick)
            }
        }
    }

    enum class Destinations(val icon: ImageVector, @StringRes val textRes: Int) {
        ARTISTS(Icons.Default.Brush, R.string.alley_nav_bar_artists),
        MAP(Icons.Default.Map, R.string.alley_nav_bar_map),
        STAMP_RALLIES(Icons.Default.Approval, R.string.alley_nav_bar_stamp_rallies),
    }

    class UpdateViewModel : ViewModel() {
        var updateAppUrl by mutableStateOf<String?>(null)

        init {
            viewModelScope.launch(CustomDispatchers.IO) {
                try {
                    val latestVersion = URL(Secrets.updateUrl)
                        .openStream()
                        .use { it.reader().readText() }
                        .let(Regex("(\\d+)", RegexOption.MULTILINE)::find)
                        ?.groupValues
                        ?.firstOrNull()
                    if (latestVersion != Secrets.currentSheetVersion) {
                        withContext(CustomDispatchers.Main) {
                            updateAppUrl = Secrets.apkUrl
                        }
                    }
                } catch (ignored: Throwable) {
                }
            }
        }
    }
}
