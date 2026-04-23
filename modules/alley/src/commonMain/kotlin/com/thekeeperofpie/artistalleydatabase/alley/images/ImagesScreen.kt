package com.thekeeperofpie.artistalleydatabase.alley.images

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_details_close_image
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistTitle
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyTitle
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ZoomSlider
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationRequestKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.rememberMultiZoomableState
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object ImagesScreen {

    val REQUEST_KEY = NavigationRequestKey<Int>("ImagesScreen")

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        route: AlleyDestination.Images,
        onNavigateBack: () -> Unit,
        viewModel: ImagesViewModel = viewModel { graph.imagesViewModel() },
    ) {
        val data by produceState(route.type to route.images.orEmpty(), route, viewModel) {
            if (route.images.isNullOrEmpty()) {
                viewModel.load(route)?.let {
                    value = it
                }
            }
        }
        val (type, images) = data
        ImagesScreen(
            year = route.year,
            id = route.id,
            initialImageIndex = route.initialImageIndex ?: 1,
            type = type,
            images = images,
            onNavigateBack = onNavigateBack,
        )
    }

    @Composable
    operator fun invoke(
        year: DataYear,
        id: String,
        initialImageIndex: Int?,
        type: AlleyDestination.Images.Type,
        images: List<CatalogImage>,
        onNavigateBack: () -> Unit,
    ) {
        val imagePagerState = rememberImagePagerState(
            images = images,
            initialImageIndex = initialImageIndex ?: 0,
        )
        val navigationResults = LocalNavigationResults.current
        LaunchedEffect(imagePagerState, navigationResults) {
            snapshotFlow { imagePagerState.targetPage }
                .collectLatest {
                    navigationResults[REQUEST_KEY] = it
                }
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        when (type) {
                            is AlleyDestination.Images.Type.Artist ->
                                ArtistTitle(
                                    year = year,
                                    id = id,
                                    booth = type.booth,
                                    profileImage = type.profileImage,
                                    name = type.name,
                                    useSharedElement = false,
                                )
                            is AlleyDestination.Images.Type.StampRally ->
                                StampRallyTitle(
                                    year = year,
                                    id = id,
                                    hostTable = type.hostTable,
                                    fandom = type.fandom,
                                    useSharedElement = false,
                                )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.alley_details_close_image),
                            )
                        }
                    },
                    modifier = Modifier
                        .sharedBounds("container", id, zIndexInOverlay = 1f)
                )
            }
        ) {
            Column(Modifier.padding(it)) {
                val multiZoomableState = rememberMultiZoomableState(images.size)
                ImagePager(
                    images = images,
                    pagerState = imagePagerState,
                    sharedElementId = id,
                    onClickPage = null,
                    onClickFullscreen = null,
                    clipCorners = false,
                    forceMinHeight = false,
                    imageContentScale = ContentScale.Fit,
                    multiZoomableState = multiZoomableState,
                    modifier = Modifier.weight(1f)
                )

                if (images.isNotEmpty()) {
                    val imageIndex = imagePagerState.currentPage - 1
                    val zoomPanState = multiZoomableState[imageIndex.coerceAtLeast(0)]
                    val alpha by animateFloatAsState(if (imageIndex >= 0 || images.size == 1) 1f else 0f)
                    ZoomSlider(
                        state = zoomPanState,
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
