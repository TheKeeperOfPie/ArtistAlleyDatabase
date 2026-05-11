package com.thekeeperofpie.artistalleydatabase.alley.images

import androidx.annotation.ColorInt
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.utils.ImageWithDimensions
import kotlinx.serialization.Serializable

@Serializable
data class CatalogImage(
    val uri: Uri,
    override val width: Int?,
    override val height: Int?,
    @ColorInt
    val color: Int? = null,
) : ImageWithDimensions {
    override val coilImageModel: Uri get() = uri
}

object CatalogImagePreviewProvider : PreviewParameterProvider<CatalogImage> {
    override val values = generateSequence<Pair<Int, CatalogImage?>>(0 to null) { (count, value) ->
        (count + 1) to CatalogImage(
            uri = Uri.parse("jar:file/composeResources/files/catalogs/A01/$count.webp"),
            width = 200,
            height = 100
        )
    }.filter { it.second != null }
        .map { it.second!! }
}
