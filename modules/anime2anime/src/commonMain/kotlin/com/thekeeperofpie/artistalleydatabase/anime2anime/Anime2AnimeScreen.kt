package com.thekeeperofpie.artistalleydatabase.anime2anime

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachReversed
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.generated.resources.anime_character_image_long_press_preview
import artistalleydatabase.modules.anime.generated.resources.anime_staff_image_long_press_preview
import artistalleydatabase.modules.anime2anime.generated.resources.Res
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_app_bar_title
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_game_tab_custom
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_game_tab_daily
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_game_tab_random
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_game_tab_user_list
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_instructions
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_media_name_placeholder
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_media_reset_custom
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_media_reset_random
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_media_reset_user_list
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_media_show_characters_content_description
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_media_show_staff_content_description
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_retry
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_starting_media_header
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_submit_error_failed_to_load
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_submit_error_media_not_found
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_submit_error_no_connection
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_submit_error_same_media
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_submit_media_content_description
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_submit_restart_button
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_swap_start_target_content_description
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_target_media_header
import com.anilist.fragment.AniListMedia
import com.anilist.fragment.CharacterNavigationData
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.StaffNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeStrings
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.character.CharactersSection
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffListRow
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.CharacterCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.anime2anime.game.GameContinuation
import com.thekeeperofpie.artistalleydatabase.entry.EntryPrefilledAutocompleteDropdown
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.OnChangeEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionPrefixKeys
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class,
)
object Anime2AnimeScreen {

    private val COLUMN_MAX_WIDTH = 460.dp

    @Composable
    operator fun invoke(
        viewModel: Anime2AnimeViewModel,
        upIconOption: UpIconOption?,
    ) = this(
        upIconOption = upIconOption,
        viewer = { viewModel.viewer.collectAsState().value },
        selectedTab = { viewModel.selectedTab },
        onSelectedTabChange = { viewModel.selectedTab = it },
        onSwitchStartTargetClick = viewModel::onSwitchStartTargetClick,
        options = { viewModel.currentGame().options },
        optionsState = { viewModel.currentGame().optionsState },
        startMedia = { viewModel.currentGame().state.startMedia.media },
        startMediaCustomText = { viewModel.currentGame().state.startMedia.customText },
        onStartMediaCustomTextChange = { viewModel.currentGame().state.startMedia.customText = it },
        startMediaCustomPredictions = { viewModel.currentGame().state.startMedia.customPredictions },
        onStartMediaRefresh = { viewModel.currentGame().refreshStart() },
        onStartMediaReset = { viewModel.currentGame().resetStart() },
        onStartMediaChooseCustomMedia = { viewModel.currentGame().onChooseStartMedia(it) },
        targetMedia = { viewModel.currentGame().state.targetMedia.media },
        targetMediaCustomText = { viewModel.currentGame().state.targetMedia.customText },
        onTargetMediaCustomTextChange = {
            viewModel.currentGame().state.targetMedia.customText = it
        },
        targetMediaCustomPredictions = { viewModel.currentGame().state.targetMedia.customPredictions },
        onTargetMediaRefresh = { viewModel.currentGame().refreshTarget() },
        onTargetMediaReset = { viewModel.currentGame().resetTarget() },
        onTargetMediaChooseCustomMedia = { viewModel.currentGame().onChooseTargetMedia(it) },
        continuations = { viewModel.currentGame().state.continuations },
        lastSubmitResult = { viewModel.currentGame().state.lastSubmitResult },
        text = { viewModel.text },
        onTextChange = { viewModel.text = it },
        predictions = { viewModel.predictions },
        onRefresh = viewModel::onRefresh,
        onSubmitMedia = viewModel::onSubmit,
        onChooseMedia = viewModel::onChooseMedia,
        onRestart = viewModel::onRestart,
    )

    @Suppress("NAME_SHADOWING")
    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        viewer: @Composable () -> AniListViewer?,
        selectedTab: () -> GameTab,
        onSelectedTabChange: (GameTab) -> Unit,
        onSwitchStartTargetClick: () -> Unit,
        options: () -> List<SortFilterSection>,
        optionsState: () -> SortFilterSection.ExpandedState,
        startMedia: () -> LoadingResult<GameContinuation>,
        startMediaCustomText: () -> String,
        onStartMediaCustomTextChange: (String) -> Unit,
        startMediaCustomPredictions: () -> List<EntrySection.MultiText.Entry.Prefilled<AniListMedia>>,
        onStartMediaRefresh: () -> Unit,
        onStartMediaReset: () -> Unit,
        onStartMediaChooseCustomMedia: (AniListMedia) -> Unit,
        targetMedia: () -> LoadingResult<GameContinuation>,
        targetMediaCustomText: () -> String,
        onTargetMediaCustomTextChange: (String) -> Unit,
        targetMediaCustomPredictions: () -> List<EntrySection.MultiText.Entry.Prefilled<AniListMedia>>,
        onTargetMediaRefresh: () -> Unit,
        onTargetMediaReset: () -> Unit,
        onTargetMediaChooseCustomMedia: (AniListMedia) -> Unit,
        continuations: () -> List<GameContinuation>,
        lastSubmitResult: () -> Anime2AnimeSubmitResult,
        text: () -> String,
        onTextChange: (String) -> Unit,
        predictions: () -> List<EntrySection.MultiText.Entry.Prefilled<AniListMedia>>,
        onRefresh: () -> Unit,
        onSubmitMedia: () -> Unit,
        onChooseMedia: (AniListMedia) -> Unit,
        onRestart: () -> Unit,
    ) {
        val animeComponent = LocalAnimeComponent.current
        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
        MediaEditBottomSheetScaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            viewModel = editViewModel,
            topBar = {
                EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(Res.string.anime2anime_app_bar_title)
                            )
                        },
                        navigationIcon = { upIconOption?.let { UpIconButton(it) } },
                    )
                }
            }
        ) {
            Box {
                val refreshing by remember { derivedStateOf { startMedia().loading || targetMedia().loading } }
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
                    OnChangeEffect(lastSubmitResult() to continuations.size) {
                        if (continuations.isNotEmpty()) {
                            listState.animateScrollToItem(0, 0)
                        }
                    }

                    val canRefresh by remember { derivedStateOf { !startMedia().success || !targetMedia().success } }
                    LazyColumn(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        reverseLayout = true,
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .pullRefresh(
                                state = pullRefreshState,
                                enabled = canRefresh,
                            )
                    ) {
                        lastSubmitResultText(lastSubmitResult, onRestart)

                        // TODO: Filter/handle duplicates
                        continuations.fastForEachReversed {
                            item {
                                AnimeMediaListRow(
                                    entry = it.media,
                                    viewer = viewer,
                                    modifier = Modifier
                                        .animateItem()
                                        .padding(start = 16.dp, end = 16.dp),
                                    onClickListEdit = editViewModel::initialize
                                )
                            }
                            connections(it.connections)
                        }

                        mediaRow(
                            key = "startingMedia",
                            selectedTab = selectedTab,
                            viewer = viewer,
                            continuationResult = startMedia(),
                            onClickListEdit = editViewModel::initialize,
                            customText = startMediaCustomText,
                            onCustomTextChange = onStartMediaCustomTextChange,
                            onRefresh = onStartMediaRefresh,
                            customPredictions = startMediaCustomPredictions,
                            onClickReset = onStartMediaReset,
                            onChooseCustomMedia = onStartMediaChooseCustomMedia,
                        )
                        item(key = "startingMediaHeader") {
                            Text(
                                text = stringResource(Res.string.anime2anime_starting_media_header),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier
                                    .animateItem()
                                    .align(Alignment.CenterHorizontally)
                                    .padding(start = 16.dp, end = 16.dp, top = 4.dp)
                            )
                        }

                        mediaRow(
                            key = "targetMedia",
                            selectedTab = selectedTab,
                            viewer = viewer,
                            continuationResult = targetMedia(),
                            onClickListEdit = editViewModel::initialize,
                            customText = targetMediaCustomText,
                            onCustomTextChange = onTargetMediaCustomTextChange,
                            onRefresh = onTargetMediaRefresh,
                            customPredictions = targetMediaCustomPredictions,
                            onClickReset = onTargetMediaReset,
                            onChooseCustomMedia = onTargetMediaChooseCustomMedia,
                        )

                        item(key = "targetMediaHeader") {
                            Text(
                                text = stringResource(Res.string.anime2anime_target_media_header),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier
                                    .animateItem()
                                    .align(Alignment.CenterHorizontally)
                                    .padding(start = 16.dp, end = 16.dp, top = 4.dp)
                            )
                        }

                        val options = options()
                        if (options.isNotEmpty()) {
                            item(key = "options") {
                                OutlinedCard(
                                    modifier = Modifier
                                        .animateItem()
                                        .padding(horizontal = 16.dp)
                                        .width(COLUMN_MAX_WIDTH)
                                ) {
                                    val state = optionsState()
                                    Column {
                                        options.forEach {
                                            it.Content(state, showDivider = true)
                                        }
                                    }
                                }
                            }
                        }

                        item(key = "gameVariant") {
                            GameVariantRow(
                                viewer = viewer,
                                selectedTab = selectedTab,
                                onSelectedTabChange = onSelectedTabChange,
                                onSwitchStartTargetClick = onSwitchStartTargetClick,
                                modifier = Modifier.animateItem()
                            )
                        }

                        item(key = "instructions") {
                            Text(
                                text = stringResource(Res.string.anime2anime_instructions),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .animateItem()
                                    .widthIn(max = COLUMN_MAX_WIDTH)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }

                    val textFieldText = text()
                    val predictions = predictions()
                    EntryPrefilledAutocompleteDropdown(
                        text = text,
                        predictions = predictions,
                        showPredictions = {
                            textFieldText.isNotEmpty()
                                    && lastSubmitResult() != Anime2AnimeSubmitResult.Loading
                        },
                        onPredictionChosen = { onChooseMedia(predictions[it].value) },
                    ) {
                        TextField(
                            value = textFieldText,
                            enabled = lastSubmitResult() != Anime2AnimeSubmitResult.Finished,
                            onValueChange = onTextChange,
                            placeholder = {
                                Text(stringResource(Res.string.anime2anime_media_name_placeholder))
                            },
                            maxLines = 1,
                            trailingIcon = {
                                IconButton(
                                    onClick = onSubmitMedia,
                                    enabled = lastSubmitResult() != Anime2AnimeSubmitResult.Finished,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = stringResource(
                                            Res.string.anime2anime_submit_media_content_description
                                        )
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { onSubmitMedia() }),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryEditable)
                                .fillMaxWidth()
                                .onKeyEvent {
                                    if (it.type == KeyEventType.KeyUp && it.key == Key.Enter) {
                                        onSubmitMedia()
                                        true
                                    } else false
                                }
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

    private fun LazyListScope.connections(connections: List<GameContinuation.Connection>) {
        connections.fastForEachReversed {
            when (it) {
                // TODO: Key with ID (scoped to parent media with uniqueness)
                is GameContinuation.Connection.Character -> item {
                    CharacterRow(
                        previousCharacter = it.previousCharacter,
                        character = it.character,
                        voiceActor = it.voiceActor,
                        modifier = Modifier.animateItem()
                    )
                }
                is GameContinuation.Connection.Staff -> item {
                    StaffRow(
                        staff = it.staff,
                        previousRole = it.previousRole,
                        role = it.role,
                        modifier = Modifier.animateItem()
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
                val voiceActorName = voiceActor.name?.primaryName()
                val voiceActorSubtitle = voiceActor.name?.subtitleName()
                val coverImageState = rememberCoilImageState(voiceActor.image?.large)
                val sharedTransitionKey = SharedTransitionKey.makeKeyForId(voiceActor.id.toString())
                StaffCoverImage(
                    imageState = coverImageState,
                    image = coverImageState.request().build(),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .fillMaxHeight()
                        .width(48.dp)
                        .combinedClickable(
                            onClick = {
                                navigationCallback.navigate(
                                    AnimeDestination.StaffDetails(
                                        staffId = voiceActor.id.toString(),
                                        sharedTransitionKey = sharedTransitionKey,
                                        headerParams = StaffHeaderParams(
                                            name = voiceActorName,
                                            subtitle = voiceActorSubtitle,
                                            coverImage = coverImageState.toImageState(),
                                            favorite = null,
                                        )
                                    )
                                )
                            },
                            onLongClick = {
                                voiceActor.image?.large?.let(fullscreenImageHandler::openImage)
                            },
                            onLongClickLabel = stringResource(
                                AnimeStrings.anime_staff_image_long_press_preview
                            ),
                        ),
                    contentScale = ContentScale.Crop
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
        val characterName = character.name?.primaryName()
        val subtitleName = character.name?.subtitleName()
        val coverImageState = rememberCoilImageState(character.image?.large)
        val sharedTransitionKey = SharedTransitionKey.makeKeyForId(character.id.toString())
        val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
        CharacterCoverImage(
            imageState = coverImageState,
            image = coverImageState.request().build(),
            modifier = Modifier
                .fillMaxHeight()
                .width(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .sharedElement(sharedTransitionKey, "character_image")
                .combinedClickable(
                    onClick = {
                        navigationCallback.navigate(
                            AnimeDestination.CharacterDetails(
                                characterId = character.id.toString(),
                                sharedTransitionScopeKey = sharedTransitionScopeKey,
                                headerParams = CharacterHeaderParams(
                                    name = characterName,
                                    subtitle = subtitleName,
                                    coverImage = coverImageState.toImageState(),
                                    favorite = null,
                                )
                            )
                        )
                    },
                    onLongClick = {
                        character.image?.large?.let(fullscreenImageHandler::openImage)
                    },
                    onLongClickLabel = stringResource(
                        AnimeStrings.anime_character_image_long_press_preview
                    ),
                ),
            contentScale = ContentScale.Crop
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
        val staffName = staff.name?.primaryName()
        val staffSubtitle = staff.name?.subtitleName()
        val coverImageState = rememberCoilImageState(staff.image?.large)
        val sharedTransitionKey = SharedTransitionKey.makeKeyForId(staff.id.toString())
        OutlinedCard(
            onClick = {
                navigationCallback.navigate(
                    AnimeDestination.StaffDetails(
                        staffId = staff.id.toString(),
                        sharedTransitionKey = sharedTransitionKey,
                        headerParams = StaffHeaderParams(
                            name = staffName,
                            subtitle = staffSubtitle,
                            coverImage = coverImageState.toImageState(),
                            favorite = null,
                        )
                    )
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
                    imageState = coverImageState,
                    image = coverImageState.request().build(),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(48.dp)
                        .sharedElement(sharedTransitionKey, "staff_image")
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .align(Alignment.CenterVertically)
                        .combinedClickable(
                            onClick = {
                                navigationCallback.navigate(
                                    AnimeDestination.StaffDetails(
                                        staffId = staff.id.toString(),
                                        sharedTransitionKey = sharedTransitionKey,
                                        headerParams = StaffHeaderParams(
                                            name = staffName,
                                            subtitle = staffSubtitle,
                                            coverImage = coverImageState.toImageState(),
                                            favorite = null,
                                        )
                                    )
                                )
                            },
                            onLongClick = {
                                staff.image?.large?.let(fullscreenImageHandler::openImage)
                            },
                            onLongClickLabel = stringResource(
                                AnimeStrings.anime_staff_image_long_press_preview
                            ),
                        ),
                    contentScale = ContentScale.Crop
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

    private fun LazyListScope.lastSubmitResultText(
        lastSubmitResult: () -> Anime2AnimeSubmitResult,
        onRestart: () -> Unit,
    ) {
        item(key = "lastSubmitResult") {
            val modifier = Modifier
                .animateItem()
                .padding(horizontal = 24.dp)
            @Suppress("NAME_SHADOWING")
            when (val lastSubmitResult = lastSubmitResult()) {
                Anime2AnimeSubmitResult.None,
                Anime2AnimeSubmitResult.Success,
                -> Unit
                Anime2AnimeSubmitResult.Loading -> CircularProgressIndicator()
                Anime2AnimeSubmitResult.Finished -> {
                    Button(onClick = onRestart, modifier = modifier) {
                        Text(
                            text = stringResource(
                                Res.string.anime2anime_submit_restart_button,
                            ),
                        )
                    }
                }
                is Anime2AnimeSubmitResult.NoConnection -> {
                    Text(
                        text = stringResource(
                            Res.string.anime2anime_submit_error_no_connection,
                            lastSubmitResult.media.title?.primaryTitle().orEmpty(),
                        ),
                        textAlign = TextAlign.Center,
                        modifier = modifier
                    )
                }
                is Anime2AnimeSubmitResult.SameMedia -> {
                    Text(
                        text = stringResource(Res.string.anime2anime_submit_error_same_media),
                        textAlign = TextAlign.Center,
                        modifier = modifier
                    )
                }
                is Anime2AnimeSubmitResult.MediaNotFound -> {
                    Text(
                        text = stringResource(
                            Res.string.anime2anime_submit_error_media_not_found,
                            lastSubmitResult.text,
                        ),
                        textAlign = TextAlign.Center,
                        modifier = modifier
                    )
                }
                is Anime2AnimeSubmitResult.FailedToLoad -> {
                    Text(
                        text = stringResource(
                            Res.string.anime2anime_submit_error_failed_to_load,
                            lastSubmitResult.media.title?.primaryTitle().orEmpty(),
                        ),
                        textAlign = TextAlign.Center,
                        modifier = modifier
                    )
                }
            }
        }
    }

    @Composable
    private fun GameVariantRow(
        viewer: AniListViewer?,
        selectedTab: () -> GameTab,
        onSelectedTabChange: (GameTab) -> Unit,
        onSwitchStartTargetClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        @Suppress("NAME_SHADOWING")
        val selectedTab = selectedTab()
        Row(verticalAlignment = Alignment.Top, modifier = modifier.padding(vertical = 8.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 64.dp, end = 8.dp)
            ) {
                GameTab.entries
                    .filter { it != GameTab.USER_LIST || viewer != null }
                    .forEach {
                        FilterChip(
                            selected = selectedTab == it,
                            onClick = { onSelectedTabChange(it) },
                            label = { Text(text = stringResource(it.textRes)) },
                        )
                    }
            }

            IconButton(
                onClick = onSwitchStartTargetClick,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = stringResource(
                        Res.string.anime2anime_swap_start_target_content_description
                    )
                )
            }
        }
    }

    private fun LazyListScope.mediaRow(
        key: String?,
        selectedTab: () -> GameTab,
        viewer: AniListViewer?,
        continuationResult: LoadingResult<GameContinuation>,
        onRefresh: () -> Unit,
        onClickListEdit: (MediaNavigationData) -> Unit,
        onClickReset: () -> Unit,
        customText: () -> String,
        onCustomTextChange: (String) -> Unit,
        customPredictions: () -> List<EntrySection.MultiText.Entry.Prefilled<AniListMedia>>,
        onChooseCustomMedia: (AniListMedia) -> Unit,
    ) {
        val continuation = continuationResult.result
        if (continuation?.staffExpanded == true) {
            item(key = key?.let { "$it-staff" }) {
                OutlinedCard(
                    modifier = Modifier
                        .animateItem()
                        .animateContentSize()
                        .padding(horizontal = 16.dp)
                        .widthIn(max = COLUMN_MAX_WIDTH)
                ) {
                    StaffListRow(
                        staffList = { continuation.staff.collectAsLazyPagingItems() },
                        contentPadding = PaddingValues(0.dp),
                        // TODO: View all staff
                    )
                }
            }
        }
        if (continuation?.charactersExpanded == true) {
            item(key = key?.let { "$it-characters" }) {
                OutlinedCard(
                    modifier = Modifier
                        .animateItem()
                        .animateContentSize()
                        .padding(horizontal = 16.dp)
                        .widthIn(max = COLUMN_MAX_WIDTH)
                ) {
                    CharactersSection(
                        charactersInitial = emptyList(),
                        charactersDeferred = { continuation.characters.collectAsLazyPagingItems() },
                        contentPadding = PaddingValues(0.dp),
                        showVoiceActorAsMain = true,
                        // TODO: View all characters
                    )
                }
            }
        }

        val error = continuationResult.error
        if (!continuationResult.loading && error != null) {
            item(key = "$key-error") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(error.first))
                    error.second?.let {
                        Text(text = it.stackTraceToString())
                    }
                    Button(onClick = onRefresh) {
                        Text(text = stringResource(Res.string.anime2anime_retry))
                    }
                }
            }
        }

        item(key = key) {
            Column(
                modifier = Modifier
                    .animateItem()
                    .padding(start = 16.dp, end = 16.dp)
                    .widthIn(max = COLUMN_MAX_WIDTH)
            ) {
                when (selectedTab()) {
                    GameTab.DAILY,
                    GameTab.RANDOM,
                    GameTab.USER_LIST,
                    -> AnimeMediaListRow(
                        entry = continuation?.media,
                        viewer = viewer,
                        onClickListEdit = onClickListEdit,
                    )
                    GameTab.CUSTOM -> {
                        if (continuationResult.isEmpty()) {
                            val textFieldText = customText()
                            val predictions = customPredictions()
                            EntryPrefilledAutocompleteDropdown(
                                text = customText,
                                predictions = predictions,
                                showPredictions = textFieldText::isNotEmpty,
                                onPredictionChosen = { onChooseCustomMedia(predictions[it].value) },
                            ) {
                                TextField(
                                    value = textFieldText,
                                    onValueChange = onCustomTextChange,
                                    placeholder = {
                                        Text(stringResource(Res.string.anime2anime_media_name_placeholder))
                                    },
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        predictions.firstOrNull()?.value
                                            ?.let(onChooseCustomMedia)
                                    }),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                                        .fillMaxWidth()
                                        .onKeyEvent {
                                            if (it.type == KeyEventType.KeyUp && it.key == Key.Enter) {
                                                predictions.firstOrNull()?.value
                                                    ?.let(onChooseCustomMedia)
                                                true
                                            } else false
                                        }
                                )
                            }
                        } else {
                            AnimeMediaListRow(
                                entry = continuation?.media,
                                viewer = viewer,
                                onClickListEdit = onClickListEdit,
                            )
                        }
                    }
                }

                MediaActions(
                    continuation = continuation,
                    resetButtonContentDescription = when (selectedTab()) {
                        GameTab.DAILY -> null
                        GameTab.USER_LIST -> Res.string.anime2anime_media_reset_user_list
                        GameTab.RANDOM -> Res.string.anime2anime_media_reset_random
                        GameTab.CUSTOM -> Res.string.anime2anime_media_reset_custom
                            .takeUnless { continuationResult.isEmpty() }
                    },
                    onClickReset = onClickReset,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }

    @Composable
    private fun MediaActions(
        continuation: GameContinuation?,
        resetButtonContentDescription: StringResource?,
        onClickReset: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        if (resetButtonContentDescription != null
            || continuation != null
            && (continuation.hasCharacters || continuation.hasStaff)
        ) {
            Row(modifier = modifier) {
                if (resetButtonContentDescription != null) {
                    IconButton(onClick = onClickReset) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = stringResource(
                                resetButtonContentDescription
                            ),
                        )
                    }
                }

                if (continuation?.hasCharacters == true) {
                    IconButton(onClick = {
                        continuation.charactersExpanded = !continuation.charactersExpanded
                    }) {
                        Icon(
                            imageVector = Icons.Default.PeopleAlt,
                            contentDescription = stringResource(
                                Res.string.anime2anime_media_show_characters_content_description
                            ),
                            tint = if (continuation.charactersExpanded) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                LocalContentColor.current
                            },
                        )
                    }
                }

                if (continuation?.hasStaff == true) {
                    IconButton(onClick = {
                        continuation.staffExpanded = !continuation.staffExpanded
                    }) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = stringResource(
                                Res.string.anime2anime_media_show_staff_content_description
                            ),
                            tint = if (continuation.staffExpanded) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                LocalContentColor.current
                            },
                        )
                    }
                }
            }
        }
    }

    enum class GameTab(val textRes: StringResource) {
        DAILY(Res.string.anime2anime_game_tab_daily),
        RANDOM(Res.string.anime2anime_game_tab_random),
        CUSTOM(Res.string.anime2anime_game_tab_custom),
        USER_LIST(Res.string.anime2anime_game_tab_user_list),
    }
}
