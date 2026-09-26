package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.runtime.snapshotFlow
import coil3.ComponentRegistry
import coil3.ImageLoader
import coil3.fetch.Fetcher
import coil3.key.Keyer
import coil3.map.Mapper
import coil3.request.Options
import coil3.toUri
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesId
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.utils.ImageWithDimensions
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first
import com.eygraber.uri.Uri as KmpUri

@Inject
class AlleyCoilInit(
    private val seriesImageLoader: SeriesImageLoader,
) {

    context(builder: ComponentRegistry.Builder)
    fun addComponents() {
        builder.run {
            add(Mapper<CatalogImage, KmpUri> { data, _ -> data.coilImageModel })
            add(Mapper<ImageWithDimensions, Any> { data, _ -> data.coilImageModel })
            add(Mapper<ImageWithDimensions, KmpUri> { data, _ -> data.coilImageModel as? KmpUri })
            add(Keyer<KmpUri> { data, _ -> data.toString() })
            add(Keyer<CatalogImage> { data, _ -> data.coilImageModel.toString() })
            add(Keyer<ImageWithDimensions> { data, _ -> (data.coilImageModel as? Uri).toString() })
            add(Keyer<SeriesId> { data, _ -> "series-image-${data.id}" })
            add(Keyer<SeriesInfo> { data, _ -> "series-image-${data.id}" })
            add(Keyer<SeriesImageInfo> { data, _ -> "series-image-${data.id}" })

            add(DelegateFetcherFactory<SeriesId> {
                seriesImageLoader.getSeriesImage(it.id)
                    ?: snapshotFlow { seriesImageLoader.getSeriesImage(it.id) }.first { it != null }
            })
            add(DelegateFetcherFactory<SeriesInfo> {
                seriesImageLoader.getSeriesImage(it)
                    ?: snapshotFlow { seriesImageLoader.getSeriesImage(it) }.first { it != null }
            })
            add(DelegateFetcherFactory<SeriesImageInfo> {
                seriesImageLoader.getSeriesImage(it)
                    ?: snapshotFlow { seriesImageLoader.getSeriesImage(it) }.first { it != null }
            })
        }
    }

    private class DelegateFetcherFactory<T : Any>(
        private val transform: suspend (T) -> String?,
    ) : Fetcher.Factory<T> {
        override fun create(
            data: T,
            options: Options,
            imageLoader: ImageLoader,
        ) = Fetcher {
            val uri = transform(data)?.let(Uri::parseOrNull) ?: return@Fetcher null
            val fetcher = imageLoader.components.newFetcher(uri, options, imageLoader)?.first
                ?: imageLoader.components.newFetcher(uri.toString().toUri(), options, imageLoader)?.first
            fetcher?.fetch()
        }
    }
}
