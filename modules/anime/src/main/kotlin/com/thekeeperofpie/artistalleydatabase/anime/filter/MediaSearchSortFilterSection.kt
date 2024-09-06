package com.thekeeperofpie.artistalleydatabase.anime.filter

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.isImeVisible
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.anilist.MediaAutocompleteQuery.Data.Page.Medium
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toIcon
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toIconContentDescription
import com.thekeeperofpie.artistalleydatabase.anime.ui.MediaCoverImage
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

// TODO: Refactor this code so it can be shared with AADB functionality
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
class MediaSearchSortFilterSection(
    id: String = "mediaSearch",
    @StringRes private val titleTextRes: Int,
    @StringRes private val titleDropdownContentDescriptionRes: Int,
    scope: CoroutineScope,
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    private val mediaType: MediaType?,
    private val mediaSharedElement: Boolean = true,
) : SortFilterSection.Custom(id) {

    var selectedMedia by mutableStateOf<Medium?>(null)
        private set

    private var query by mutableStateOf("")

    private var predictions by mutableStateOf(emptyList<Medium>())

    init {
        scope.launch(CustomDispatchers.Main) {
            combine(
                snapshotFlow { query },
                settings.showAdult,
            ) { query, showAdult ->
                if (query.isEmpty()) {
                    emptyList()
                } else {
                    // TODO: Pagination
                    aniListApi.mediaAutocomplete(
                        query = query,
                        isAdult = if (showAdult) null else false,
                        mediaType = mediaType,
                    ).page?.media?.filterNotNull().orEmpty()
                }
            }
                .catch { emit(emptyList()) }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { predictions = it }
        }
    }

    override fun showingPreview() = selectedMedia != null

    override fun clear() {
        selectedMedia = null
    }

    @Composable
    override fun Content(state: ExpandedState, showDivider: Boolean) {
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

            if (expanded) {
                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = {
                            Text(
                                stringResource(
                                    when (mediaType) {
                                        MediaType.ANIME -> R.string.anime_media_filter_search_media_placeholder_anime
                                        MediaType.MANGA -> R.string.anime_media_filter_search_media_placeholder_manga
                                        MediaType.UNKNOWN__,
                                        null,
                                        -> R.string.anime_media_filter_search_media_placeholder_media
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
                    BackHandler(enabled = focused && !WindowInsets.isImeVisible) {
                        focusManager.clearFocus()
                    }

                    val isImeVisible = WindowInsets.isImeVisible
                    val media = predictions
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
                                    selectedMedia = it
                                    focusManager.clearFocus(true)
                                },
                            )
                        }
                    }
                }
            }

            val selectedMedia = selectedMedia
            if (selectedMedia != null) {
                val navigationCallback = LocalNavigationCallback.current
                val languageOptionMedia = LocalLanguageOptionMedia.current
                val sharedTransitionKey =
                    SharedTransitionKey.makeKeyForId(selectedMedia.id.toString())
                val coverImageState = rememberCoilImageState(selectedMedia.coverImage?.medium)
                OutlinedCard(
                    onClick = {
                        navigationCallback.navigate(
                            AnimeDestination.MediaDetails(
                                mediaNavigationData = selectedMedia,
                                coverImage = coverImageState.toImageState(),
                                languageOptionMedia = languageOptionMedia,
                                sharedTransitionKey = sharedTransitionKey,
                            )
                        )
                    },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MediaContent(
                            media = selectedMedia,
                            imageState = coverImageState,
                            sharedTransitionKey = sharedTransitionKey,
                            clipImage = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { this@MediaSearchSortFilterSection.selectedMedia = null },
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(
                                    R.string.anime_media_filter_clear_media
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
        media: Medium,
        modifier: Modifier = Modifier,
        imageState: CoilImageState = rememberCoilImageState(media.coverImage?.medium),
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
