package com.thekeeperofpie.artistalleydatabase.alley.home

import androidx.compose.material3.AppBarRow
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
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
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffoldState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.rememberSortFilterBottomScaffoldState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.toggle
import com.thekeeperofpie.artistalleydatabase.utils_compose.withSaturation
import com.thekeeperofpie.artistalleydatabase.utils_preview.AlleyPreviewThemes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AlleyHomeFilterFloatingActionButton(
    scaffoldState: SortFilterBottomScaffoldState,
    vibrant: Boolean = true,
) {
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
        val onClick: () -> Unit = {
            scope.launch {
                scaffoldState.bottomSheetState.toggle()
            }
        }
        val iconContent: @Composable () -> Unit = {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = contentDescription,
            )
        }
        if (vibrant) {
            FloatingToolbarDefaults.VibrantFloatingActionButton(
                onClick = onClick,
                content = iconContent,
            )
        } else {
            FloatingActionButton(
                onClick = onClick,
                content = iconContent,
            )
        }
    }
}

@Composable
internal fun AlleyHomeNavigationToolbar(
    scaffoldState: SortFilterBottomScaffoldState,
    scrollBehavior: FloatingToolbarScrollBehavior,
    artistsDataYear: () -> DataYear,
    destination: () -> AlleyHomeDestination,
    onChangeDestination: (AlleyHomeDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalFloatingToolbar(
        expanded = true,
        scrollBehavior = scrollBehavior,
        floatingActionButton = { AlleyHomeFilterFloatingActionButton(scaffoldState) },
        modifier = modifier
    ) {
        val hasRallies = artistsDataYear().hasRallies
        val itemsWithText =
            AlleyHomeDestination.entries
                .filter { hasRallies || it != AlleyHomeDestination.STAMP_RALLIES }
                .map { it to stringResource(it.textRes) }
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
                                    checkedContainerColor = MaterialTheme.colorScheme.surfaceVariant.withSaturation(
                                        0.3f
                                    )
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
private fun AlleyHomeNavigationToolbarPreview() {
    val scaffoldState = rememberSortFilterBottomScaffoldState()
    AlleyHomeNavigationToolbar(
        scaffoldState = scaffoldState,
        scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
            FloatingToolbarExitDirection.Bottom
        ),
        artistsDataYear = { DataYear.LATEST },
        destination = { AlleyHomeDestination.ARTISTS },
        onChangeDestination = {},
    )
}
