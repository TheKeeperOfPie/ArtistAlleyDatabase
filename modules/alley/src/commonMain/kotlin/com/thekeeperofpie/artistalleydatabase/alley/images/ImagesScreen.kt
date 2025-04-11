package com.thekeeperofpie.artistalleydatabase.alley.images

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.animateZoomBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_details_close_image
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistTitle
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyTitle
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.utils_compose.MultiZoomPanState
import com.thekeeperofpie.artistalleydatabase.utils_compose.ZoomSlider
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object ImagesScreen {

    @Composable
    operator fun invoke(
        route: Destinations.Images,
        images: () -> List<CatalogImage>,
        imagePagerState: PagerState,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        when (val title = route.title) {
                            is Destinations.Images.Title.Artist ->
                                ArtistTitle(
                                    year = route.year,
                                    id = route.id,
                                    booth = title.booth,
                                    name = title.name,
                                    useSharedElement = false,
                                )
                            is Destinations.Images.Title.StampRally ->
                                StampRallyTitle(
                                    id = route.id,
                                    hostTable = title.hostTable,
                                    fandom = title.fandom,
                                    useSharedElement = false,
                                )
                        }
                    },
                    navigationIcon = {
                        val navigationController = LocalNavigationController.current
                        IconButton(onClick = { navigationController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.alley_details_close_image),
                            )
                        }
                    },
                    modifier = Modifier
                        .sharedBounds("container", route.id, zIndexInOverlay = 1f)
                )
            }
        ) {
            Column(Modifier.padding(it)) {
                val images = images()
                val zoomPanStates = rememberSaveable(
                    images,
                    LocalDensity.current,
                    saver = MultiZoomPanState.Saver
                ) {
                    MultiZoomPanState(images.size)
                }
                ImagePager(
                    images = images,
                    pagerState = imagePagerState,
                    sharedElementId = route.id,
                    onClickPage = null,
                    clipCorners = false,
                    forceMinHeight = false,
                    imageContentScale = ContentScale.Fit,
                    zoomPanStates = zoomPanStates,
                    modifier = Modifier.weight(1f)
                )

                if (images.isNotEmpty()) {
                    val imageIndex = imagePagerState.currentPage - 1
                    val zoomPanState = zoomPanStates[imageIndex.coerceAtLeast(0)]
                    val scope = rememberCoroutineScope()
                    val alpha by animateFloatAsState(if (imageIndex >= 0 || images.size == 1) 1f else 0f)
                    ZoomSlider(
                        scale = { zoomPanState.scale },
                        onScaleChange = zoomPanState::onZoomChange,
                        scaleRange = (1f..5f),
                        onClickZoomOut = {
                            scope.launch {
                                zoomPanState.transformableState.animateZoomBy(0.8f)
                            }
                        },
                        onClickZoomIn = {
                            scope.launch {
                                zoomPanState.transformableState.animateZoomBy(1.2f)
                            }
                        },
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .widthIn(max = 480.dp)
                            .align(Alignment.CenterHorizontally)
                            .graphicsLayer { this.alpha = alpha }
                    )
                }
            }
        }
    }
}
