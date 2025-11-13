package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_action_delete
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_action_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_booth
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_catalog_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_commissions
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_id
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_merch_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_merch_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_series_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_series_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_store_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_summary
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_title_adding
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_title_editing
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_save_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.more_actions_content_description
import coil3.compose.AsyncImage
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.name
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImagesEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagePager
import com.thekeeperofpie.artistalleydatabase.alley.images.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkRow
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.IconButtonWithTooltip
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListDataUtils
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.entry.form.LongTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationResultEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid
import artistalleydatabase.modules.utils_compose.generated.resources.Res as UtilsComposeRes

object ArtistEditScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        artistId: Uuid?,
        graph: ArtistAlleyEditGraph,
        onClickBack: () -> Unit,
        onClickEditImages: (displayName: String, List<EditImage>) -> Unit,
        viewModel: ArtistEditViewModel = viewModel {
            graph.artistEditViewModelFactory.create(
                dataYear = dataYear,
                artistId = artistId ?: Uuid.random(),
                mode = if (artistId == null) Mode.ADD else Mode.EDIT,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        NavigationResultEffect(ImagesEditScreen.RESULT_KEY) {
            val images = withContext(PlatformDispatchers.IO) {
                Json.decodeFromString<List<EditImage>>(it)
            }
            Snapshot.withMutableSnapshot {
                viewModel.state.images.clear()
                viewModel.state.images += images
            }
        }
        ArtistEditScreen(
            dataYear = dataYear,
            mode = viewModel.mode,
            state = viewModel.state,
            seriesPredictions = viewModel::seriesPredictions,
            merchPredictions = viewModel::merchPredictions,
            seriesImage = viewModel::seriesImage,
            onClickBack = onClickBack,
            onClickEditImages = {
                onClickEditImages(viewModel.state.textState.name.value.text.toString(), it)
            },
            onClickSave = viewModel::onClickSave,
        )
    }

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        mode: Mode,
        state: State,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: () -> Unit,
        onClickEditImages: (List<EditImage>) -> Unit,
        onClickSave: () -> Unit,
    ) {
        val windowSizeClass = currentWindowSizeClass()
        val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val text = when (mode) {
                            Mode.ADD ->
                                stringResource(
                                    Res.string.alley_artist_edit_title_adding,
                                    stringResource(dataYear.shortName),
                                )
                            Mode.EDIT ->
                                stringResource(
                                    Res.string.alley_artist_edit_title_editing,
                                    stringResource(dataYear.shortName),
                                    state.textState.name.value.text.ifBlank { state.textState.id.value.text.toString() },
                                )
                        }
                        Text(text)
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = onClickBack) },
                    actions = {
                        IconButton(onClick = onClickSave) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(Res.string.alley_edit_artist_action_save_content_description),
                            )
                        }
                    },
                    modifier = Modifier
                        .conditionally(!isExpanded, Modifier.widthIn(max = 960.dp))
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            val imagePagerState = rememberImagePagerState(state.images, 0)
            val form = remember {
                movableContentOf { modifier: Modifier ->
                    Form(
                        state = state,
                        seriesPredictions = seriesPredictions,
                        merchPredictions = merchPredictions,
                        seriesImage = seriesImage,
                        modifier = modifier,
                    )
                }
            }
            if (isExpanded) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                        .padding(it)
                ) {
                    form(
                        Modifier.fillMaxHeight()
                            .width(800.dp)
                            .verticalScroll(rememberScrollState())
                    )
                    Column {
                        EditImagesButton(
                            onClick = { onClickEditImages(state.images.toList()) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        ImageGrid(
                            images = state.images,
                            onClickImage = {},
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                Box(
                    contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()
                        .padding(it)
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 960.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        EditImagesButton(
                            onClick = { onClickEditImages(state.images.toList()) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        ImagesPager(
                            sharedElementId = state.textState.id.value.text.toString(),
                            images = state.images,
                            imagePagerState = imagePagerState,
                            onClickImage = {},
                        )
                        form(Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }

    @Composable
    private fun EditImagesButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
        FilledTonalButton(onClick = onClick, modifier = modifier.padding(16.dp)) {
            Text(stringResource(Res.string.alley_artist_edit_action_images))
        }
    }

    @Composable
    private fun ImagesPager(
        sharedElementId: Any,
        images: List<EditImage>,
        imagePagerState: PagerState,
        onClickImage: (EditImage) -> Unit,
    ) {
        ImagePager(
            images = images,
            pagerState = imagePagerState,
            sharedElementId = sharedElementId,
            onClickPage = { onClickImage(images[it]) },
            imageContentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().height(400.dp)
        )
    }

    @Composable
    private fun Form(
        state: State,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        modifier: Modifier = Modifier,
    ) {
        val textState = state.textState
        EntryForm2(modifier = modifier) {
            SingleTextSection(textState.id, Res.string.alley_artist_edit_id)
            SingleTextSection(textState.booth, Res.string.alley_artist_edit_booth)
            SingleTextSection(textState.name, Res.string.alley_artist_edit_name)
            SingleTextSection(textState.summary, Res.string.alley_artist_edit_summary)
            LinksSection(
                state = textState.links,
                title = Res.string.alley_artist_edit_links,
                items = state.links,
            )
            LinksSection(
                state = textState.storeLinks,
                title = Res.string.alley_artist_edit_store_links,
                items = state.storeLinks,
            )
            MultiTextSection(
                state = textState.catalogLinks,
                title = Res.string.alley_artist_edit_catalog_links,
                items = state.catalogLinks,
                itemToText = { it },
            )
            MultiTextSection(
                state = textState.commissions,
                title = Res.string.alley_artist_edit_commissions,
                items = state.commissions,
                itemToText = { it },
            )
            SeriesSection(
                state = textState.seriesInferred,
                title = Res.string.alley_artist_edit_series_inferred,
                items = state.seriesInferred,
                predictions = seriesPredictions,
                image = seriesImage,
            )
            SeriesSection(
                state = textState.seriesConfirmed,
                title = Res.string.alley_artist_edit_series_confirmed,
                items = state.seriesConfirmed,
                predictions = seriesPredictions,
                image = seriesImage,
            )
            MultiTextSection(
                state = textState.merchInferred,
                title = Res.string.alley_artist_edit_merch_inferred,
                items = state.merchInferred,
                predictions = merchPredictions,
                itemToText = { it.name },
            )
            MultiTextSection(
                state = textState.merchConfirmed,
                title = Res.string.alley_artist_edit_merch_confirmed,
                items = state.merchConfirmed,
                predictions = merchPredictions,
                itemToText = { it.name },
            )
            LongTextSection(
                textState.notes,
                headerText = {
                    Text(stringResource(Res.string.alley_artist_edit_notes))
                },
            )
        }
    }

    @Composable
    private fun EntryFormScope.SingleTextSection(
        state: EntryForm2.SingleTextState,
        title: StringResource,
    ) {
        SingleTextSection(state = state, headerText = { Text(stringResource(title)) })
    }

    @Composable
    private fun <T> EntryFormScope.MultiTextSection(
        state: EntryForm2.PendingTextState,
        title: StringResource,
        items: SnapshotStateList<T>,
        predictions: suspend (String) -> Flow<List<T>> = { emptyFlow() },
        itemToText: (T) -> String,
    ) {
        MultiTextSection(
            state = state,
            title = title,
            items = items,
            predictions = predictions,
            removeLastItem = { items.removeLastOrNull()?.let { itemToText(it) } },
            item = { item ->
                Box {
                    TextField(
                        value = itemToText(item),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Box {
                                var showMenu by remember { mutableStateOf(false) }
                                MenuIcon(
                                    visible = state.lockState.editable,
                                    onClick = { showMenu = true },
                                )

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.alley_artist_edit_action_delete)) },
                                        onClick = {
                                            items.remove(item)
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            prediction = { Text(text = itemToText(it)) },
        )
    }

    @Composable
    private fun <T> EntryFormScope.MultiTextSection(
        state: EntryForm2.PendingTextState,
        title: StringResource,
        items: SnapshotStateList<T>,
        predictions: suspend (String) -> Flow<List<T>> = { emptyFlow() },
        removeLastItem: () -> String?,
        item: @Composable (T) -> Unit,
        prediction: @Composable (T) -> Unit,
    ) {
        MultiTextSection(
            state = state,
            headerText = { Text(stringResource(title)) },
            entryPredictions = predictions,
            items = items,
            onItemCommitted = { },
            removeLastItem = removeLastItem,
            prediction = prediction,
            preferPrediction = true,
            item = item,
        )
    }

    @Composable
    private fun EntryFormScope.SeriesSection(
        state: EntryForm2.PendingTextState,
        title: StringResource,
        items: SnapshotStateList<SeriesInfo>,
        predictions: suspend (String) -> Flow<List<SeriesInfo>>,
        image: (SeriesInfo) -> String?,
    ) {
        MultiTextSection(
            state = state,
            title = title,
            items = items,
            predictions = predictions,
            removeLastItem = { items.removeLastOrNull()?.titlePreferred },
            prediction = { Text(it.titlePreferred) },
            item = { SeriesRow(series = it, image = { image(it) }) },
        )
    }

    @Composable
    private fun MenuIcon(visible: Boolean, onClick: () -> Unit) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(
                        UtilsComposeRes.string.more_actions_content_description
                    ),
                )
            }
        }
    }

    @Composable
    private fun EntryFormScope.LinksSection(
        state: EntryForm2.PendingTextState,
        title: StringResource,
        items: SnapshotStateList<LinkModel>,
    ) {
        MultiTextSection(
            state = state,
            headerText = { Text(stringResource(title)) },
            items = items,
            onItemCommitted = {},
            removeLastItem = { items.removeLastOrNull()?.link },
            item = { LinkRow(it, isLast = false) },
        )
    }

    @Composable
    fun SeriesRow(
        series: SeriesInfo,
        image: () -> String?,
        modifier: Modifier = Modifier,
        textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.height(IntrinsicSize.Min)
        ) {
            AsyncImage(
                model = image(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxHeight()
                    .width(56.dp)
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            val languageOptionMedia = LocalLanguageOptionMedia.current
            val colorScheme = MaterialTheme.colorScheme
            val title = remember(series, languageOptionMedia, colorScheme) {
                val name = series.name(languageOptionMedia)
                val otherTitles = listOf(
                    series.titlePreferred,
                    series.titleEnglish,
                    series.titleRomaji,
                    series.titleNative,
                ).distinct() - name
                buildAnnotatedString {
                    withStyle(SpanStyle(color = colorScheme.secondary)) {
                        append(name)
                    }
                    if (otherTitles.isNotEmpty()) {
                        otherTitles.forEach {
                            append(" / ")
                            append(it)
                        }
                    }
                }
            }
            Text(
                text = title,
                style = textStyle,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            val uriHandler = LocalUriHandler.current
            if (series.aniListId != null) {
                val mediaType = when (series.aniListType) {
                    "ANIME" -> MediaType.ANIME
                    "MANGA" -> MediaType.MANGA
                    else -> MediaType.UNKNOWN__
                }
                val icon = when (series.aniListType) {
                    "ANIME" -> Icons.Default.Monitor
                    "MANGA" -> Icons.Default.Book
                    else -> Icons.Default.Monitor
                }
                val aniListUrl = AniListDataUtils.mediaUrl(mediaType, series.aniListId.toString())
                IconButtonWithTooltip(
                    imageVector = icon,
                    tooltipText = aniListUrl,
                    onClick = { uriHandler.openUri(aniListUrl) },
                    allowPopupHover = false,
                )
            }
        }
    }

    enum class Mode {
        ADD, EDIT
    }

    @Stable
    class State(
        val images: SnapshotStateList<EditImage>,
        val links: SnapshotStateList<LinkModel>,
        val storeLinks: SnapshotStateList<LinkModel>,
        val catalogLinks: SnapshotStateList<String>,
        val commissions: SnapshotStateList<String>,
        val seriesInferred: SnapshotStateList<SeriesInfo>,
        val seriesConfirmed: SnapshotStateList<SeriesInfo>,
        val merchInferred: SnapshotStateList<MerchInfo>,
        val merchConfirmed: SnapshotStateList<MerchInfo>,
        val textState: TextState,
    ) {
        @Stable
        class TextState(
            val id: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val booth: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val name: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val summary: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val links: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val storeLinks: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val catalogLinks: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val commissions: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val seriesInferred: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val seriesConfirmed: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val merchInferred: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val merchConfirmed: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val notes: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
        ) {
            object Saver : ComposeSaver<TextState, List<Any>> {
                override fun SaverScope.save(value: TextState) = listOf(
                    with(EntryForm2.SingleTextState.Saver) { save(value.id) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.booth) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.name) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.summary) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.links) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.storeLinks) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.catalogLinks) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.commissions) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.seriesInferred) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.seriesConfirmed) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.merchInferred) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.merchConfirmed) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.notes) },
                )

                override fun restore(value: List<Any>) = TextState(
                    id = with(EntryForm2.SingleTextState.Saver) { restore(value[0]) },
                    booth = with(EntryForm2.SingleTextState.Saver) { restore(value[1]) },
                    name = with(EntryForm2.SingleTextState.Saver) { restore(value[2]) },
                    summary = with(EntryForm2.SingleTextState.Saver) { restore(value[3]) },
                    links = with(EntryForm2.PendingTextState.Saver) { restore(value[4]) },
                    storeLinks = with(EntryForm2.PendingTextState.Saver) { restore(value[5]) },
                    catalogLinks = with(EntryForm2.PendingTextState.Saver) { restore(value[6]) },
                    commissions = with(EntryForm2.PendingTextState.Saver) { restore(value[7]) },
                    seriesInferred = with(EntryForm2.PendingTextState.Saver) { restore(value[8]) },
                    seriesConfirmed = with(EntryForm2.PendingTextState.Saver) { restore(value[9]) },
                    merchInferred = with(EntryForm2.PendingTextState.Saver) { restore(value[10]) },
                    merchConfirmed = with(EntryForm2.PendingTextState.Saver) { restore(value[11]) },
                    notes = with(EntryForm2.PendingTextState.Saver) { restore(value[12]) },
                )
            }
        }
    }
}
