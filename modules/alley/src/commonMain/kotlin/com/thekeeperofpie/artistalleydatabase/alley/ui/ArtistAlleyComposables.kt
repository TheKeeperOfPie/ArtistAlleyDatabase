@file:OptIn(
    ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalComposeUiApi::class
)

package com.thekeeperofpie.artistalleydatabase.alley.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_answer_expand_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_con_upcoming_show_qr
import artistalleydatabase.modules.alley.generated.resources.alley_con_upcoming_suffix
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_settings
import artistalleydatabase.modules.alley.generated.resources.alley_switch_data_year
import artistalleydatabase.modules.entry.generated.resources.entry_search_clear
import artistalleydatabase.modules.entry.generated.resources.entry_search_hint
import artistalleydatabase.modules.entry.generated.resources.entry_search_hint_with_entry_count
import coil3.compose.AsyncImage
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen.DisplayType
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen.SearchEntryModel
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.fullName
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagePager
import com.thekeeperofpie.artistalleydatabase.alley.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.ThemeAwareElevatedCard
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIcon
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalAnimatedVisibilityScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.atStartOfDayIn
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import artistalleydatabase.modules.entry.generated.resources.Res as EntryRes

@Composable
fun <EntryModel : SearchEntryModel> ItemCard(
    entry: EntryModel,
    sharedElementId: Any,
    showGridByDefault: Boolean,
    showRandomCatalogImage: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    onIgnoredToggle: (Boolean) -> Unit,
    onClick: (EntryModel, Int) -> Unit,
    modifier: Modifier = Modifier,
    itemRow: @Composable (
        entry: EntryModel,
        onFavoriteToggle: (Boolean) -> Unit,
        modifier: Modifier,
    ) -> Unit,
) {
    val images = entry.images
    val pagerState = rememberPagerState(
        entry = entry,
        images = images,
        showGridByDefault = showGridByDefault,
        showRandomCatalogImage = showRandomCatalogImage,
    )

    val ignored = entry.ignored
    ThemeAwareElevatedCard(
        onClick = { onClick(entry, pagerState.settledPage) },
        onLongClick = { onIgnoredToggle(!ignored) },
        modifier = modifier.alpha(if (entry.ignored) 0.38f else 1f)
    ) {
        if (images.isNotEmpty() && !entry.ignored) {
            ImagePager(
                images = images,
                pagerState = pagerState,
                sharedElementId = sharedElementId,
                onClickPage = { onClick(entry, it) },
            )
        }

        itemRow(entry, onFavoriteToggle, Modifier)
    }
}

@Composable
fun <EntryModel : SearchEntryModel> ItemImage(
    entry: EntryModel,
    sharedElementId: Any,
    showGridByDefault: Boolean,
    showRandomCatalogImage: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    onIgnoredToggle: (Boolean) -> Unit,
    onClick: (EntryModel, Int) -> Unit,
    modifier: Modifier = Modifier,
    itemRow: @Composable (
        entry: EntryModel,
        onFavoriteToggle: (Boolean) -> Unit,
        modifier: Modifier,
    ) -> Unit,
) {
    val images = entry.images
    val pagerState = rememberPagerState(
        entry = entry,
        images = images,
        showGridByDefault = showGridByDefault,
        showRandomCatalogImage = showRandomCatalogImage,
    )

    val ignored = entry.ignored
    Box(
        modifier = modifier
            .combinedClickable(
                onClick = { onClick(entry, pagerState.settledPage) },
                onLongClick = { onIgnoredToggle(!ignored) }
            )
            .background(color = MaterialTheme.colorScheme.surface)
            .run {
                if (images.isEmpty()) {
                    border(width = Dp.Hairline, color = MaterialTheme.colorScheme.surfaceBright)
                } else {
                    border(width = Dp.Hairline, color = MaterialTheme.colorScheme.surfaceDim)
                }
            }
            .alpha(if (entry.ignored) 0.38f else 1f)
    ) {
        if (images.isEmpty() || entry.ignored) {
            itemRow(entry, onFavoriteToggle, Modifier)
        } else {
            ImagePager(
                images = images,
                pagerState = pagerState,
                sharedElementId = sharedElementId,
                onClickPage = { onClick(entry, it) },
                clipCorners = false,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(topEnd = 12.dp)
                    )
                    .renderInSharedTransitionScopeOverlay()
            ) {
                val booth = entry.booth
                if (booth != null) {
                    Text(
                        text = booth,
                        modifier = Modifier
                            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                            .sharedElement("booth", sharedElementId, zIndexInOverlay = 1f)
                    )
                }

                IconButton(
                    onClick = { onFavoriteToggle(!entry.favorite) },
                    modifier = Modifier
                        .sharedElement("favorite", sharedElementId, zIndexInOverlay = 1f)
                ) {
                    Icon(
                        imageVector = if (entry.favorite) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Filled.FavoriteBorder
                        },
                        contentDescription = stringResource(
                            Res.string.alley_favorite_icon_content_description
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun <EntryModel : SearchEntryModel> rememberPagerState(
    entry: EntryModel,
    images: List<CatalogImage>,
    showGridByDefault: Boolean,
    showRandomCatalogImage: Boolean,
): PagerState {
    val pageCount = if (images.isEmpty()) {
        0
    } else if (images.size == 1) {
        1
    } else {
        images.size + 1
    }
    return rememberPagerState(
        initialPage = if (showGridByDefault || images.isEmpty()) {
            0
        } else if (showRandomCatalogImage) {
            (1..images.size).random(Random(LocalStableRandomSeed.current + entry.id.hashCode()))
        } else {
            1
        },
        pageCount = { pageCount },
    )
}

internal class WrappedViewConfiguration(
    viewConfiguration: ViewConfiguration,
    val overrideTouchSlop: Float,
) : ViewConfiguration by viewConfiguration {
    override val touchSlop = overrideTouchSlop
}

@Composable
fun Modifier.sharedElement(
    vararg keys: Any?,
    zIndexInOverlay: Float = 0f,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
): Modifier {
    if (keys.contains(null)) return this
    if (keys.any { it is SharedTransitionKey && (it.key == "null" || it.key.isEmpty()) }) return this
    // TODO: Replace with SharedTransitionKey variant
    return if (LocalInspectionMode.current) this else with(LocalSharedTransitionScope.current) {
        sharedElement(
            sharedContentState = rememberSharedContentState(key = keys.toList()),
            animatedVisibilityScope = animatedVisibilityScope
                ?: LocalAnimatedVisibilityScope.current,
            zIndexInOverlay = zIndexInOverlay,
        )
    }
}

@Composable
fun Modifier.sharedBounds(vararg keys: Any?, zIndexInOverlay: Float = 0f): Modifier {
    if (keys.contains(null)) return this
    if (keys.any { it is SharedTransitionKey && (it.key == "null" || it.key.isEmpty()) }) return this
    // TODO: sharedBounds causes bugs with scrolling?
    // TODO: Replace with SharedTransitionKey variant
    return if (LocalInspectionMode.current) this else with(LocalSharedTransitionScope.current) {
        sharedBounds(
            rememberSharedContentState(key = keys.toList()),
            animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
            zIndexInOverlay = zIndexInOverlay,
        )
    }
}

@Composable
fun HorizontalPagerIndicator(pagerState: PagerState, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        repeat(pagerState.pageCount) {
            val color = if (pagerState.currentPage == it) Color.DarkGray else Color.LightGray
            Box(
                modifier = Modifier
                    .clickable { scope.launch { pagerState.animateScrollToPage(it) } }
                    .padding(2.dp)
                    .background(color, CircleShape)
                    .border(1.dp, Color.DarkGray, CircleShape)
                    .size(8.dp)
            )
        }
    }
}

@Composable
internal fun SmallImageGrid(
    targetHeight: Int? = null,
    images: List<CatalogImage>,
    onImageClick: (index: Int, image: Uri) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .let {
                if (targetHeight == null) {
                    it
                } else if (targetHeight > 0) {
                    it.height(density.run { targetHeight.toDp() })
                } else {
                    it.heightIn(max = 320.dp)
                }
            }
    ) {
        itemsIndexed(images) { index, image ->
            AsyncImage(
                model = image.uri,
                contentScale = ContentScale.FillWidth,
                contentDescription = stringResource(Res.string.alley_artist_catalog_image),
                modifier = Modifier
                    .clickable { onImageClick(index, image.uri) }
                    .sharedElement("gridImage", image.uri)
                    .fillMaxWidth()
                    .conditionally(image.width != null && image.height != null) {
                        aspectRatio(image.width!! / image.height!!.toFloat())
                    }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun currentWindowSizeClass(): WindowSizeClass {
    val density = LocalDensity.current
    val windowConfiguration = LocalWindowConfiguration.current
    val width = windowConfiguration.screenWidthDp
    val height = windowConfiguration.screenHeightDp
    return remember(density, windowConfiguration) {
        WindowSizeClass.calculateFromSize(DpSize(width, height))
    }
}

// TooltipBox crashes on web
@Composable
fun Tooltip(
    text: String? = null,
    popupAlignment: Alignment = Alignment.BottomCenter,
    onClick: (() -> Unit)? = null,
    allowPopupHover: Boolean = true,
    showOnClick: Boolean = false,
    content: @Composable () -> Unit,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val contentInteractionSource = remember { MutableInteractionSource() }
    var popupPressVisible by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .let {
                if (onClick == null) {
                    it.pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                popupPressVisible = true
                            },
                            onTap = if (showOnClick) {
                                {
                                    popupPressVisible = true
                                }
                            } else null,
                        )
                    }
                } else {
                    it.combinedClickable(
                        onClick = onClick,
                        onLongClick = { popupPressVisible = true },
                    )
                }
            }
            .hoverable(contentInteractionSource)
            .onGloballyPositioned { size = it.size }
    ) {
        content()

        if (text != null) {
            val popupInteractionSource = remember { MutableInteractionSource() }
            val contentIsHovered by contentInteractionSource.collectIsHoveredAsState()
            val popupIsHovered by popupInteractionSource.collectIsHoveredAsState()
            if (contentIsHovered || (popupIsHovered && allowPopupHover) || popupPressVisible) {
                Popup(
                    alignment = popupAlignment,
                    offset = IntOffset(0, -size.height),
                    onDismissRequest = { popupPressVisible = false },
                ) {
                    Text(
                        text = text,
                        modifier = Modifier
                            .hoverable(popupInteractionSource)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceDim,
                                shape = MaterialTheme.shapes.small,
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = MaterialTheme.shapes.small,
                            )
                            .padding(8.dp)
                            .widthIn(max = 240.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun IconWithTooltip(
    imageVector: ImageVector,
    tooltipText: String,
    contentDescription: String? = null,
    allowPopupHover: Boolean = false,
    modifier: Modifier,
) {
    Tooltip(text = tooltipText, allowPopupHover = allowPopupHover) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = modifier.height(24.dp)
        )
    }
}

@Composable
fun IconButtonWithTooltip(
    imageVector: ImageVector,
    tooltipText: String,
    onClick: () -> Unit,
    contentDescription: String? = null,
    allowPopupHover: Boolean = true,
) {
    Tooltip(text = tooltipText, onClick = onClick, allowPopupHover = allowPopupHover) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.minimumInteractiveComponentSize()
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

@Composable
fun rememberDataYearHeaderState(
    dataYear: MutableStateFlow<DataYear>,
    lockedYear: DataYear?,
): DataYearHeaderState {
    var year by dataYear.collectAsMutableStateWithLifecycle()
    return remember(dataYear, lockedYear) {
        DataYearHeaderState(
            dataYear = { year },
            onYearChange = {
                if (lockedYear == null) {
                    year = it
                }
            },
            lockedYear = lockedYear != null,
        )
    }
}

@Stable
class DataYearHeaderState(
    private val dataYear: () -> DataYear,
    val onYearChange: (DataYear) -> Unit,
    val lockedYear: Boolean,
) {
    var year
        get() = dataYear()
        set(value) {
            onYearChange(value)
        }
}

private val UPCOMING_PROMPT_DURATION = 15.days

@Composable
fun DataYearHeader(state: DataYearHeaderState, additionalActions: (@Composable () -> Unit)? = null) {
    Column {
        val year = state.year
        val windowSizeClass = currentWindowSizeClass()
        val relativeTime = remember(windowSizeClass, year) {
            if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                return@remember null
            }
            val start = year.dates.start.atStartOfDayIn(year.timeZone)
            val now = Clock.System.now()
            if (start > now && start - now < UPCOMING_PROMPT_DURATION) {
                HumanReadable.timeAgo(start)
            } else {
                null
            }
        }
        if (relativeTime != null) {
            ThemeAwareElevatedCard(Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    val text = buildAnnotatedString {
                        append(stringResource(year.shortName))
                        append(" is ")
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Black,
                            )
                        ) {
                            append(relativeTime)
                        }
                        append(stringResource(Res.string.alley_con_upcoming_suffix))
                    }
                    Text(
                        text = text,
                        modifier = Modifier.weight(1f)
                    )
                    val navigationController = LocalNavigationController.current
                    Button(onClick = { navigationController.navigate(Destinations.Export) }) {
                        Text(stringResource(Res.string.alley_con_upcoming_show_qr))
                    }
                }
            }
        }

        if (year == DataYear.ANIME_EXPO_2025) {
            ThemeAwareElevatedCard(Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    val text = buildAnnotatedString {
                        append("Thanks for attending ")
                        append(stringResource(year.shortName))
                        append("! If you have any feedback, please let us know ")
                        withStyle(SpanStyle(color = colorScheme.primary)) {
                            withLink(LinkAnnotation.Url(BuildKonfig.feedbackFormLink)) {
                                append("here")
                            }
                        }
                    }
                    Text(
                        text = text,
                        modifier = Modifier.weight(1f)
                    )
                    val uriHandler = LocalUriHandler.current
                    Button(onClick = { uriHandler.openUri(BuildKonfig.feedbackFormLink) }) {
                        Text("Open")
                    }
                }
            }
        }

        if (state.lockedYear) {
            Text(
                text = stringResource(state.year.fullName),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    ) {
                        Text(
                            text = stringResource(state.year.fullName),
                            style = MaterialTheme.typography.headlineSmall,
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            TrailingDropdownIcon(
                                expanded = expanded,
                                contentDescription = stringResource(Res.string.alley_switch_data_year),
                            )
                        }
                    }
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        DataYear.entries.forEach {
                            DropdownMenuItem(
                                text = { Text(stringResource(it.fullName)) },
                                leadingIcon = {
                                    RadioButton(
                                        selected = state.year == it,
                                        onClick = { state.year = it },
                                    )
                                },
                                onClick = {
                                    state.year = it
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                if (additionalActions != null) {
                    additionalActions()
                }

                val navigationController = LocalNavigationController.current
                IconButton(onClick = { navigationController.navigate(Destinations.Settings) }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(Res.string.alley_settings),
                    )
                }
            }
        }
    }
}

@Composable
fun DisplayTypeSearchBar(
    onClickBack: (() -> Unit)?,
    query: MutableStateFlow<String>,
    displayType: MutableStateFlow<DisplayType>,
    itemCount: () -> Int,
    title: () -> String?,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        var query by query.collectAsMutableStateWithLifecycle()
        val isNotEmpty by remember { derivedStateOf { query.isNotEmpty() } }
        BackHandler(isNotEmpty && !WindowInsets.isImeVisibleKmp) {
            query = ""
        }

        StaticSearchBar(
            leadingIcon = if (onClickBack != null) {
                { ArrowBackIconButton(onClickBack) }
            } else null,
            query = query,
            onQueryChange = { query = it },
            placeholder = {
                val title = title()
                if (title != null) {
                    Text(title)
                } else {
                    val itemCount = itemCount()
                    Text(
                        if (itemCount > 0) {
                            stringResource(
                                EntryRes.string.entry_search_hint_with_entry_count,
                                itemCount,
                            )
                        } else {
                            stringResource(EntryRes.string.entry_search_hint)
                        }
                    )
                }
            },
            trailingIcon = {
                Row {
                    actions?.invoke(this)

                    AnimatedVisibility(isNotEmpty) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(
                                    EntryRes.string.entry_search_clear
                                ),
                            )
                        }
                    }
                    Box {
                        var displayType by displayType
                            .collectAsMutableStateWithLifecycle()
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = displayType.icon,
                                contentDescription = stringResource(
                                    Res.string.alley_display_type_icon_content_description,
                                ),
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            DisplayType.entries.forEach {
                                DropdownMenuItem(
                                    text = { Text(stringResource(it.label)) },
                                    leadingIcon = {
                                        RadioButton(
                                            selected = displayType == it,
                                            onClick = { displayType = it },
                                        )
                                    },
                                    onClick = { displayType = it },
                                )
                            }
                        }
                    }
                }
            },
            onSearch = {},
            modifier = Modifier
                .widthIn(max = 1200.dp)
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
    }
}

@Composable
fun QuestionAnswer(
    question: String,
    answer: String,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
    extraContent: @Composable () -> Unit = {},
) = QuestionAnswer(
    question = question,
    answer = { append(answer) },
    inlineContent = inlineContent,
    extraContent = extraContent,
)

@Composable
fun QuestionAnswer(
    question: String,
    answer: AnnotatedString.Builder.() -> Unit,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
    extraContent: @Composable () -> Unit = {},
) {
    Column {
        var expanded by rememberSaveable { mutableStateOf(false) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .clickable { expanded = !expanded }
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = stringResource(Res.string.alley_answer_expand_content_description),
                onClick = { expanded = !expanded },
            )
        }

        AnimatedVisibility(expanded, enter = expandVertically(), exit = shrinkVertically()) {
            if (expanded) {
                val answerText = remember(answer) {
                    buildAnnotatedString(answer)
                }
                Text(
                    text = answerText,
                    inlineContent = inlineContent,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 12.dp,
                    )
                )
            }
        }

        extraContent()
    }
}
