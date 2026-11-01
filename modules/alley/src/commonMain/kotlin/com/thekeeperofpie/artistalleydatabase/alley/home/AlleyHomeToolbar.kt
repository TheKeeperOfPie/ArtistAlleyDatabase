package com.thekeeperofpie.artistalleydatabase.alley.home

import androidx.compose.material3.AppBarRow
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_generic_filter_content_description
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.FilterList
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.rememberSortFilterBottomScaffoldState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.toggle
import com.thekeeperofpie.artistalleydatabase.utils_compose.withSaturation
import com.thekeeperofpie.artistalleydatabase.utils_preview.AlleyPreviewThemes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AlleyHomeToolbar(
    navigationState: AlleyHomeNavigationState,
    scrollBehavior: FloatingToolbarScrollBehavior,
    destination: () -> AlleyHomeDestination,
    onChangeDestination: (AlleyHomeDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalFloatingToolbar(
        expanded = true,
        scrollBehavior = scrollBehavior,
        floatingActionButton = {
            val contentDescription =
                stringResource(Res.string.alley_generic_filter_content_description)
            TooltipBox(
                positionProvider =
                    TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above
                    ),
                tooltip = {
                    PlainTooltip(
                        modifier =
                            Modifier.semantics {
                                liveRegion = LiveRegionMode.Assertive
                                paneTitle = contentDescription
                            }
                    ) {
                        Text(contentDescription)
                    }
                },
                state = rememberTooltipState(),
            ) {
                val scope = rememberCoroutineScope()
                FloatingToolbarDefaults.VibrantFloatingActionButton(
                    onClick = {
                        scope.launch {
                            navigationState.run {
                                when (destination()) {
                                    AlleyHomeDestination.ARTISTS -> artistsScaffoldState
                                    AlleyHomeDestination.BROWSE -> browseScaffoldState
                                    AlleyHomeDestination.FAVORITES -> favoritesScaffoldState
                                    AlleyHomeDestination.MAP -> mapScaffoldState
                                    AlleyHomeDestination.STAMP_RALLIES -> ralliesScaffoldState
                                }
                            }.bottomSheetState.toggle()
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = contentDescription,
                    )
                }
            }
        },
        modifier = modifier
    ) {
        val itemsWithText =
            AlleyHomeDestination.entries.map { it to stringResource(it.textRes) }
        val currentDestination = destination()
        AppBarRow {
            itemsWithText.forEach { [item, text] ->
                customItem(
                    appbarContent = {
                        TooltipBox(
                            positionProvider =
                                TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Above
                                ),
                            tooltip = { PlainTooltip { Text(text) } },
                            state = rememberTooltipState(),
                        ) {
                            val checked = item == currentDestination
                            IconToggleButton(
                                colors = IconButtonDefaults.iconToggleButtonVibrantColors(
                                    checkedContainerColor = MaterialTheme.colorScheme.surfaceVariant.withSaturation(0.3f)
                                ),
                                checked = checked,
                                onCheckedChange = { if (it) onChangeDestination(item) },
                                content = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = text,
                                    )
                                },
                            )
                        }
                    },
                    menuContent = {
                        DropdownMenuItem(
                            text = { Text(text) },
                            onClick = {
                                if (item != currentDestination) onChangeDestination(item)
                                it.dismiss()
                            },
                        )
                    }
                )
            }
        }
    }
}

@AlleyPreviewThemes
@Composable
private fun AlleyHomeToolbarPreview() {
    val scaffoldState = rememberSortFilterBottomScaffoldState()
    AlleyHomeToolbar(
        navigationState = remember {
            AlleyHomeNavigationState(
                artistsScaffoldState = scaffoldState,
                browseScaffoldState = scaffoldState,
                favoritesScaffoldState = scaffoldState,
                mapScaffoldState = scaffoldState,
                ralliesScaffoldState = scaffoldState,
            )
        },
        scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
            FloatingToolbarExitDirection.Bottom
        ),
        destination = { AlleyHomeDestination.ARTISTS },
        onChangeDestination = {},
    )
}
