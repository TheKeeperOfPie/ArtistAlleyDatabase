package com.thekeeperofpie.artistalleydatabase.alley.images

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_details_close_image
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistTitle
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyTitle
import com.thekeeperofpie.artistalleydatabase.alley.ui.ImagePager
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object ImagesScreen {

    @Composable
    operator fun invoke(
        route: Destinations.Images,
        images: () -> List<CatalogImage>,
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
                                )
                            is Destinations.Images.Title.StampRally ->
                                StampRallyTitle(
                                    id = route.id,
                                    hostTable = title.hostTable,
                                    fandom = title.fandom,
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
            val pagerState = rememberImagePagerState(
                images = images,
                initialImageIndex = route.initialImageIndex ?: 0,
            )
            val images = images()
            ImagePager(
                images = images,
                pagerState = pagerState,
                sharedElementId = route.id,
                onClickPage = null,
                clipCorners = false,
                imageContentScale = ContentScale.Fit,
                modifier = Modifier.padding(it)
            )
        }
    }
}
