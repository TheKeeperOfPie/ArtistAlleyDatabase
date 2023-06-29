package com.thekeeperofpie.artistalleydatabase.alley.details

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.compose.InfoText
import com.thekeeperofpie.artistalleydatabase.compose.ZoomPanBox
import com.thekeeperofpie.artistalleydatabase.compose.expandableListInfoText
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
object ArtistDetailsScreen {

    private val IMAGE_HEIGHT = 320.dp

    @Composable
    operator fun invoke(viewModel: ArtistDetailsViewModel, onClickBack: () -> Unit) {
        val entry = viewModel.entry
        if (entry == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                CircularProgressIndicator()
            }
            return
        }

        var showFullImagesIndex by rememberSaveable { mutableStateOf<Int?>(null) }

        val artist = entry.artist
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(
                                R.string.alley_artist_details_booth_and_table_name,
                                artist.booth,
                                artist.tableName.orEmpty(),
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    actions = {
                        IconButton(onClick = { viewModel.onFavoriteToggle(!entry.favorite) }) {
                            Icon(
                                imageVector = if (entry.favorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Filled.FavoriteBorder
                                },
                                contentDescription = stringResource(
                                    R.string.alley_artist_favorite_icon_content_description
                                ),
                            )
                        }
                    }
                )
            }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                val images = viewModel.images
                if (viewModel.images.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BrokenImage,
                            contentDescription = stringResource(
                                R.string.alley_artist_catalog_image_none
                            )
                        )
                    }
                } else {
                    val pagerState = rememberPagerState(pageCount = { images.size })
                    val context = LocalContext.current
                    val targetHeight =
                        LocalDensity.current.run {
                            Dimension.Pixels(
                                IMAGE_HEIGHT.toPx().roundToInt()
                            )
                        }
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .height(IMAGE_HEIGHT)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        ZoomPanBox(onClick = { showFullImagesIndex = null }) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .height(IMAGE_HEIGHT)
                                    .fillMaxWidth()
                                    .clickable { showFullImagesIndex = it }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(images[it])
                                        .size(width = Dimension.Undefined, targetHeight)
                                        .build(),
                                    contentScale = ContentScale.Fit,
                                    contentDescription = stringResource(R.string.alley_artist_catalog_image),
                                    modifier = Modifier.height(IMAGE_HEIGHT),
                                )
                            }
                        }
                    }
                }

                ElevatedCard(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                ) {
                    var shown = false
                    if (!artist.tableName.isNullOrBlank()) {
                        InfoText(
                            stringResource(R.string.alley_artist_details_table_name),
                            artist.tableName,
                            showDividerAbove = false,
                        )
                        shown = true
                    }

                    if (!artist.region.isNullOrBlank()) {
                        InfoText(
                            stringResource(R.string.alley_artist_details_region),
                            artist.region,
                            showDividerAbove = shown,
                        )
                        shown = true
                    }

                    expandableListInfoText(
                        labelTextRes = R.string.alley_artist_details_artist_names,
                        contentDescriptionTextRes = R.string.alley_artist_details_artist_names_expand_content_description,
                        values = artist.artistNames.map {
                            it.split("\n")
                                .mapIndexed { index, name -> if (index == 0) name else "\t\t\t$name" }
                                .joinToString(separator = "\n")
                        },
                        valueToText = { it },
                        showDividerAbove = shown,
                    )
                }

                if (!artist.description.isNullOrBlank()) {
                    ElevatedCard(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp)
                            .animateContentSize()
                    ) {
                        InfoText(
                            stringResource(R.string.alley_artist_details_description),
                            artist.description,
                            showDividerAbove = false
                        )
                    }
                }

                val uriHandler = LocalUriHandler.current
                if (entry.links.isNotEmpty()) {
                    ElevatedCard(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp)
                            .animateContentSize()
                    ) {
                        expandableListInfoText(
                            labelTextRes = R.string.alley_artist_details_links,
                            contentDescriptionTextRes = R.string.alley_artist_details_links_expand_content_description,
                            values = entry.links,
                            valueToText = { it },
                            onClick = {
                                try {
                                    uriHandler.openUri(it)
                                } catch (ignored: Throwable) {
                                }
                            },
                            showDividerAbove = false,
                        )
                    }
                }

                if (entry.catalogLinks.isNotEmpty()) {
                    ElevatedCard(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp)
                            .animateContentSize()
                    ) {
                        expandableListInfoText(
                            labelTextRes = R.string.alley_artist_details_catalog_links,
                            contentDescriptionTextRes = R.string.alley_artist_details_catalog_links_expand_content_description,
                            values = entry.catalogLinks,
                            valueToText = { it },
                            onClick = {
                                try {
                                    uriHandler.openUri(it)
                                } catch (ignored: Throwable) {
                                }
                            },
                            showDividerAbove = false,
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }

        if (showFullImagesIndex != null) {
            BackHandler { showFullImagesIndex = null }
            Surface(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp)
                    .copy(alpha = 0.4f)
            ) {
                val images = viewModel.images
                val pagerState = rememberPagerState(pageCount = { images.size })
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 16.dp,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ZoomPanBox(onClick = { showFullImagesIndex = null }) {
                        AsyncImage(
                            model = images[it],
                            contentScale = ContentScale.Fit,
                            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                            contentDescription = stringResource(R.string.alley_artist_catalog_image),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .clickable(
                                    // Consume click events so that tapping image doesn't dismiss
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {}
                        )
                    }
                }
            }
        }
    }
}
