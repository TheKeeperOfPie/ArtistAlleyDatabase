package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.fragment.UserFavoriteMediaNode
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListScreen
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysNavigationBar
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.navigationBarEnterAlwaysScrollBehavior
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
object AnimeHomeScreen {

    @Composable
    operator fun invoke(
        onClickNav: () -> Unit,
        needAuth: () -> Boolean,
        onClickAuth: () -> Unit,
        onSubmitAuthToken: (String) -> Unit,
        onTagClick: (tagId: String, tagName: String) -> Unit,
        onMediaClick: (AnimeMediaListRow.Entry) -> Unit,
        onUserMediaClick: (UserFavoriteMediaNode) -> Unit,
        onCharacterClicked: (String) -> Unit,
        onCharacterLongClicked: (String) -> Unit,
        onStaffClicked: (String) -> Unit,
        onStaffLongClicked: (String) -> Unit,
        onStudioClicked: (String) -> Unit,
        errorRes: () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = { },
    ) {
        var selectedScreen by remember { mutableStateOf(AnimeNavDestinations.SEARCH) }
        val scrollBehavior = navigationBarEnterAlwaysScrollBehavior()
        Scaffold(
            snackbarHost = {
                SnackbarErrorText(
                    errorRes()?.first,
                    errorRes()?.second,
                    onErrorDismiss = onErrorDismiss
                )
            },
            bottomBar = {
                EnterAlwaysNavigationBar(scrollBehavior = scrollBehavior) {
                    AnimeNavDestinations.values().forEach { destination ->
                        NavigationBarItem(
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.textRes)) },
                            selected = selectedScreen == destination,
                            onClick = { selectedScreen = destination }
                        )
                    }
                }
            },
        ) {
            // TODO: Use an offset that mutates the filter bottom panel directly
            val offset = LocalDensity.current.run { -scrollBehavior.state.heightOffset.toDp() }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = it.calculateBottomPadding() - offset)
            ) {
                if (needAuth()) {
                    AuthPrompt(onClickAuth = onClickAuth, onSubmitAuthToken = onSubmitAuthToken)
                } else {
                    val scrollPositions =
                        rememberSaveable(saver = object :
                            Saver<MutableMap<String, Pair<Int, Int>>, String> {
                            override fun restore(value: String) =
                                Json.decodeFromString<Map<String, Pair<Int, Int>>>(value)
                                    .toMutableMap()

                            override fun SaverScope.save(value: MutableMap<String, Pair<Int, Int>>) =
                                Json.encodeToString(value)

                        }) { mutableStateMapOf() }

                    Crossfade(
                        targetState = selectedScreen,
                        label = "Anime home destination crossfade",
                    ) {
                        when (it) {
                            AnimeNavDestinations.LIST -> {
                                val viewModel = hiltViewModel<AnimeUserListViewModel>()
                                    .apply { initialize() }
                                AnimeUserListScreen(
                                    nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                                    onClickNav = onClickNav,
                                    query = { viewModel.query.collectAsState().value },
                                    onQueryChange = viewModel::onQuery,
                                    filterData = { viewModel.filterData() },
                                    onRefresh = viewModel::onRefresh,
                                    content = { viewModel.content },
                                    tagShown = { viewModel.tagShown },
                                    onTagDismiss = viewModel::onTagDismiss,
                                    onTagClick = onTagClick,
                                    onTagLongClick = viewModel::onTagLongClick,
                                    onMediaClick = onMediaClick,
                                    scrollStateSaver = ScrollStateSaver.fromMap(
                                        AnimeNavDestinations.LIST.id,
                                        scrollPositions
                                    ),
                                )
                            }
                            AnimeNavDestinations.SEARCH -> {
                                AnimeNavigator.SearchScreen(
                                    title = null,
                                    tagId = null,
                                    onClickNav = onClickNav,
                                    onTagClick = onTagClick,
                                    onMediaClick = onMediaClick,
                                    scrollStateSaver = ScrollStateSaver.fromMap(
                                        AnimeNavDestinations.SEARCH.id,
                                        scrollPositions
                                    ),
                                    nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                                )
                            }
                            AnimeNavDestinations.PROFILE -> {
                                AnimeNavigator.UserScreen(
                                    userId = null,
                                    scrollStateSaver = ScrollStateSaver.fromMap(
                                        AnimeNavDestinations.PROFILE.id,
                                        scrollPositions
                                    ),
                                    onMediaClick = onUserMediaClick,
                                    onCharacterClicked = onCharacterClicked,
                                    onCharacterLongClicked = onCharacterLongClicked,
                                    onStaffClicked = onStaffClicked,
                                    onStaffLongClicked = onStaffLongClicked,
                                    onStudioClicked = onStudioClicked,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AuthPrompt(onClickAuth: () -> Unit, onSubmitAuthToken: (String) -> Unit) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.anime_auth_prompt))
            TextButton(onClick = onClickAuth) {
                Text(stringResource(R.string.anime_auth_button))
            }
            Text(stringResource(R.string.anime_auth_prompt_paste))

            var value by remember { mutableStateOf("") }
            TextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier
                    .sizeIn(minWidth = 200.dp, minHeight = 200.dp)
                    .padding(16.dp),
            )

            TextButton(onClick = {
                val token = value
                value = ""
                onSubmitAuthToken(token)
            }) {
                Text(stringResource(UtilsStringR.confirm))
            }
        }
    }
}
