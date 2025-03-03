package com.thekeeperofpie.artistalleydatabase.anime.media.data.filter

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.anime.media.data.generated.resources.Res
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_clear_media
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_search_media_placeholder_anime
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_search_media_placeholder_manga
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_search_media_placeholder_media
import com.anilist.data.MediaAutocompleteQuery.Data.Page.Medium
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaNavigationDataImpl
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toIcon
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toIconContentDescription
import com.thekeeperofpie.artistalleydatabase.anime.ui.MediaCoverImage
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.debounceState
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapState
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterExpandedState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

// TODO: Refactor this code so it can be shared with AADB functionality
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
class MediaSearchSortFilterSection(
    id: String = "mediaSearch",
    private val titleTextRes: StringResource,
    private val titleDropdownContentDescriptionRes: StringResource,
    scope: CoroutineScope,
    aniListApi: AuthedAniListApi,
    mediaDataSettings: MediaDataSettings,
    private val mediaType: MediaType?,
    private val mediaSharedElement: Boolean = true,
    private val mediaDetailsRoute: MediaDetailsRoute,
    private val mediaSelected: MutableStateFlow<MediaNavigationDataImpl?>,
    private val query: MutableStateFlow<String>,
) : SortFilterSectionState.Custom(id) {

    private var predictions = combineStates(query, mediaDataSettings.showAdult, ::Pair)
        .debounceState(scope, 2.seconds)
        .mapState(scope, initialValue = { emptyList() }, mapping = { (query, showAdult) ->
            if (query.isEmpty()) {
                emptyList()
            } else {
                withContext(CustomDispatchers.IO) {
                    runCatching {
                        // TODO: Pagination
                        aniListApi.mediaAutocomplete(
                            query = query,
                            isAdult = if (showAdult) null else false,
                            mediaType = mediaType,
                        ).page?.media?.filterNotNull().orEmpty()
                    }.getOrElse { emptyList() }
                }
            }
        })

    override fun clear() {
        mediaSelected.value = null
    }

    @Composable
    override fun isDefault() = mediaSelected.collectAsMutableStateWithLifecycle().value == null

    @Composable
    override fun Content(state: SortFilterExpandedState, showDivider: Boolean) {
        Column {
            val interactionSource = remember { MutableInteractionSource() }
            val expanded = state.expandedState[id] ?: false
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { state.expandedState[id] = !expanded }
                    .heightIn(min = 32.dp)
            ) {
                Text(
                    text = stringResource(titleTextRes),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                TrailingDropdownIconButton(
                    expanded = expanded,
                    contentDescription = stringResource(titleDropdownContentDescriptionRes),
                    onClick = { state.expandedState[id] = !expanded },
                    modifier = Modifier.align(Alignment.Top),
                )
            }

            var selectedMedia by mediaSelected.collectAsMutableStateWithLifecycle()
            if (expanded) {
                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                    var query by query.collectAsMutableStateWithLifecycle()
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = {
                            Text(
                                stringResource(
                                    when (mediaType) {
                                        MediaType.ANIME -> Res.string.anime_media_filter_search_media_placeholder_anime
                                        MediaType.MANGA -> Res.string.anime_media_filter_search_media_placeholder_manga
                                        MediaType.UNKNOWN__,
                                        null,
                                            -> Res.string.anime_media_filter_search_media_placeholder_media
                                    }
                                )
                            )
                        },
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    )

                    val focused by interactionSource.collectIsFocusedAsState()
                    val focusManager = LocalFocusManager.current
                    val isImeVisible = WindowInsets.isImeVisibleKmp
                    BackHandler(enabled = focused && !isImeVisible) {
                        focusManager.clearFocus()
                    }

                    val media by predictions.collectAsStateWithLifecycle()
                    DropdownMenu(
                        expanded = focused && media.isNotEmpty(),
                        onDismissRequest = {
                            // This callback is invoked whenever the query changes,
                            // which makes it unusable if the user is typing
                            if (!isImeVisible) {
                                focusManager.clearFocus()
                            }
                        },
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        media.forEach {
                            MediaDropdownItem(
                                media = it,
                                onClick = {
                                    selectedMedia = MediaNavigationDataImpl(it)
                                    focusManager.clearFocus(true)
                                },
                            )
                        }
                    }
                }
            }

            selectedMedia?.let {
                val navigationController = LocalNavigationController.current
                val languageOptionMedia = LocalLanguageOptionMedia.current
                val sharedTransitionKey =
                    SharedTransitionKey.makeKeyForId(it.id.toString())
                val coverImageState = rememberCoilImageState(it.coverImage?.extraLarge)
                OutlinedCard(
                    onClick = {
                        navigationController.navigate(
                            mediaDetailsRoute(
                                it,
                                coverImageState.toImageState(),
                                languageOptionMedia,
                                sharedTransitionKey,
                            )
                        )
                    },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MediaContent(
                            media = it,
                            imageState = coverImageState,
                            sharedTransitionKey = sharedTransitionKey,
                            clipImage = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { selectedMedia = null },
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(
                                    Res.string.anime_media_filter_clear_media
                                ),
                            )
                        }
                    }
                }
            }
        }

        if (showDivider) {
            HorizontalDivider()
        }
    }

    @Composable
    private fun MediaDropdownItem(
        media: Medium,
        onClick: (Medium) -> Unit,
    ) {
        DropdownMenuItem(
            onClick = { onClick(media) },
            text = { MediaContent(media = media) },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        )
    }

    @Composable
    private fun MediaContent(
        media: MediaNavigationData,
        modifier: Modifier = Modifier,
        imageState: CoilImageState = rememberCoilImageState(media.coverImage?.extraLarge),
        clipImage: Boolean = false,
        sharedTransitionKey: SharedTransitionKey? = null,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.animateContentSize(),
        ) {
            MediaCoverImage(
                imageState = imageState,
                modifier = Modifier
                    .conditionally(mediaSharedElement) {
                        sharedElement(sharedTransitionKey, "media_image")
                    }
                    .fillMaxHeight()
                    .conditionally(clipImage) {
                        clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    }
                    .heightIn(min = 54.dp)
                    .width(42.dp),
                contentScale = ContentScale.FillWidth
            )

            Column(
                Modifier
                    .weight(1f)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 8.dp,
                    )
            ) {
                Text(
                    text = media.title?.primaryTitle().orEmpty(),
                    maxLines = 1,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Icon(
                imageVector = media.type.toIcon(),
                contentDescription = media.type.toIconContentDescription(),
            )
        }
    }
}
