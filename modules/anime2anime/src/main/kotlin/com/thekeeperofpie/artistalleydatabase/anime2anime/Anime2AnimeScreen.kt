package com.thekeeperofpie.artistalleydatabase.anime2anime

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.fragment.AniListMedia
import com.anilist.fragment.CharacterNavigationData
import com.anilist.fragment.StaffNavigationData
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeStringR
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.ui.CharacterCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.entry.EntryPrefilledAutocompleteDropdown
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class
)
object Anime2AnimeScreen {

    private const val SCREEN_KEY = "Anime2Anime"

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        viewModel: Anime2AnimeViewModel = hiltViewModel(),
    ) = this(
        upIconOption = upIconOption,
        viewer = { viewModel.viewer.collectAsState().value },
        startAndTargetMedia = { viewModel.startAndTargetMedia },
        continuations = { viewModel.continuations },
        text = { viewModel.text },
        onTextChange = { viewModel.text = it },
        predictions = { viewModel.predictions },
        onRefresh = viewModel::onRefresh,
        onSubmitMedia = viewModel::onSubmit,
        onChooseMedia = viewModel::onChooseMedia,
    )

    @Suppress("NAME_SHADOWING")
    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        viewer: @Composable () -> AniListViewer?,
        startAndTargetMedia: () -> LoadingResult<Anime2AnimeStartAndTargetMedia>,
        continuations: () -> List<Anime2AnimeContinuation>,
        text: () -> String,
        onTextChange: (String) -> Unit,
        predictions: () -> List<EntrySection.MultiText.Entry.Prefilled<AniListMedia>>,
        onRefresh: () -> Unit,
        onSubmitMedia: () -> Unit,
        onChooseMedia: (AniListMedia) -> Unit,
    ) {
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
        MediaEditBottomSheetScaffold(
            screenKey = SCREEN_KEY,
            viewModel = editViewModel,
            topBar = {
                EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.anime2anime_app_bar_title)
                            )
                        },
                        navigationIcon = { upIconOption?.let { UpIconButton(it) } },
                    )
                }
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Box {
                val startAndTargetMedia = startAndTargetMedia()
                val refreshing = startAndTargetMedia.loading
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = refreshing,
                    onRefresh = onRefresh,
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    val viewer = viewer()
                    val listState = rememberLazyListState()
                    val continuations = continuations()
                    LaunchedEffect(continuations.size) {
                        if (continuations.isNotEmpty()) {
                            listState.animateScrollToItem(continuations.size - 1)
                        }
                    }
                    LazyColumn(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .pullRefresh(pullRefreshState)
                    ) {
                        item(key = "instructions") {
                            Text(
                                text = stringResource(R.string.anime2anime_instructions),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 300.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }

                        item(key = "targetMediaHeader") {
                            Text(
                                text = stringResource(R.string.anime2anime_target_media_header),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                    .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                            )
                        }
                        item(key = "targetMedia") {
                            AnimeMediaListRow(
                                screenKey = SCREEN_KEY,
                                entry = startAndTargetMedia.result?.targetMedia?.media,
                                viewer = viewer,
                                onClickListEdit = editViewModel::initialize,
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .padding(start = 16.dp, end = 16.dp)
                            )
                        }

                        item(key = "startingMediaHeader") {
                            Text(
                                text = stringResource(R.string.anime2anime_starting_media_header),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                    .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                            )
                        }
                        item(key = "startingMedia") {
                            AnimeMediaListRow(
                                screenKey = SCREEN_KEY,
                                entry = startAndTargetMedia.result?.startMedia?.media,
                                viewer = viewer,
                                onClickListEdit = editViewModel::initialize,
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .padding(start = 16.dp, end = 16.dp)
                            )
                        }

                        // TODO: Filter/handle duplicates
                        continuations.forEach {
                            connections(it.connections)
                            item {
                                AnimeMediaListRow(
                                    screenKey = SCREEN_KEY,
                                    entry = it.media,
                                    viewer = viewer,
                                    onClickListEdit = editViewModel::initialize,
                                    modifier = Modifier
                                        .animateItemPlacement()
                                        .padding(start = 16.dp, end = 16.dp)
                                )
                            }
                        }
                    }

                    val textFieldText = text()
                    val predictions = predictions()
                    EntryPrefilledAutocompleteDropdown(
                        text = text,
                        predictions = predictions,
                        showPredictions = { textFieldText.isNotEmpty() },
                        onPredictionChosen = { onChooseMedia(predictions[it].value) },
                    ) {
                        TextField(
                            value = textFieldText,
                            onValueChange = onTextChange,
                            placeholder = {
                                Text(stringResource(R.string.anime2anime_media_name_placeholder))
                            },
                            maxLines = 1,
                            trailingIcon = {
                                IconButton(onClick = onSubmitMedia) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = stringResource(
                                            R.string.anime2anime_submit_media_content_description
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                    }
                }

                PullRefreshIndicator(
                    refreshing = refreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

    private fun LazyListScope.connections(connections: List<Anime2AnimeContinuation.Connection>) {
        connections.forEach {
            when (it) {
                // TODO: Key with ID (scoped to parent media with uniqueness)
                is Anime2AnimeContinuation.Connection.Character -> item {
                    CharacterRow(
                        previousCharacter = it.previousCharacter,
                        character = it.character,
                        voiceActor = it.voiceActor,
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                is Anime2AnimeContinuation.Connection.Staff -> item {
                    StaffRow(
                        staff = it.staff,
                        previousRole = it.previousRole,
                        role = it.role,
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }

    @Composable
    private fun CharacterRow(
        previousCharacter: CharacterNavigationData?,
        character: CharacterNavigationData,
        voiceActor: StaffNavigationData,
        modifier: Modifier = Modifier,
    ) {
        val navigationCallback = LocalNavigationCallback.current
        OutlinedCard(
            modifier = modifier.padding(start = 48.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .heightIn(min = 56.dp)
            ) {
                if (previousCharacter != null) {
                    CharacterImage(previousCharacter)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = previousCharacter?.name?.primaryName().orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Spacer(Modifier.height(12.dp))
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = voiceActor.name?.primaryName().orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.End,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
                val fullscreenImageHandler = LocalFullscreenImageHandler.current
                StaffCoverImage(
                    screenKey = SCREEN_KEY,
                    staffId = voiceActor.id.toString(),
                    image = ImageRequest.Builder(LocalContext.current)
                        .data(voiceActor.image?.large)
                        .crossfade(true)
                        .size(
                            width = Dimension.Pixels(LocalDensity.current.run { 48.dp.roundToPx() }),
                            height = Dimension.Undefined,
                        )
                        .build(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .fillMaxHeight()
                        .width(48.dp)
                        .combinedClickable(
                            onClick = {
                                navigationCallback.onStaffClick(
                                    staff = voiceActor,
                                    favorite = null,
                                    imageWidthToHeightRatio = 1f,
                                    color = null,
                                )
                            },
                            onLongClick = {
                                voiceActor.image?.large?.let(fullscreenImageHandler::openImage)
                            },
                            onLongClickLabel = stringResource(
                                AnimeStringR.anime_staff_image_long_press_preview
                            ),
                        )
                )
                Text(
                    text = character.name?.primaryName().orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
                CharacterImage(character)
            }
        }
    }

    @Composable
    private fun CharacterImage(character: CharacterNavigationData) {
        val navigationCallback = LocalNavigationCallback.current
        val fullscreenImageHandler = LocalFullscreenImageHandler.current
        CharacterCoverImage(
            screenKey = SCREEN_KEY,
            characterId = character.id.toString(),
            image = ImageRequest.Builder(LocalContext.current)
                .data(character.image?.large)
                .crossfade(true)
                .size(
                    width = Dimension.Pixels(LocalDensity.current.run { 48.dp.roundToPx() }),
                    height = Dimension.Undefined
                )
                .build(),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxHeight()
                .width(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .combinedClickable(
                    onClick = {
                        navigationCallback.onCharacterClick(
                            character = character,
                            favorite = null,
                            imageWidthToHeightRatio = 1f,
                            color = null,
                        )
                    },
                    onLongClick = {
                        character.image?.large?.let(fullscreenImageHandler::openImage)
                    },
                    onLongClickLabel = stringResource(
                        AnimeStringR.anime_character_image_long_press_preview
                    ),
                )
        )
    }

    // TODO: Include role
    @Composable
    private fun StaffRow(
        staff: StaffNavigationData,
        previousRole: String?,
        role: String?,
        modifier: Modifier = Modifier,
    ) {
        val navigationCallback = LocalNavigationCallback.current
        OutlinedCard(
            onClick = {
                navigationCallback.onStaffClick(
                    staff = staff,
                    favorite = null,
                    imageWidthToHeightRatio = 1f,
                    color = null,
                )
            },
            modifier = modifier.padding(start = 48.dp, end = 16.dp)
        ) {
            // TODO: Intrinsics don't work properly
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .heightIn(min = 56.dp)
            ) {
                val fullscreenImageHandler = LocalFullscreenImageHandler.current
                StaffCoverImage(
                    screenKey = SCREEN_KEY,
                    staffId = staff.id.toString(),
                    image = ImageRequest.Builder(LocalContext.current)
                        .data(staff.image?.large)
                        .crossfade(true)
                        .size(
                            width = Dimension.Pixels(LocalDensity.current.run { 48.dp.roundToPx() }),
                            height = Dimension.Undefined,
                        )
                        .build(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .align(Alignment.CenterVertically)
                        .fillMaxHeight()
                        .width(48.dp)
                        .combinedClickable(
                            onClick = {
                                navigationCallback.onStaffClick(
                                    staff = staff,
                                    favorite = null,
                                    imageWidthToHeightRatio = 1f,
                                    color = null,
                                )
                            },
                            onLongClick = {
                                staff.image?.large?.let(fullscreenImageHandler::openImage)
                            },
                            onLongClickLabel = stringResource(
                                AnimeStringR.anime_staff_image_long_press_preview
                            ),
                        )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    previousRole?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    Text(
                        text = staff.name?.primaryName().orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                    role?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}
